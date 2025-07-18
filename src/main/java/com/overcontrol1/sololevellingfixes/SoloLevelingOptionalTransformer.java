package com.overcontrol1.sololevellingfixes;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

public class SoloLevelingOptionalTransformer implements ILaunchPluginService {
    private static final EnumSet<Phase> YAY = EnumSet.of(Phase.BEFORE);
    private static final EnumSet<Phase> NAY = EnumSet.noneOf(Phase.class);

    public SoloLevelingOptionalTransformer() {

    }

    @Override
    public void initializeLaunch(ITransformerLoader transformerLoader, NamedPath[] specialPaths) {
        try {
            Files.delete(Path.of("solocraft_classes.txt"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "solo_levelling_fixes";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return !isEmpty && classType.getClassName().startsWith("net.solocraft") ? YAY : NAY;
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        boolean modified = false;
        for (MethodNode node : classNode.methods) {
            var insns = node.instructions;

            for (int i = 0; i < insns.size(); i++) {
                var insn = insns.get(i);
                if (!(insn instanceof TypeInsnNode typeInsn)) continue;
                if (insn.getOpcode() != Opcodes.NEW) continue;
                if (!typeInsn.desc.equals("net/solocraft/network/SololevelingModVariables$PlayerVariables"))
                    continue;

                var dup = nextCodeInsn(insn);
                if (dup.getOpcode() != Opcodes.DUP) continue;
                if (!(nextCodeInsn(dup) instanceof MethodInsnNode ctor)) continue;
                if (!ctor.name.equals("<init>") || !ctor.owner.equals(typeInsn.desc)) continue;
                if (!(nextCodeInsn(ctor) instanceof MethodInsnNode orElse)) continue;
                if (!orElse.name.equals("orElse") || !orElse.desc.equals("(Ljava/lang/Object;)Ljava/lang/Object;"))
                    continue;

                modified = true;

                insns.remove(ctor);
                insns.remove(dup);
                insns.remove(typeInsn);

                var staticCall = staticCall();
                insns.insertBefore(orElse, staticCall);

                orElse.name = "orElseGet";
                orElse.desc = "(Lnet/minecraftforge/common/util/NonNullSupplier;)Ljava/lang/Object;";
                i = Math.max(i - 3, 0);
            }
        }

        return modified;
    }

    private static MethodInsnNode staticCall() {
        return new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "com/overcontrol1/sololevellingfixes/DefaultPlayerVariablesStorage",
                "getVariablesSupplier",
                "()Lnet/minecraftforge/common/util/NonNullSupplier;",
                false);
    }

    private static InvokeDynamicInsnNode methodRef(String desc) {
        Handle bootstrap = new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory",
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;"
                        + "Ljava/lang/String;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;"
                        + "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                false
        );

        Handle constructor = new Handle(
                Opcodes.H_NEWINVOKESPECIAL,
                desc,
                "<init>",
                "()V",
                false
        );

        return new InvokeDynamicInsnNode(
                "get",
                "()Lnet/minecraftforge/common/util/NonNullSupplier;",
                bootstrap,
                Type.getType("()Ljava/lang/Object;"),
                constructor,
                Type.getMethodType("()L" + desc + ";")
        );
    }

    private static AbstractInsnNode nextCodeInsn(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();

        while (!isCodeInsn(next)) {
            next = next.getNext();
        }

        return next;
    }

    private static boolean isCodeInsn(AbstractInsnNode insn) {
        int type = insn.getType();

        return (type != AbstractInsnNode.LABEL &&
                type != AbstractInsnNode.LINE &&
                type != AbstractInsnNode.FRAME);
    }

    private static void dumpClassNode(ClassNode classNode, Type classType) {
        final var dirs = classType.getInternalName().split("/");
        dirs[dirs.length - 1] = dirs[dirs.length - 1] + ".class";
        final var trimmedDirs = Arrays.copyOf(dirs, dirs.length - 1);
        final Path dirPath = Path.of("transformed_classes", trimmedDirs);
        final Path filePath = dirPath.resolve(dirs[dirs.length - 1]);
        try {
            Files.createDirectories(dirPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try (var fileWriter = new BufferedOutputStream(Files.newOutputStream(filePath))) {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            byte[] bytecode = classWriter.toByteArray();

            fileWriter.write(bytecode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void inject() throws Throwable {
        final LaunchPluginHandler handler = (LaunchPluginHandler) Reflections.HANDLE.findVarHandle(Launcher.class, "launchPlugins", LaunchPluginHandler.class)
                .get(Launcher.INSTANCE);
        final Map<String, ILaunchPluginService> plugins = (Map<String, ILaunchPluginService>) Reflections.HANDLE.findVarHandle(LaunchPluginHandler.class, "plugins", Map.class)
                .get(handler);
        final SoloLevelingOptionalTransformer transformer = new SoloLevelingOptionalTransformer();
        plugins.put(transformer.name(), transformer);
    }
}

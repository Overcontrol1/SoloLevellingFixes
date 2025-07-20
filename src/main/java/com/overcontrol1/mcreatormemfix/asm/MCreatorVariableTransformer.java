package com.overcontrol1.mcreatormemfix.asm;

import com.google.gson.*;
import com.overcontrol1.mcreatormemfix.ModConfigEntry;
import com.overcontrol1.mcreatormemfix.Reflections;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraftforge.fml.loading.FMLPaths;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.service.MixinService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MCreatorVariableTransformer implements ILaunchPluginService {
    private static final EnumSet<Phase> YAY = EnumSet.of(Phase.BEFORE);
    private static final EnumSet<Phase> NAY = EnumSet.noneOf(Phase.class);
    private static final List<ModConfigEntry> modEntries = new ArrayList<>();
    private static final Map<String, VariableTransformationProfile> createdTemplates = new Object2ObjectArrayMap<>();

    private static final String STORAGE_INTERNAL_CLASS_NAME = "com/overcontrol1/mcreatormemfix/MCreatorPlayerVariablesStorage";

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("mcreator_mem_fix.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MCreatorVariableTransformer() {
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
                var object = JsonParser.parseReader(reader).getAsJsonObject();
                var array = object.getAsJsonArray("mods");

                for (JsonElement e : array) {
                    var o = e.getAsJsonObject();
                    modEntries.add(new ModConfigEntry(o.get("package").getAsJsonPrimitive().getAsString(), o.get("className").getAsJsonPrimitive().getAsString()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (modEntries.isEmpty()) {
            modEntries.add(new ModConfigEntry("net.solocraft", "Sololeveling"));
            modEntries.add(new ModConfigEntry("net.mcreator.jujutsucraft", "Jujutsucraft"));

            try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
                var object = new JsonObject();
                var array = new JsonArray();

                for (ModConfigEntry entry : modEntries) {
                    var subObject = new JsonObject();
                    subObject.addProperty("package", entry.packageName());
                    subObject.addProperty("className", entry.className());
                    array.add(subObject);
                }

                object.add("mods", array);

                gson.toJson(object, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (ModConfigEntry entry : modEntries) {
            generate(entry);
        }

        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, VariableTransformationProfile> entry : createdTemplates.entrySet()) {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode(entry.getValue().innerClassInternalName);
            } catch (ClassNotFoundException | IOException e) {
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(createdTemplates::remove);
    }

    @Override
    public String name() {
        return "mcreator_mem_fix";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        if (classType.getInternalName().equals(STORAGE_INTERNAL_CLASS_NAME)) return YAY;

        return !isEmpty && modEntries.stream().anyMatch(s -> classType.getClassName().startsWith(s.packageName())) ? YAY : NAY;
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        if (classType.getInternalName().equals(STORAGE_INTERNAL_CLASS_NAME)) {
            processStorageClass(classNode, classType);
            return true;
        }
        VariableTransformationProfile variableTransformationProfile = getGeneratedMetadata(classType);

        boolean modified = false;
        for (MethodNode node : classNode.methods) {
            var insns = node.instructions;

            for (int i = 0; i < insns.size(); i++) {
                var insn = insns.get(i);
                if (!(insn instanceof TypeInsnNode typeInsn)) continue;
                if (insn.getOpcode() != Opcodes.NEW) continue;
                if (!typeInsn.desc.equals(variableTransformationProfile.innerClassInternalName))
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

                var staticCall = staticCall(variableTransformationProfile.lowercaseModName);
                insns.insertBefore(orElse, staticCall);

//                insns.insertBefore(orElse, methodRef(typeInsn.desc));

                orElse.name = "orElseGet";
                orElse.desc = "(Lnet/minecraftforge/common/util/NonNullSupplier;)Ljava/lang/Object;";
                i = Math.max(i - 3, 0);
            }
        }

        return modified;
    }

    private void processStorageClass(ClassNode classNode, Type classType) {
        for (VariableTransformationProfile profile : createdTemplates.values()) {
            MethodNode supplierMethod =
                    DefaultVariableClassWriter.createStaticSupplierMethod(
                            classType.getInternalName(),
                            profile.lowercaseModName,
                            profile.innerClassInternalName);
            MethodNode lambda =
                    DefaultVariableClassWriter.createLambda(
                            classType.getInternalName(),
                            profile.lowercaseModName,
                            profile.innerClassInternalName);
            FieldNode staticField =
                    DefaultVariableClassWriter.createField(
                            classType.getInternalName(),
                            profile.lowercaseModName,
                            profile.innerClassInternalName);

            boolean hasProcessedClinit = false;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<clinit>")) {
                    DefaultVariableClassWriter.addFieldInit(
                            methodNode,
                            classType.getInternalName(),
                            profile.lowercaseModName,
                            profile.innerClassInternalName
                            );
                    hasProcessedClinit = true;
                    break;
                }
            }

            if (!hasProcessedClinit) {
                MethodNode clinit = DefaultVariableClassWriter.createClinit();
                clinit.instructions.add(new LabelNode());
                clinit.instructions.add(new InsnNode(Opcodes.RETURN));
                DefaultVariableClassWriter.addFieldInit(
                        clinit,
                        classType.getInternalName(),
                        profile.lowercaseModName,
                        profile.innerClassInternalName);
                classNode.methods.add(clinit);
            }

            classNode.methods.add(supplierMethod);
            classNode.methods.add(lambda);
            classNode.fields.add(staticField);
        }
    }

    private VariableTransformationProfile getGeneratedMetadata(Type classType) {
        ModConfigEntry chosenEntry = null;

        for (ModConfigEntry entry : modEntries) {
            if (classType.getClassName().startsWith(entry.packageName())) {
                chosenEntry = entry;
                break;
            }
        }

        if (chosenEntry == null) throw new IllegalStateException("Something went wrong generating a template storage.");

        var generationResult = createdTemplates.get(chosenEntry.packageName());

        if (generationResult != null) {
            return generationResult;
        }

        throw new IllegalStateException("Something went wrong. Tried to get a generated variable storage when none existed for " + chosenEntry.packageName());
    }

    private void generate(ModConfigEntry entry) {
        String outerClassName = entry.packageName() + ".network." + entry.className() + "ModVariables";
        String outerClassInternalName = outerClassName.replace('.', '/');
        String innerClassInternalName = outerClassInternalName + "$PlayerVariables";


        createdTemplates.put(entry.packageName(),
                new VariableTransformationProfile(outerClassName,
                        outerClassInternalName, innerClassInternalName,
                        entry.className().toLowerCase()));
    }

    private record VariableTransformationProfile(String outerClassName, String outerClassInternalName,
                                                 String innerClassInternalName, String lowercaseModName) {

    }

    private static MethodInsnNode staticCall(String lowercaseClassName) {
        return new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                MCreatorVariableTransformer.STORAGE_INTERNAL_CLASS_NAME,
                "get_variables_" + lowercaseClassName,
                "()Lnet/minecraftforge/common/util/NonNullSupplier;",
                false);
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
        final MCreatorVariableTransformer transformer = new MCreatorVariableTransformer();
        plugins.put(transformer.name(), transformer);
    }
}

package com.overcontrol1.mcreatormemfix.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class DefaultVariableClassWriter {
    public static MethodNode createStaticSupplierMethod(String ownerInternalName, String name, String innerClassInternalName) {
        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "get_variables_" + name, "()Lnet/minecraftforge/common/util/NonNullSupplier;",
                "()Lnet/minecraftforge/common/util/NonNullSupplier<L" + innerClassInternalName + ";>;", null);


        method.instructions.add(new LabelNode());
        method.instructions.add(generateIndy(ownerInternalName, innerClassInternalName, "lambda$" + name));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 1;
        method.maxLocals = 0;

        return method;
    }

    public static MethodNode createLambda(String ownerInternalName, String name, String innerClassInternalName) {
        MethodNode method = new MethodNode(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC,
                "lambda$" + name, "()L" + innerClassInternalName + ";",
                null, null
        );

        method.instructions.add(new LabelNode());
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, ownerInternalName, "variables_" + name,
                "L" + innerClassInternalName + ";"));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 1;
        method.maxLocals = 0;
        return method;
    }

    public static FieldNode createField(String ownerInternalName, String name, String innerClassInternalName) {
        return new FieldNode(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                "variables_" + name,
                "L" + innerClassInternalName + ";", null, null
        );
    }

    public static void addFieldInit(MethodNode clinit, String ownerInternalName, String name, String innerClassInternalName) {
        InsnList tempList = new InsnList();

        tempList.add(new TypeInsnNode(Opcodes.NEW, innerClassInternalName));
        tempList.add(new InsnNode(Opcodes.DUP));
        tempList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, innerClassInternalName, "<init>", "()V"));
        tempList.add(new FieldInsnNode(Opcodes.PUTSTATIC, ownerInternalName,
                "variables_" + name,
                "L" + innerClassInternalName + ";"));

        for (int i = 0; i < clinit.instructions.size(); i++) {
            AbstractInsnNode insn = clinit.instructions.get(i);
            if (insn.getOpcode() == Opcodes.RETURN) {
                clinit.instructions.insertBefore(insn, tempList);
                break;
            }
        }
    }

    private static InvokeDynamicInsnNode generateIndy(String ownerName, String innerClassInternalName, String lambdaName) {
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

        Handle syntheticHandle = new Handle(
                Opcodes.H_INVOKESTATIC,
                ownerName,
                lambdaName,
                "()L" + innerClassInternalName + ";",
                false
        );

        return new InvokeDynamicInsnNode(
                "get",
                "()Lnet/minecraftforge/common/util/NonNullSupplier;",
                bootstrap,
                Type.getType("()Ljava/lang/Object;"),
                syntheticHandle,
                Type.getMethodType("()L" + innerClassInternalName + ";")
        );
    }

    public static MethodNode createClinit() {
        return new MethodNode(
                Opcodes.ACC_STATIC,
                "<clinit>",
                "()V",
                null, null
        );
    }
}

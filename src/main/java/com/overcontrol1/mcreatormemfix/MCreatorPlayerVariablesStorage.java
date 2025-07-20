package com.overcontrol1.mcreatormemfix;

import net.minecraftforge.common.util.NonNullSupplier;

public class MCreatorPlayerVariablesStorage {
    private static final TemplateVariablesClass.InnerClass variables_template = new TemplateVariablesClass.InnerClass();

    public static NonNullSupplier<TemplateVariablesClass.InnerClass> template_supplier() {
        return () -> variables_template;
    }
}
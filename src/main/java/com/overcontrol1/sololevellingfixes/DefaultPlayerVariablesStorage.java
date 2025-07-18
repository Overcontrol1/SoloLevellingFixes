package com.overcontrol1.sololevellingfixes;

import net.minecraftforge.common.util.NonNullSupplier;
import net.solocraft.network.SololevelingModVariables;

public class DefaultPlayerVariablesStorage {
    private static final SololevelingModVariables.PlayerVariables solo_levelling_fixes$default_variables = new SololevelingModVariables.PlayerVariables();

    public static NonNullSupplier<SololevelingModVariables.PlayerVariables> getVariablesSupplier() {
        return () -> solo_levelling_fixes$default_variables;
    }
}

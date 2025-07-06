package com.overcontrol1.sololevellingfixes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.overcontrol1.sololevellingfixes.SoloLevellingFixes;
import com.overcontrol1.sololevellingfixes.duck.SoloLevellingFixesPlayer;
import net.minecraft.world.entity.Entity;
import net.solocraft.network.SololevelingModVariables;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = SololevelingModVariables.PlayerVariables.class, remap = false)
public class PlayerVariablesMixin {
    @WrapMethod(method = "syncPlayerVariables")
    private void solo_levelling_fixes$hijack_sync_spam(Entity entity, Operation<Void> original) {
        if (SoloLevellingFixes.SYNC_LOCK_SWITCH) {
            original.call(entity);
        } else {
            if (entity instanceof SoloLevellingFixesPlayer extendedPlayer) {
                extendedPlayer.solo_levelling_fixes$mark_sync_lock();
            }
        }
    }
}

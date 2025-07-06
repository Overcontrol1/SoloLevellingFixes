package com.overcontrol1.sololevellingfixes.mixin;

import com.overcontrol1.sololevellingfixes.duck.SoloLevellingFixesPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements SoloLevellingFixesPlayer {
    @Unique
    private boolean solo_levelling_fixes$sync_lock;

    @Override
    public boolean solo_levelling_fixes$sync_lock() {
        return solo_levelling_fixes$sync_lock;
    }

    @Override
    public void solo_levelling_fixes$set_sync_lock(boolean value) {
        solo_levelling_fixes$sync_lock = value;
    }
}

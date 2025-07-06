package com.overcontrol1.sololevellingfixes;

import com.overcontrol1.sololevellingfixes.duck.SoloLevellingFixesPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.solocraft.network.SololevelingModVariables;

@Mod(SoloLevellingFixes.MOD_ID)
public class SoloLevellingFixes {
    public static final String MOD_ID = "solo_levelling_fixes";
    public static boolean SYNC_LOCK_SWITCH = false;

    public SoloLevellingFixes(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.addListener(this::serverPlayerTick);
    }

    private void serverPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            var player = event.player;
            var extensionPlayer = (SoloLevellingFixesPlayer) player;

            if (extensionPlayer.solo_levelling_fixes$sync_lock()) {
                player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {
                    SYNC_LOCK_SWITCH = true;
                    vars.syncPlayerVariables(player);
                    extensionPlayer.solo_levelling_fixes$set_sync_lock(false);
                    SYNC_LOCK_SWITCH = false;
                });
            }
        }
    }
}

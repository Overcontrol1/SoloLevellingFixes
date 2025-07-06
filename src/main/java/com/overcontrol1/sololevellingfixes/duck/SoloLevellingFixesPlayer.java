package com.overcontrol1.sololevellingfixes.duck;

public interface SoloLevellingFixesPlayer {
    boolean solo_levelling_fixes$sync_lock();
    void solo_levelling_fixes$set_sync_lock(boolean value);

    default void solo_levelling_fixes$mark_sync_lock() {
        solo_levelling_fixes$set_sync_lock(true);
    }
}

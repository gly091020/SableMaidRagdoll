package com.gly091020.SableMaidRagdoll.util;

import java.util.Locale;

public enum MaidRagdollAdvancementEvents {
    HIT_MAID, CONTROL_MAID, DOUBLE_CANNON;

    public String getName(){
        return name().toLowerCase(Locale.ROOT);
    }
}

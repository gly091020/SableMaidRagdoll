package com.gly091020.SableMaidRagdoll.compat.util;

import net.neoforged.fml.ModList;

public class CompatMods {
    public static final boolean LOVE_LOATHE = ModList.get().isLoaded("callresponse");
    public static final boolean PLAYER_RAGDOLL = ModList.get().isLoaded("sable_player_ragdoll");
}

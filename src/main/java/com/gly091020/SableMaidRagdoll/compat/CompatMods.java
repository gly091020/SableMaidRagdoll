package com.gly091020.SableMaidRagdoll.compat;

import net.neoforged.fml.loading.LoadingModList;

public enum CompatMods {
    LOVE_LOATHE("callresponse"),
    PLAYER_RAGDOLL("sable_player_ragdoll"),
    LAOWU_WINE_FOX("laowu_maid_1786527352"),
    LITTLE_MAID_REBIRTH("littlemaidrebirth"),
    WINFOX_LITTLE_MAID("touhou_little_maid"),
    WINFOX_LITTLE_MAID_SPELL("touhou_little_maid_spell"),
    REVIVE_MAID("revivemaid");

    final String modID;
    final boolean isLoaded;
    CompatMods(String modID){
        this.modID = modID;
        this.isLoaded = LoadingModList.get().getModFileById(modID) != null;
    }

    public String getModID() {
        return modID;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}

package com.gly091020.SableMaidRagdoll.maid.tlm.util;

import net.minecraft.resources.ResourceLocation;

public class WineFoxUtil {
    private static final String[] WINE_FOX_MAID_MODELS = {
            "touhou_little_maid_spell:stellar_witch"
    };

    public static boolean isWineFox(String modelID){
        for (String v: WINE_FOX_MAID_MODELS)
            if(v.equals(modelID))return true;
        return modelID.contains("wine") && modelID.contains("fox");
    }

    public static boolean isWineFox(ResourceLocation ragdollID){
        return isWineFox(ragdollID.getPath().replace("/", ":"));
    }
}

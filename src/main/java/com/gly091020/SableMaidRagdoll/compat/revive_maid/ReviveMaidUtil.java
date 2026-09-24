package com.gly091020.SableMaidRagdoll.compat.revive_maid;

import com.github.JumDa5he.revivemaid.data.PossessionData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class ReviveMaidUtil {
    @Nullable
    public static String getPlayerPossessionTarget(ServerPlayer player){
        PossessionData data = PossessionData.load(player);
        if (data != null) {
            String modelId = data.modelId();
            if(data.ysmModel())return null;
            return modelId;
        }
        return null;
    }

    public static void init(){
        NeoForge.EVENT_BUS.register(ReviveMaidEventHandler.class);
    }
}

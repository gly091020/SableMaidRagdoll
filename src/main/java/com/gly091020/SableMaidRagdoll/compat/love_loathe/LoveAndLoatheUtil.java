package com.gly091020.SableMaidRagdoll.compat.love_loathe;

import com.github.JumDa5he.callresponse.init.InitAttachTypes;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public class LoveAndLoatheUtil {
    public static void removeWanderTag(EntityMaid maid){
        maid.removeData(InitAttachTypes.SYNCED_WANDERING_SPECIAL);
    }
}

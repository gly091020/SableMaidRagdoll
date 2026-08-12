package com.gly091020.SableMaidRagdoll.compat.love_loathe;

import com.github.JumDa5he.callresponse.compat.api.broadcast.BroadcastManager;
import com.github.JumDa5he.callresponse.compat.api.broadcast.SimpleBroadcast;
import com.gly091020.SableMaidRagdoll.item.CheatDeathBauble;

public class RagdollBroadcastMessages {
    public static void init(){
        BroadcastManager.registry(new SimpleBroadcast((maid, player) -> {
            CheatDeathBauble.toRagdoll(maid, 20);
        }, "装死"));
    }
}

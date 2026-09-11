package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.maid.tlm.TLMMaidRagdoll;
import net.neoforged.bus.api.IEventBus;

public class InitMaidRagdoll {
    public static void init(IEventBus bus){
        // 如果哪天酒狐女仆社区真的炸了
        // 我可以随时删除这行代码……
        if(CompatMods.WINFOX_LITTLE_MAID.isLoaded())
            MaidRagdollTypesManager.registry(new TLMMaidRagdoll());

        MaidRagdollTypesManager.getTypes().forEach(a -> a.init(bus));
    }
}

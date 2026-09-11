package com.gly091020.SableMaidRagdoll.maid.tlm;

import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.ToolRegister;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.MaidEdibleBlockManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.compat.tlm.RollTool;
import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableMaidRagdoll.maid.tlm.bauble.CheatDeathBauble;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.TNTCakeEdible;

@LittleMaidExtension
public class TLMMaidRagdollPlugin implements ILittleMaid {
    @Override
    public void bindMaidBauble(BaubleManager manager) {
        manager.bind(InitItems.CHEAT_DEATH_BAUBLE_ITEM.get(), new CheatDeathBauble());
    }

    @Override
    public void registerAITool(ToolRegister register) {
        if(SableMaidRagdoll.CONFIG.loveAndLoathe.moreAIFunction && CompatMods.LOVE_LOATHE.isLoaded())
            register.register(new RollTool());
    }

    @Override
    public void registerMaidEdibleBlock(MaidEdibleBlockManager manager) {
        manager.add(new TNTCakeEdible());
    }
}

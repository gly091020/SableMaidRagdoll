package com.gly091020.SableMaidRagdoll;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.gly091020.SableMaidRagdoll.item.CheatDeathBauble;

@LittleMaidExtension
public class SableMaidRagdollPlugin implements ILittleMaid {
    @Override
    public void bindMaidBauble(BaubleManager manager) {
        manager.bind(SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get(), new CheatDeathBauble());
    }
}

package com.gly091020.SableMaidRagdoll.item;

import com.gly091020.SableMaidRagdoll.client.api.ITooltipAbleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.List;

public class CheatDeathBaubleItem extends Item implements ITooltipAbleItem {
    public CheatDeathBaubleItem() {
        super(new Properties().stacksTo(1).durability(10).rarity(Rarity.RARE));
    }

    @Override
    public void appendHelpText(List<Component> tooltips) {
        generateComponentList(tooltips, this, 1, true);
    }

    @Override
    public void appendBackgroundText(List<Component> tooltips) {
        generateComponentList(tooltips, this, 1, false);
    }
}

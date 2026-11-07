package com.gly091020.SableMaidRagdoll.item.tooltip;

import com.gly091020.SableMaidRagdoll.client.api.ITooltipAbleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class TooltipBlockItem extends BlockItem implements ITooltipAbleItem {
    private final int helpCount;
    private final int backgroundCount;
    public TooltipBlockItem(Block block, Properties properties, int helpCount, int backgroundCount) {
        super(block, properties);
        this.backgroundCount = backgroundCount;
        this.helpCount = helpCount;
    }

    @Override
    public void appendHelpText(List<Component> tooltips) {
        if(helpCount <= 0)return;
        generateComponentList(tooltips, this, helpCount, true);
    }

    @Override
    public void appendBackgroundText(List<Component> tooltips) {
        if(backgroundCount <= 0)return;
        generateComponentList(tooltips, this, backgroundCount, false);
    }
}

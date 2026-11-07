package com.gly091020.SableMaidRagdoll.block.giant_slime;

import com.gly091020.SableMaidRagdoll.client.api.ITooltipAbleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GiantSlimeBlockItem extends BlockItem implements ITooltipAbleItem {
    public GiantSlimeBlockItem(GiantSlimeBlock block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean mustSurvive() {
        return false;
    }

    @Override
    public @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos placementCenter = GiantSlimeBlock.findPlacementCenter(
                context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getPlayer());
        if (placementCenter == null) {
            return null;
        }
        return BlockPlaceContext.at(context, placementCenter, context.getClickedFace());
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

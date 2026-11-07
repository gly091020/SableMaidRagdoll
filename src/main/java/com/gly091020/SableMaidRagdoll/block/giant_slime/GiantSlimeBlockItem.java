package com.gly091020.SableMaidRagdoll.block.giant_slime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;

public class GiantSlimeBlockItem extends BlockItem {
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
}

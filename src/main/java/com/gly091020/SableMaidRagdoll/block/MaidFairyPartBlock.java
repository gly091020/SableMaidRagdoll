package com.gly091020.SableMaidRagdoll.block;

import com.gly091020.SableRagdollLib.block.AbstractPartBlock;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MaidFairyPartBlock extends AbstractPartBlock {
    public MaidFairyPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Function<Properties, AbstractPartBlock> createBlock() {
        return MaidFairyPartBlock::new;
    }

    @Override
    public @NotNull AbstractPartBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MaidFairyPartBlockEntity(blockPos, blockState);
    }
}

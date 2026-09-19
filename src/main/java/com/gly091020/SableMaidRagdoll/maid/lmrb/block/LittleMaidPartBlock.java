package com.gly091020.SableMaidRagdoll.maid.lmrb.block;

import com.gly091020.SableRagdollLib.block.AbstractPartBlock;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class LittleMaidPartBlock extends AbstractPartBlock {
    public static final Properties PROPERTIES = Properties.ofFullCopy(Blocks.WHITE_WOOL)
            .noLootTable()
            .sound(SoundType.WOOL)
            .destroyTime(0)
            .explosionResistance(3600000.0F)
            .isValidSpawn(Blocks::never)
            .dynamicShape();

    public LittleMaidPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Function<Properties, AbstractPartBlock> createBlock() {
        return LittleMaidPartBlock::new;
    }

    @Override
    public @NotNull AbstractPartBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new LittleMaidPartBlockEntity(blockPos, blockState);
    }
}

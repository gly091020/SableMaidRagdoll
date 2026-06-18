package com.gly091020.SableMaidRagdoll.block;

import com.gly091020.SableMaidRagdoll.util.MixinUseGlobalBE;
import com.mojang.serialization.MapCodec;
import dev.ryanhcode.sable.api.block.BlockSubLevelCollisionShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaidPartBlock extends BaseEntityBlock implements BlockSubLevelCollisionShape {
    public static final Properties PROPERTIES = Properties.ofFullCopy(Blocks.WHITE_WOOL).noLootTable().sound(SoundType.WOOL).dynamicShape();

    public MaidPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MaidPartBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new MaidPartBlockEntity(blockPos, blockState);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        if(blockGetter.getBlockEntity(blockPos) instanceof MaidPartBlockEntity blockEntity)
            return blockEntity.getShape();
        return Shapes.block();
    }

    @Override
    protected int getLightBlock(@NotNull BlockState p_60585_, @NotNull BlockGetter p_60586_, @NotNull BlockPos p_60587_) {
        return 0;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState p_60475_, @NotNull PathComputationType p_60478_) {
        return false;
    }

    @Override
    public VoxelShape getSubLevelCollisionShape(BlockGetter blockGetter, BlockState state) {
        if(MixinUseGlobalBE.blockEntity != null)
            return MixinUseGlobalBE.blockEntity.getShape();
        return Shapes.block();
    }
}

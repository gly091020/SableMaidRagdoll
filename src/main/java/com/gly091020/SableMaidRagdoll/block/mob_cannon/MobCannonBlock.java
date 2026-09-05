package com.gly091020.SableMaidRagdoll.block.mob_cannon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MobCannonBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.5, 1);
    public MobCannonBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MobCannonBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MobCannonBlockEntity(blockPos, blockState);
    }

    @Override
    protected int getLightBlock(BlockState p_60585_, BlockGetter p_60586_, BlockPos p_60587_) {
        return 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof MobCannonBlockEntity blockEntity) {
            player.openMenu(blockEntity);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MobCannonBlockEntity blockEntity) {
            for (int i = 0; i < blockEntity.getSlots(); i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blockEntity.getStackInSlot(i));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return ((level, blockPos, blockState, t) -> {
            if (t instanceof MobCannonBlockEntity blockEntity)
                MobCannonBlockEntity.tick(level, blockPos, blockState, blockEntity);
        });
    }

    @Override
    protected VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, BlockPos blockPos, boolean b) {
        super.neighborChanged(blockState, level, pos, block, blockPos, b);
        if(level.hasNeighborSignal(pos) &&
                level.getBlockEntity(pos) instanceof MobCannonBlockEntity blockEntity &&
                blockEntity.isPlayerMode()) {
            blockEntity.setCooldown(0);
            blockEntity.tryFire();
            blockEntity.setCooldown(0);
        }
    }
}

package com.gly091020.SableMaidRagdoll.block.maid_doll;

import com.github.tartaricacid.touhoulittlemaid.block.BlockGarageKit;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.mojang.serialization.MapCodec;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaidDollBlock extends HorizontalDirectionalBlock implements EntityBlock, BlockWithSubLevelCollisionCallback {
    public MaidDollBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_);
        p_49915_.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(MaidDollBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MaidDollBlockEntity(blockPos, blockState);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockentity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        var s = new ItemStack(SableMaidRagdoll.PLAYER_CHEAT_DEATH_ITEM.get());
        if(!(blockentity instanceof MaidDollBlockEntity blockEntity))return List.of(s);
        s.set(SableMaidRagdoll.MAID_MODEL, blockEntity.getModelID());
        s.set(SableMaidRagdoll.MAID_SOUND, blockEntity.getSoundID());
        return List.of(s);
    }

    @Override
    public BlockState playerWillDestroy(Level p_49852_, BlockPos p_49853_, BlockState p_49854_, Player p_49855_) {
        if(p_49855_.isCreative() && p_49852_.getBlockEntity(p_49853_) instanceof MaidDollBlockEntity blockEntity)
            dropResources(p_49854_, p_49852_, p_49853_, blockEntity);
        return super.playerWillDestroy(p_49852_, p_49853_, p_49854_, p_49855_);
    }

    @Override
    protected int getLightBlock(BlockState p_60585_, BlockGetter p_60586_, BlockPos p_60587_) {
        return 0;
    }

    @Override
    protected VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return BlockGarageKit.BLOCK_AABB;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide || !(level.getBlockEntity(pos) instanceof MaidDollBlockEntity blockEntity))return InteractionResult.SUCCESS_NO_ITEM_USED;
        blockEntity.triggerPat();
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        var be = level.getBlockEntity(pos);
        var r = super.getCloneItemStack(state, target, level, pos, player);
        if(!(be instanceof MaidDollBlockEntity blockEntity))return r;
        r.set(SableMaidRagdoll.MAID_SOUND, blockEntity.getSoundID());
        r.set(SableMaidRagdoll.MAID_MODEL, blockEntity.getModelID());
        r.set(SableMaidRagdoll.ENABLE_CONTROL, blockEntity.isControlMode());
        return r;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockSubLevelCollisionCallback sable$getCallback() {
        return MaidDollBlockSubLevelCollisionCallback.INSTANCE;
    }
}

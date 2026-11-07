package com.gly091020.SableMaidRagdoll.block.giant_slime;

import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.mojang.serialization.MapCodec;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class GiantSlimeBlock extends SlimeBlock
        implements EntityBlock, BlockWithSubLevelCollisionCallback, BlockSubLevelAssemblyListener {
    public static final int SIZE = 3;
    public static final int HORIZONTAL_RADIUS = SIZE / 2;
    private static final int PLACEMENT_SEARCH_RADIUS = 1;
    public static final EnumProperty<GiantSlimePart> PART =
            EnumProperty.create("part", GiantSlimePart.class);

    public GiantSlimeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, GiantSlimePart.PART));
    }

    @Override
    public MapCodec<SlimeBlock> codec() {
        return simpleCodec(GiantSlimeBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!canPlaceStructure(context.getLevel(), context.getClickedPos(), context.getPlayer())) {
            return null;
        }
        return defaultBlockState().setValue(PART, GiantSlimePart.CORE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GiantSlimeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof GiantSlimeBlockEntity slime) {
                GiantSlimeBlockEntity.tick(tickLevel, pos, tickState, slime);
            }
        };
    }

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState,
                          BlockPos oldPos, BlockPos newPos) {
        if (originLevel.getBlockEntity(oldPos) instanceof GiantSlimeBlockEntity oldEntity) {
            oldEntity.clearAdhesions();
        }
        if (resultingLevel.getBlockEntity(newPos) instanceof GiantSlimeBlockEntity newEntity) {
            newEntity.clearAdhesions();
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        for (BlockPos part : structurePositions(pos)) {
            if (!part.equals(pos)) {
                level.setBlock(part, state.setValue(PART, GiantSlimePart.PART), Block.UPDATE_ALL);
            }
            if (level.getBlockEntity(part) instanceof GiantSlimeBlockEntity partEntity) {
                partEntity.setMasterPos(pos);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()) {
            popResource(level, pos, new ItemStack(InitItems.GIANT_SLIME_BLOCK_ITEM.get()));
        }
        handleStructureRemove(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(InitItems.GIANT_SLIME_BLOCK_ITEM.get()));
        }
        handleStructureRemove(level, pos);
        super.onBlockExploded(state, level, pos, explosion);
    }

    public static @Nullable BlockPos findPlacementCenter(Level level, BlockPos start,
                                                         Direction placementDirection, @Nullable Player placer) {
        BlockPos preferredCenter = start.relative(placementDirection);
        BlockPos best = null;
        long bestScore = Long.MAX_VALUE;
        for (int dx = -PLACEMENT_SEARCH_RADIUS; dx <= PLACEMENT_SEARCH_RADIUS; dx++) {
            for (int dy = -PLACEMENT_SEARCH_RADIUS; dy <= PLACEMENT_SEARCH_RADIUS; dy++) {
                for (int dz = -PLACEMENT_SEARCH_RADIUS; dz <= PLACEMENT_SEARCH_RADIUS; dz++) {
                    BlockPos candidate = preferredCenter.offset(dx, dy, dz);
                    if (!canPlaceStructure(level, candidate, placer)) {
                        continue;
                    }
                    long score = (long) dx * dx + (long) dz * dz + 2L * dy * dy;
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    public static boolean canPlaceStructure(Level level, BlockPos centerPos, @Nullable Player placer) {
        for (BlockPos part : structurePositions(centerPos)) {
            if (level.isOutsideBuildHeight(part) || !level.getWorldBorder().isWithinBounds(part)) {
                return false;
            }
            if (!level.getBlockState(part).canBeReplaced()) {
                return false;
            }
        }
        if (!hasSupportingFace(level, centerPos)) {
            return false;
        }
        AABB structureBounds = new AABB(
                centerPos.getX() - HORIZONTAL_RADIUS,
                centerPos.getY() - HORIZONTAL_RADIUS,
                centerPos.getZ() - HORIZONTAL_RADIUS,
                centerPos.getX() + HORIZONTAL_RADIUS + 1,
                centerPos.getY() + HORIZONTAL_RADIUS + 1,
                centerPos.getZ() + HORIZONTAL_RADIUS + 1
        );
        if (placer != null && overlapsBlockGrid(placer, centerPos)) {
            return false;
        }
        for (Player player : level.getEntitiesOfClass(
                Player.class, structureBounds.inflate(0.01),
                player -> player.isAlive() && !player.isSpectator())) {
            if (overlapsBlockGrid(player, centerPos)) {
                return false;
            }
        }
        return true;
    }

    private static boolean overlapsBlockGrid(Player player, BlockPos centerPos) {
        AABB playerBounds = player.getBoundingBox().inflate(1.0E-4);
        for (BlockPos part : structurePositions(centerPos)) {
            if (playerBounds.intersects(new AABB(part))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSupportingFace(Level level, BlockPos centerPos) {
        for (Direction direction : Direction.values()) {
            if (hasFaceSupport(level, centerPos, direction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFaceSupport(Level level, BlockPos centerPos, Direction direction) {
        int minX = centerPos.getX() - HORIZONTAL_RADIUS;
        int minY = centerPos.getY() - HORIZONTAL_RADIUS;
        int minZ = centerPos.getZ() - HORIZONTAL_RADIUS;
        int maxX = centerPos.getX() + HORIZONTAL_RADIUS;
        int maxY = centerPos.getY() + HORIZONTAL_RADIUS;
        int maxZ = centerPos.getZ() + HORIZONTAL_RADIUS;
        int supportX = direction == Direction.WEST ? minX - 1
                : direction == Direction.EAST ? maxX + 1 : -1;
        int supportY = direction == Direction.DOWN ? minY - 1
                : direction == Direction.UP ? maxY + 1 : -1;
        int supportZ = direction == Direction.NORTH ? minZ - 1
                : direction == Direction.SOUTH ? maxZ + 1 : -1;

        for (int a = 0; a < SIZE; a++) {
            for (int b = 0; b < SIZE; b++) {
                BlockPos supportPos = switch (direction.getAxis()) {
                    case X -> new BlockPos(supportX, minY + a, minZ + b);
                    case Y -> new BlockPos(minX + a, supportY, minZ + b);
                    case Z -> new BlockPos(minX + a, minY + b, supportZ);
                };
                if (level.getBlockState(supportPos).isFaceSturdy(
                        level, supportPos, direction.getOpposite())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Iterable<BlockPos> structurePositions(BlockPos centerPos) {
        return BlockPos.betweenClosed(
                centerPos.offset(-HORIZONTAL_RADIUS, -HORIZONTAL_RADIUS, -HORIZONTAL_RADIUS),
                centerPos.offset(HORIZONTAL_RADIUS, HORIZONTAL_RADIUS, HORIZONTAL_RADIUS)
        );
    }

    public static @Nullable BlockPos findMasterPos(Level level, BlockPos partPos) {
        for (BlockPos pos : BlockPos.betweenClosed(
                partPos.offset(-HORIZONTAL_RADIUS, -HORIZONTAL_RADIUS, -HORIZONTAL_RADIUS),
                partPos.offset(HORIZONTAL_RADIUS, HORIZONTAL_RADIUS, HORIZONTAL_RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof GiantSlimeBlock
                    && state.getValue(PART) == GiantSlimePart.CORE) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static void handleStructureRemove(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        BlockPos masterPos = findMasterPos(level, pos);
        if (masterPos == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        for (BlockPos part : structurePositions(masterPos)) {
            if (level.getBlockState(part).is(state.getBlock())) {
                level.setBlockAndUpdate(part, Blocks.AIR.defaultBlockState());
            }
        }
    }

    @Override
    public boolean isSlimeBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        return state.isSlimeBlock() && other.isSlimeBlock();
    }

    @Override
    public BlockSubLevelCollisionCallback sable$getCallback() {
        return GiantSlimeBlockSubLevelCollisionCallback.INSTANCE;
    }
}

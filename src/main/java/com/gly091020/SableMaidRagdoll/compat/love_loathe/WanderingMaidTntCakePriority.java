package com.gly091020.SableMaidRagdoll.compat.love_loathe;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.TNTCakeEdible;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

public final class WanderingMaidTntCakePriority {
    private static final int SEARCH_RADIUS = 5;
    private static final int VERTICAL_SEARCH_RADIUS = 2;
    private static final int EAT_COOLDOWN_TICKS = 45 * 20;
    private static final int ATTEMPT_TIMEOUT_TICKS = 10 * 20;
    private static final double EAT_DISTANCE_SQR = 4.0;
    private static final float WALK_SPEED = 0.6F;
    private static final TNTCakeEdible TNT_CAKE_EDIBLE = new TNTCakeEdible();
    private static final Map<EntityMaid, Attempt> ATTEMPTS = new WeakHashMap<>();
    private static final Map<EntityMaid, Long> COOLDOWNS = new WeakHashMap<>();

    private WanderingMaidTntCakePriority() {
    }

    public static boolean tick(EntityMaid maid, long gameTime) {
        if (!(maid.level() instanceof ServerLevel level) || !maid.isAlive()) {
            return false;
        }

        long cooldownUntil = COOLDOWNS.getOrDefault(maid, 0L);
        if (gameTime < cooldownUntil) {
            return false;
        }

        Attempt attempt = ATTEMPTS.get(maid);
        if (attempt != null) {
            if (!isTntCake(level, attempt.target())) {
                ATTEMPTS.remove(maid);
                return false;
            }
            if (gameTime - attempt.startedAt() > ATTEMPT_TIMEOUT_TICKS) {
                ATTEMPTS.remove(maid);
                COOLDOWNS.put(maid, gameTime + 5 * 20);
                return false;
            }
        } else {
            BlockPos target = findNearestTntCake(level, maid.blockPosition());
            if (target == null) {
                return false;
            }
            attempt = new Attempt(target, gameTime);
            ATTEMPTS.put(maid, attempt);
        }

        BlockPos target = attempt.target();
        if (maid.distanceToSqr(Vec3.atCenterOf(target)) <= EAT_DISTANCE_SQR) {
            BlockState state = level.getBlockState(target);
            if (!state.is(InitBlocks.TNT_CAKE_BLOCK)) {
                ATTEMPTS.remove(maid);
                return false;
            }
            ATTEMPTS.remove(maid);
            if (TNT_CAKE_EDIBLE.consume(maid, target, state)) {
                maid.swing(InteractionHand.MAIN_HAND);
                COOLDOWNS.put(maid, gameTime + EAT_COOLDOWN_TICKS);
            }
            return true;
        }

        maid.setInSittingPose(false);
        maid.setBegging(false);
        maid.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(target));
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(new BlockPosTracker(target), WALK_SPEED, 2));
        maid.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, WALK_SPEED);
        return true;
    }

    private static BlockPos findNearestTntCake(ServerLevel level, BlockPos center) {
        BlockPos min = center.offset(-SEARCH_RADIUS, -VERTICAL_SEARCH_RADIUS, -SEARCH_RADIUS);
        BlockPos max = center.offset(SEARCH_RADIUS, VERTICAL_SEARCH_RADIUS, SEARCH_RADIUS);
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.isLoaded(pos) || !isTntCake(level, pos)) {
                continue;
            }
            double distance = center.distSqr(pos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = pos.immutable();
            }
        }
        return nearest;
    }

    private static boolean isTntCake(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(InitBlocks.TNT_CAKE_BLOCK);
    }

    private record Attempt(BlockPos target, long startedAt) {
    }
}

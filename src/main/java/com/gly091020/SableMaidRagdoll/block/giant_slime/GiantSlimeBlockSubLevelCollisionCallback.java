package com.gly091020.SableMaidRagdoll.block.giant_slime;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class GiantSlimeBlockSubLevelCollisionCallback implements BlockSubLevelCollisionCallback {
    public static final GiantSlimeBlockSubLevelCollisionCallback INSTANCE = new GiantSlimeBlockSubLevelCollisionCallback();

    private GiantSlimeBlockSubLevelCollisionCallback() {
    }

    @Override
    public CollisionResult sable$onCollision(BlockPos hitBlockPos, @Nullable BlockPos otherHitBlockPos,
                                             Vector3d impactPosition, double impactVelocity) {
        if (otherHitBlockPos == null) {
            return CollisionResult.NONE;
        }
        ServerLevel level = SubLevelPhysicsSystem.getCurrentlySteppingSystem().getLevel();
        if (!(Sable.HELPER.getContaining(level, hitBlockPos) instanceof ServerSubLevel thisSubLevel)
                || !(Sable.HELPER.getContaining(level, otherHitBlockPos) instanceof ServerSubLevel otherSubLevel)
                || thisSubLevel == otherSubLevel) {
            return CollisionResult.NONE;
        }
        BlockPos masterPos = GiantSlimeBlock.findMasterPos(level, hitBlockPos);
        if (masterPos == null || !(level.getBlockEntity(masterPos) instanceof GiantSlimeBlockEntity master)) {
            return CollisionResult.NONE;
        }

        Vector3d worldImpact = thisSubLevel.logicalPose().transformPosition(impactPosition, new Vector3d());
        Vector3d otherImpact = otherSubLevel.logicalPose().transformPositionInverse(worldImpact, new Vector3d());
        Direction thisFace = nearestFace(impactPosition, hitBlockPos);
        Direction otherFace = nearestFace(otherImpact, otherHitBlockPos);
        Vector3d thisNormalWorld = thisSubLevel.logicalPose().transformNormal(
                new Vector3d(thisFace.getStepX(), thisFace.getStepY(), thisFace.getStepZ()), new Vector3d());
        Vector3d otherNormalWorld = otherSubLevel.logicalPose().transformNormal(
                new Vector3d(otherFace.getStepX(), otherFace.getStepY(), otherFace.getStepZ()), new Vector3d());
        double alignment = Math.max(-1.0, Math.min(1.0, -thisNormalWorld.dot(otherNormalWorld)));
        double angle = Math.acos(alignment);

        master.queueCollision(otherSubLevel, impactPosition, otherImpact,
                new Vector3d(thisFace.getStepX(), thisFace.getStepY(), thisFace.getStepZ()),
                new Vector3d(otherFace.getStepX(), otherFace.getStepY(), otherFace.getStepZ()),
                angle <= Math.toRadians(30.0));
        return CollisionResult.NONE;
    }

    private static Direction nearestFace(Vector3d impactPosition, BlockPos blockPos) {
        double x = impactPosition.x - blockPos.getX();
        double y = impactPosition.y - blockPos.getY();
        double z = impactPosition.z - blockPos.getZ();
        double[] distances = {x, 1.0 - x, y, 1.0 - y, z, 1.0 - z};
        Direction[] faces = {Direction.WEST, Direction.EAST, Direction.DOWN,
                Direction.UP, Direction.NORTH, Direction.SOUTH};
        int nearest = 0;
        for (int i = 1; i < distances.length; i++) {
            if (distances[i] < distances[nearest]) {
                nearest = i;
            }
        }
        return faces[nearest];
    }

}

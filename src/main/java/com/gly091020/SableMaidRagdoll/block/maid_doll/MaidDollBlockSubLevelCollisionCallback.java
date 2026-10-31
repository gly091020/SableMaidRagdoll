package com.gly091020.SableMaidRagdoll.block.maid_doll;

import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class MaidDollBlockSubLevelCollisionCallback implements BlockSubLevelCollisionCallback {
    public static final MaidDollBlockSubLevelCollisionCallback INSTANCE = new MaidDollBlockSubLevelCollisionCallback();
    @Override
    public CollisionResult sable$onCollision(BlockPos hitBlockPos, @Nullable BlockPos otherHitBlockPos, Vector3d impactPosition, double impactVelocity) {
        var level = SubLevelPhysicsSystem.getCurrentlySteppingSystem().getLevel();
        if(!(level.getBlockEntity(hitBlockPos) instanceof MaidDollBlockEntity blockEntity))return CollisionResult.NONE;
        if(impactVelocity * impactVelocity > 16){
            blockEntity.triggerHurt();
        }
        return CollisionResult.NONE;
    }
}

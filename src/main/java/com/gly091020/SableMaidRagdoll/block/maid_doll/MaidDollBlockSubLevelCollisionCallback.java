package com.gly091020.SableMaidRagdoll.block.maid_doll;

import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.maid.api.MaidSoundType;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
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
            var pos = SableCompanion.INSTANCE.projectOutOfSubLevel(level, impactPosition);
            MaidRagdollTypesManager.playSound(blockEntity.getRagdollTypeID(), level, JOMLConversion.toMojang(pos), blockEntity.getSoundID(), MaidSoundType.HURT, 0.5f);
        }
        return CollisionResult.NONE;
    }
}

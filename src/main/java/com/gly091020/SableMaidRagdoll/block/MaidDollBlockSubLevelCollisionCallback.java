package com.gly091020.SableMaidRagdoll.block;

import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.network.message.PlayMaidSoundAtPosPackage;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;
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
            PacketDistributor.sendToAllPlayers(new PlayMaidSoundAtPosPackage(
                    InitSounds.MAID_HURT.getId(), blockEntity.getSoundID(),
                    pos.x, pos.y, pos.z, 0.5f, 1
            ));
        }
        return CollisionResult.NONE;
    }
}

package com.gly091020.SableMaidRagdoll.mixin;

import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.util.MaidPartColliderBoxManager;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.physics.impl.rapier.RapierPhysicsPipeline;
import dev.ryanhcode.sable.physics.impl.rapier.collider.RapierVoxelColliderBakery;
import dev.ryanhcode.sable.physics.impl.rapier.collider.RapierVoxelColliderData;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RapierPhysicsPipeline.class)
// 纯迷信
// BlockSubLevelDynamicCollider 根本没实现😮
public class GetPhysicsDataBEMixin {
    @Shadow
    @Final
    private LevelAccelerator accelerator;

    @Redirect(method = "handleChunkSectionAddition", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;"))
    public RapierVoxelColliderData getBE(RapierVoxelColliderBakery instance, BlockState state, @Local(name = "globalPos") BlockPos globalPos){
        return sableMaidRagdoll$getRapierVoxelColliderData(instance, state, globalPos);
    }

    @Unique
    private @Nullable RapierVoxelColliderData sableMaidRagdoll$getRapierVoxelColliderData(RapierVoxelColliderBakery instance, BlockState state, BlockPos globalPos) {
        var be = accelerator.getBlockEntity(globalPos);
        var p = instance.getPhysicsDataForBlock(state);
        if(be instanceof MaidPartBlockEntity blockEntity) {
            if(p == null) return null;
            if(blockEntity.getRenderData() == null) return null;
            var r = MaidPartColliderBoxManager.getColliderData(blockEntity.getRenderData(), blockEntity.getMaidBlockShape());
            if(r.equals(RapierVoxelColliderData.EMPTY)) return p;
            return r;
        }
        return p;
    }

    @Redirect(method = "handleBlockChange", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;", ordinal = 0))
    public RapierVoxelColliderData getBE1(RapierVoxelColliderBakery instance, BlockState state, @Local(name = "pos") BlockPos pos){
        return sableMaidRagdoll$getRapierVoxelColliderData(instance, state, pos);
    }

    @Redirect(method = "handleBlockChange", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;", ordinal = 1))
    public RapierVoxelColliderData getBE2(RapierVoxelColliderBakery instance, BlockState state, @Local(name = "globalBlockPos") BlockPos pos){
        return sableMaidRagdoll$getRapierVoxelColliderData(instance, state, pos);
    }
}

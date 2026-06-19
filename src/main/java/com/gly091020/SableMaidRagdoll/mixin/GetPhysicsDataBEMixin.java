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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        var be = accelerator.getBlockEntity(globalPos);
        var p = instance.getPhysicsDataForBlock(state);
        if(be instanceof MaidPartBlockEntity blockEntity) {
            if(p == null)return null;
            var r = MaidPartColliderBoxManager.getColliderData(blockEntity.getMaidBlockShape());
            if(r.equals(RapierVoxelColliderData.EMPTY))return p;
            return r;
        }
        return p;
    }

    @Redirect(method = "handleBlockChange", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;", ordinal = 0))
    public RapierVoxelColliderData getBE1(RapierVoxelColliderBakery instance, BlockState state, @Local(name = "pos") BlockPos pos){
        var be = accelerator.getBlockEntity(pos);
        var p = instance.getPhysicsDataForBlock(state);
        if(be instanceof MaidPartBlockEntity blockEntity) {
            if(p == null)return null;
            var r = MaidPartColliderBoxManager.getColliderData(blockEntity.getMaidBlockShape());
            if(r.equals(RapierVoxelColliderData.EMPTY))return p;
            return r;
        }
        return p;
    }

    @Redirect(method = "handleBlockChange", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;", ordinal = 1))
    public RapierVoxelColliderData getBE2(RapierVoxelColliderBakery instance, BlockState state, @Local(name = "globalBlockPos") BlockPos pos){
        var be = accelerator.getBlockEntity(pos);
        var p = instance.getPhysicsDataForBlock(state);
        if(be instanceof MaidPartBlockEntity blockEntity) {
            if(p == null)return null;
            var r = MaidPartColliderBoxManager.getColliderData(blockEntity.getMaidBlockShape());
            if(r.equals(RapierVoxelColliderData.EMPTY))return p;
            return r;
        }
        return p;
    }
}

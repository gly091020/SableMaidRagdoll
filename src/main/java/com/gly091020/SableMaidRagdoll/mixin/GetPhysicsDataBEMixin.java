package com.gly091020.SableMaidRagdoll.mixin;

import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.util.MixinUseGlobalBE;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.RapierPhysicsPipeline")
// 纯迷信
public class GetPhysicsDataBEMixin {
    @Shadow
    @Final
    private LevelAccelerator accelerator;

    @Inject(method = "handleChunkSectionAddition", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;"))
    public void getBE(LevelChunkSection section, int x, int y, int z, boolean uploadDataIfGlobal, CallbackInfo ci, @Local(name = "globalPos") BlockPos globalPos){
        var be = accelerator.getBlockEntity(globalPos);
        if(be instanceof MaidPartBlockEntity blockEntity)
            MixinUseGlobalBE.blockEntity = blockEntity;
    }

    @Inject(method = "handleChunkSectionAddition", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderBakery;getPhysicsDataForBlock(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/physics/impl/rapier/collider/RapierVoxelColliderData;", shift = At.Shift.AFTER))
    public void clearBE(LevelChunkSection section, int x, int y, int z, boolean uploadDataIfGlobal, CallbackInfo ci){
        MixinUseGlobalBE.blockEntity = null;
    }
}

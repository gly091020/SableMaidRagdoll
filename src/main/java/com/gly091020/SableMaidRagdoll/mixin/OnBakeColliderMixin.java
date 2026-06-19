package com.gly091020.SableMaidRagdoll.mixin;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidPartColliderBoxManager;
import dev.ryanhcode.sable.physics.impl.rapier.collider.RapierVoxelColliderBakery;
import dev.ryanhcode.sable.physics.impl.rapier.collider.RapierVoxelColliderData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RapierVoxelColliderBakery.class)
public class OnBakeColliderMixin {
    @Inject(method = "buildPhysicsDataForBlock", at = @At("RETURN"))
    public void onBake(BlockState childState, CallbackInfoReturnable<RapierVoxelColliderData> cir){
        if(childState.is(SableMaidRagdoll.MAID_PART_BLOCK.get()))
            MaidPartColliderBoxManager.init();
    }
}

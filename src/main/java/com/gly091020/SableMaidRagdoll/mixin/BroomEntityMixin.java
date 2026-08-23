package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;

@Mixin(EntityBroom.class)
public class BroomEntityMixin {
    @WrapMethod(method = "canMaidRide")
    public boolean noRagdoll(EntityMaid maid, Operation<Boolean> original){
        if(maid.getVehicle() instanceof PartSeat)return false;
        return original.call(maid);
    }
}

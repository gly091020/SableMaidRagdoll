package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.AbstractBedrockEntityModel;
import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.mixin.BedrockModelAccessor;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MixinFunction {
    public static @Nullable Ragdoll getRagdoll(EntityBox self, EntityMaid maid) {
        var id = ResourceLocation.fromNamespaceAndPath(
                SableMaidRagdoll.MODID,
                maid.getModelId().replace(":", "/")
        );

        var reg = RagdollHelper.createRagdoll(
                (ServerLevel) maid.level(),
                maid.position().add(0, 0.5, 0),
                id
        );

        if (reg == null) return null;

        self.ejectPassengers();
        reg.addEntity(maid);

        ScheduleManager.scheduleDelayed((ServerLevel) maid.level(), 2, () -> {
            reg.addLinearImpulse(new Vec3(0, 10, 0), false);

            var level = (ServerLevel) maid.level();
            var nearestPlayer = level.getNearestPlayer(maid, 10);

            if (nearestPlayer != null) {
                Vec3 dir = nearestPlayer.position().subtract(maid.position());
                Vec3 horizontal = new Vec3(dir.x, 0, dir.z);

                if (horizontal.lengthSqr() > 1e-4) {
                    horizontal = horizontal.normalize();

                    double pushStrength = 2;
                    reg.addLinearImpulse(horizontal.scale(pushStrength), false);

                    Vec3 axis = horizontal.cross(new Vec3(0, 1, 0)).normalize();
                    double spinStrength = -6;
                    reg.addAngularImpulse(axis.scale(spinStrength), false);
                }
            }
        });

        return reg;
    }

    public static List<BedrockPart> getShouldRender(AbstractBedrockEntityModel<?> model){
        return ((BedrockModelAccessor)model).getShouldRender();
    }
}

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
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

import static com.gly091020.SableRagdollLib.api.ScheduleManager.scheduleDelayed;

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
            reg.addLinearImpulse(new Vec3(0, 4, 0), false);

            var level = (ServerLevel) maid.level();
            var nearestPlayer = level.getNearestPlayer(maid, 10);

            if (nearestPlayer != null) {
                Vec3 dir = nearestPlayer.position().subtract(maid.position());
                Vec3 horizontal = new Vec3(dir.x, 0, dir.z);

                if (horizontal.lengthSqr() > 1e-4) {
                    horizontal = horizontal.normalize();

                    double pushStrength = 2;
                    reg.addLinearImpulse(horizontal.scale(pushStrength), false);

                    Vec3 axis = horizontal.cross(new Vec3(0, 0.5, 0)).normalize();
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

    public static void createRagdoll(ServerLevel level, EntityMaid maid, Vector3d maidMotion, boolean addMaid){
        Vector3d forward = JOMLConversion.toJOML(maid.getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,1,0));
        createRagdoll(level, maid, maidMotion, axis, addMaid);
    }

    public static void createRagdoll(ServerLevel level, EntityMaid maid, Vector3d maidMotion, Vector3d rotation, boolean addMaid){
        rotation.add(0, 0, 10);
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        var parts = RagdollHelper.createRagdoll(level, maid.position().add(0, 0.5, 0), new Vec3(-90, -maid.getYHeadRot(), 0),
                id);
        if(parts == null)return;
        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(level, 2, () -> {
            parts.addAngularImpulse(rotation, true);
            parts.addLinearImpulse(maidMotion, true);
        });
        if(addMaid)
            parts.addEntity(maid);
    }
}

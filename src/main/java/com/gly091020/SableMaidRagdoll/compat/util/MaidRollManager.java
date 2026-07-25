package com.gly091020.SableMaidRagdoll.compat.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import com.gly091020.SableRagdollLib.common.ServerGetter;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class MaidRollManager {
    private static final List<RollingMaid> ROLLING = new ArrayList<>();

    public static void tick(){
        var r = new ArrayList<RollingMaid>();
        ROLLING.forEach(ragdoll -> {
            if(!ragdoll.ragdoll.isAlive()){
                r.add(ragdoll);
                return;
            }
            handleRagdoll(ragdoll);
        });
        ROLLING.removeAll(r);
    }

    private static void handleRagdoll(RollingMaid rollingMaid){
        int period = 40;
        long phase = (long) (System.currentTimeMillis() / 1000.0 * 20 % (period * 2)); // 0 ~ 79
        double impulseStrength;

        if (phase < period) {
            impulseStrength = 0.5;
        } else {
            impulseStrength = -0.5;
        }

        rollingMaid.ragdoll.addAngularImpulse(new Vector3d(0, impulseStrength, 0), false);
    }

    public static void startRolling(EntityMaid maid){
        if(maid.level().isClientSide)return;
        var level = (ServerLevel) maid.level();
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        var ragdoll = RagdollHelper.createRagdoll(level, maid.position().add(0, 0.5, 0), new Vec3(-90, -maid.getYHeadRot(), 0), id);
        if(ragdoll == null)return;
        var container = ServerSubLevelContainer.getContainer(level);
        if(container == null)return;
        var subLevel = container.getSubLevel(ragdoll.getCenter());
        if(subLevel == null)return;
        ragdoll.addEntity(maid);
        ROLLING.add(new RollingMaid(subLevel.logicalPose().position(), subLevel.logicalPose().orientation(), ragdoll));
    }

    public static boolean canRoll(EntityMaid maid){
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        return DefFileLoader.getDefFile(id) != null;
    }

    public record RollingMaid(Vector3d origin, Quaterniond angle, Ragdoll ragdoll){}
}

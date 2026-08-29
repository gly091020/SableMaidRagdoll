package com.gly091020.SableMaidRagdoll.compat.util;

import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.control.PartRole;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 受伤跳舞：让布娃娃四肢乱挥舞。
 * <p>
 * 结构与 {@link MaidRollManager} 一致：静态列表 + tick 驱动。
 * 入口传入已创建的 {@link Ragdoll}，之后每 tick 给左右臂/左右腿施加
 * 方向不断变化的角冲量——每个肢体独立频率/相位/振幅，视觉上像痉挛一样乱甩。
 */
public class WineFoxHurtDancingManager {
    private static final PartRole[] LIMBS = {
            PartRole.LEFT_ARM, PartRole.RIGHT_ARM, PartRole.LEFT_LEG, PartRole.RIGHT_LEG
    };
    private static final List<DancingRagdoll> DANCING = new ArrayList<>();

    private WineFoxHurtDancingManager() {
    }

    public static void tick() {
        var remove = new ArrayList<DancingRagdoll>();
        DANCING.forEach(entry -> {
            if (!entry.ragdoll().isAlive()) {
                remove.add(entry);
                return;
            }
            handleDancing(entry);
        });
        DANCING.removeAll(remove);
    }

    /** 开始乱舞：传入已创建的布娃娃（服务端）。重复传入同一布娃娃会重置。 */
    public static void startDancing(Ragdoll ragdoll) {
        if (ragdoll == null || !ragdoll.isAlive()) {
            return;
        }
        DANCING.removeIf(entry -> entry.ragdoll() == ragdoll);
        DANCING.add(new DancingRagdoll(ragdoll, System.currentTimeMillis()));
    }

    private static void handleDancing(DancingRagdoll entry) {
        Ragdoll ragdoll = entry.ragdoll();
        var sublevels = ragdoll.getSublevels();
        if (sublevels.isEmpty() || !(sublevels.getFirst().getLevel() instanceof ServerLevel level)) {
            return;
        }
        var container = ServerSubLevelContainer.getContainer(level);
        if (container == null) {
            return;
        }
        var roles = ragdoll.getPartRoles();
        if (roles == null || roles.isEmpty()) {
            return;
        }
        double t = (System.currentTimeMillis() - entry.seed()) / 1000.0;
        for (int i = 0; i < LIMBS.length; i++) {
            PartRole role = LIMBS[i];
            UUID uuid = roles.get(role);
            if (uuid == null) {
                continue;
            }
            var sub = container.getSubLevel(uuid);
            if (!(sub instanceof ServerSubLevel serverSub) || serverSub.isRemoved()) {
                continue;
            }
            // 每个肢体独立频率/相位/振幅（由 seed 派生，稳定但互不相同），
            // 叠加两个正弦让运动不规则；X/Z 甩动、Y 少量扭转
            double freq = 1.5 + i * 0.37 + (entry.seed() & 0x3) * 0.13;
            double phase = i * 2.1 + ((entry.seed() >>> 8) & 0x3) * 0.7;
            double amp = 0.35 + ((entry.seed() >>> 16) & 0x3) * 0.5;
            double ax = Math.sin(t * freq * Math.PI * 2 + phase) * amp;
            double ay = Math.sin(t * freq * 1.7 + phase * 2.0) * amp * 0.5;
            double az = Math.cos(t * freq * 2.7 + phase * 1.3) * amp;
            try {
                container.physicsSystem().getPhysicsHandle(serverSub)
                        .applyAngularImpulse(new Vector3d(ax, ay, az));
            } catch (Exception ignored) {
            }
        }
    }

    public record DancingRagdoll(Ragdoll ragdoll, long seed) {
    }
}

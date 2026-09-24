package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableMaidRagdoll.item.RagdollWandItem;
import com.gly091020.SableMaidRagdoll.mixin.EntitySharedFlagAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

/**
 * 蓄力中的布娃娃魔杖把准星上的有效目标点亮（原版发光描边）。
 * 只改客户端本地实体，所以只有自己看得到，也不会在服务端留下发光状态。
 */
@OnlyIn(Dist.CLIENT)
public final class RagdollWandTargetGlow {
    /** 原版发光用的共享标记位。 */
    private static final int GLOWING_FLAG = 6;
    /** 被我们点亮的目标。 */
    private static final Set<LivingEntity> GLOWING = new HashSet<>();

    private RagdollWandTargetGlow() {
    }

    public static void tick() {
        var player = Minecraft.getInstance().player;
        Set<LivingEntity> desired = new HashSet<>();
        if (player != null && player.isUsingItem() && player.getUseItem().is(InitItems.RAGDOLL_WAND_ITEM.get())) {
            var target = RagdollWandItem.findTarget(player);
            if (target != null) desired.add(target);
        }

        GLOWING.removeIf(entity -> {
            if (desired.contains(entity)) return false;
            setGlowing(entity, false);
            return true;
        });

        for (LivingEntity entity : desired) {
            // 本来就在发光（比如发光效果）的目标不动它
            if (entity.isCurrentlyGlowing()) continue;
            setGlowing(entity, true);
            GLOWING.add(entity);
        }
    }

    private static void setGlowing(LivingEntity entity, boolean glowing) {
        if (entity.isRemoved()) return;
        ((EntitySharedFlagAccessor) entity).smr$setSharedFlag(GLOWING_FLAG, glowing);
    }
}

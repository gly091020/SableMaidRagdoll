package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class RagdollEmoji {
    /**
     * 判断玩家当前是否正乘坐指定类型的布娃娃。
     * 客户端与服务端都可用（通过 sable 子世界容器按 PartSeat 的主子世界 UUID 反查）。
     */
    public static boolean isRagdollOfType(Player player, ResourceLocation type) {
        if (type == null || !(player.getVehicle() instanceof PartSeat seat)) return false;
        var mainUUID = seat.getMainUUID();
        if (mainUUID == null) return false;
        var container = SubLevelContainer.getContainer(player.level());
        if (container == null) return false;
        var subLevel = container.getSubLevel(mainUUID);
        if (subLevel == null) return false;
        var plot = subLevel.getPlot();

        if (plot.getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO) instanceof AbstractPartBlockEntity part) {
            return part.getPartData() != null && type.equals(part.getPartData().type());
        }
        return false;
    }

    /** 是否设置了有效表情（非空字符串）。 */
    public static boolean hasEmoji(Player player) {
        return !getEmoji(player).equals(SableMaidRagdoll.EMPTY_EMOJI);
    }

    /**
     * 获取当前表情的 ResourceLocation 字符串。
     * 未设置或已清除都返回空字符串；数据挂在玩家附件上，会自动同步到客户端。
     */
    public static ResourceLocation getEmoji(Player player) {
        return player.getData(SableMaidRagdoll.EMOJI_ATTACHMENT.get());
    }

    /**
     * 设置当前表情。{@code location} 传 null 或空字符串表示清除。
     */
    public static void setEmoji(Player player, @Nullable ResourceLocation location) {
        player.setData(SableMaidRagdoll.EMOJI_ATTACHMENT.get(), location == null ? SableMaidRagdoll.EMPTY_EMOJI : location);
    }
}

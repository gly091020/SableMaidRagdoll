package com.gly091020.SableMaidRagdoll.maid.lmrb.client;

import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.maid.lmrb.LMRBMaidRagdoll;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.resource.holder.TextureHolder;
import net.sistr.littlemaidmodelloader.resource.util.TextureColors;
import net.sistr.littlemaidrebirth.entity.LittleMaidEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 玩偶渲染用的女仆实体缓存。
 * <p>
 * {@code getRenderEntity} 会在方块实体/物品渲染里逐帧调用，这里全程只保留一个实例：
 * 层级变化才重建实体，数据没变就不重复套贴图。
 * <p>
 * {@link MaidDollData} 里 {@code modelID} 用 {@code 模型名|贴图包名|颜色索引|契约} 编码，
 * 解析见 {@link LMRBMaidRagdoll#getModelString(LittleMaidEntity)}。
 */
@OnlyIn(Dist.CLIENT)
public final class LMRBMaidRenderCache {
    private static @Nullable LittleMaidEntity cachedEntity;
    private static @Nullable MaidDollData appliedData;

    private LMRBMaidRenderCache() {
    }

    public static @Nullable Entity get(Level level, MaidDollData data) {
        if (level == null || data.modelID().isEmpty())
            return null;
        if (cachedEntity == null || cachedEntity.level() != level) {
            try {
                cachedEntity = new LittleMaidEntity(level);
            } catch (RuntimeException e) {
                cachedEntity = null;
                return null;
            }
            appliedData = null;
        }
        if (!data.equals(appliedData)) {
            apply(cachedEntity, data);
            appliedData = data;
        }
        return cachedEntity;
    }

    private static void apply(LittleMaidEntity maid, MaidDollData data) {
        TextureHolder holder = LMRBMaidRagdoll.getTextureFromData(data);
        boolean contract = LMRBMaidRagdoll.isContract(data);
        maid.setContractMM(contract);
        maid.setColorMM(pickColor(holder, contract, LMRBMaidRagdoll.getColorIndex(data)));
        maid.setTextureHolder(holder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD);
        // 与 TLM 的玩偶一样摆出待机坐姿
        maid.setOrderedToSit(true);
    }

    /** 优先用玩偶记录的颜色，该贴图包没有这个色再退回棕色/第一个可用色 */
    private static TextureColors pickColor(TextureHolder holder, boolean contract, int preferred) {
        TextureColors color = resolveColor(preferred);
        if (holder.getTexture(color, contract, false).isPresent())
            return color;
        if (holder.getTexture(TextureColors.BROWN, contract, false).isPresent())
            return TextureColors.BROWN;
        for (TextureColors candidate : TextureColors.values()) {
            if (holder.getTexture(candidate, contract, false).isPresent())
                return candidate;
        }
        return TextureColors.BROWN;
    }

    private static TextureColors resolveColor(int index) {
        try {
            return TextureColors.getColor(index);
        } catch (IllegalArgumentException e) {
            return TextureColors.BROWN;
        }
    }
}

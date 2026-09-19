package com.gly091020.SableMaidRagdoll.maid.lmrb.block;

import com.gly091020.SableMaidRagdoll.maid.lmrb.init.LMRBInitBlockEntities;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * LMML 女仆布娃娃的部位方块实体。
 * <p>
 * 除了布娃娃通用数据外，额外保存该女仆使用的贴图信息（贴图包名 + 颜色 + 契约状态），
 * 由创建布娃娃时写入，客户端渲染时据此解析实际贴图。
 */
public class LittleMaidPartBlockEntity extends AbstractPartBlockEntity {
    private static final String TEXTURE_NAME = "texture_name";
    private static final String TEXTURE_COLOR = "texture_color";
    private static final String TEXTURE_CONTRACT = "texture_contract";

    private String textureName = "Default";
    private int textureColor = 12;
    private boolean contract = true;

    public LittleMaidPartBlockEntity(BlockPos pos, BlockState state) {
        super(LMRBInitBlockEntities.LITTLE_MAID_PART_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString(TEXTURE_NAME, textureName);
        tag.putInt(TEXTURE_COLOR, textureColor);
        tag.putBoolean(TEXTURE_CONTRACT, contract);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TEXTURE_NAME, Tag.TAG_STRING))
            textureName = tag.getString(TEXTURE_NAME);
        if (tag.contains(TEXTURE_COLOR, Tag.TAG_INT))
            textureColor = tag.getInt(TEXTURE_COLOR);
        if (tag.contains(TEXTURE_CONTRACT, Tag.TAG_BYTE))
            contract = tag.getBoolean(TEXTURE_CONTRACT);
    }

    public void setMaidTexture(String textureName, int color, boolean contract) {
        this.textureName = textureName;
        this.textureColor = color;
        this.contract = contract;
        setChanged();
    }

    public String getTextureName() {
        return textureName;
    }

    public int getTextureColor() {
        return textureColor;
    }

    public boolean isContract() {
        return contract;
    }
}

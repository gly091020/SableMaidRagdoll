package com.gly091020.SableMaidRagdoll.block.parts;

import com.github.tartaricacid.touhoulittlemaid.entity.monster.FairyType;
import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public class MaidFairyPartBlockEntity extends AbstractPartBlockEntity {
    private FairyType type = FairyType.CYAN;
    private ModelType modelType = ModelType.NEW;
    private boolean rick = false;
    public MaidFairyPartBlockEntity(BlockPos pos, BlockState state) {
        super(InitBlockEntities.MAID_FAIRY_PART_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("fairy_type", type.name());
        tag.putString("model_type", modelType.name());
        tag.putBoolean("rick", rick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if(tag.contains("fairy_type", Tag.TAG_STRING))
            type = FairyType.valueOf(tag.getString("fairy_type"));
        if(type == null)type = FairyType.CYAN;
        if(tag.contains("model_type", Tag.TAG_STRING))
            modelType = ModelType.valueOf(tag.getString("model_type"));
        if(modelType == null)modelType = ModelType.NEW;
        if(tag.contains("rick", Tag.TAG_BYTE))
            rick = tag.getBoolean("rick");
    }

    public FairyType getFairyType() {
        return type;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public boolean isRick() {
        return rick;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
        setChanged();
    }

    public void setFairyType(FairyType type) {
        this.type = type;
        setChanged();
    }

    public void setRick(boolean rick) {
        this.rick = rick;
        setChanged();
    }

    public enum ModelType{
        NEW, BABY
    }
}

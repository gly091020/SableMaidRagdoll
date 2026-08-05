package com.gly091020.SableMaidRagdoll.block;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class MaidDollBlockEntity extends BlockEntity {
    private String modelID = "";
    private String soundID = "";
    public MaidDollBlockEntity(BlockPos pos, BlockState state) {
        super(SableMaidRagdoll.MAID_DOLL_BLOCK_ENTITY.get(), pos, state);
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
        setChanged();
    }

    public void setSoundID(String soundID) {
        this.soundID = soundID;
        setChanged();
    }

    public String getModelID() {
        return modelID;
    }

    public String getSoundID() {
        return soundID;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("modelID", modelID);
        tag.putString("soundID", soundID);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if(tag.contains("modelID", Tag.TAG_STRING))
            modelID = tag.getString("modelID");
        if(tag.contains("soundID", Tag.TAG_STRING))
            soundID = tag.getString("soundID");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p_323910_) {
        return saveWithoutMetadata(p_323910_);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    @OnlyIn(Dist.CLIENT)
    public long lastPat = 0;
    public boolean triggerPat;

    @OnlyIn(Dist.CLIENT)
    public void triggerPat(){
        triggerPat = true;
        lastPat = System.currentTimeMillis();
    }
}

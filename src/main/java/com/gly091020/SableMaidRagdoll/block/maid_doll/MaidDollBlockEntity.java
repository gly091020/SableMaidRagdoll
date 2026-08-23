package com.gly091020.SableMaidRagdoll.block.maid_doll;

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
import org.jetbrains.annotations.Nullable;

public class MaidDollBlockEntity extends BlockEntity {
    private String modelID = "";
    private String soundID = "";
    private boolean controlMode = false;
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
        tag.putBoolean("control", controlMode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if(tag.contains("modelID", Tag.TAG_STRING))
            modelID = tag.getString("modelID");
        if(tag.contains("soundID", Tag.TAG_STRING))
            soundID = tag.getString("soundID");
        if(tag.contains("control", Tag.TAG_BYTE))
            controlMode = tag.getBoolean("control");

        if(tag.contains("lastPat", Tag.TAG_LONG))
            lastPat = tag.getLong("lastPat");
        if(tag.contains("triggerPat", Tag.TAG_BYTE)) {
            var s = tag.getBoolean("triggerPat");
            if (s)
                triggerPat = true;
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p_323910_) {
        var tag = saveWithoutMetadata(p_323910_);
        tag.putLong("lastPat", lastPat);
        tag.putBoolean("triggerPat", triggerPat);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    public long lastPat = 0;
    public boolean triggerPat;

    public void triggerPat(){
        triggerPat = true;
        lastPat = System.currentTimeMillis();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setControlMode(boolean controlMode) {
        this.controlMode = controlMode;
        setChanged();
    }

    public boolean isControlMode() {
        return controlMode;
    }
}

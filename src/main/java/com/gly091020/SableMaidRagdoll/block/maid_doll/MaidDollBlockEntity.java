package com.gly091020.SableMaidRagdoll.block.maid_doll;

import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MaidDollBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private MaidDollData data = MaidDollData.EMPTY;
    private boolean isControl = false;
    public MaidDollBlockEntity(BlockPos pos, BlockState state) {
        super(InitBlockEntities.MAID_DOLL_BLOCK_ENTITY.get(), pos, state);
    }

    public void setData(MaidDollData data) {
        this.data = data;
        setChanged();
    }

    public String getModelID() {
        return data.modelID();
    }

    public String getSoundID() {
        return data.soundID();
    }

    public String getRagdollTypeID(){
        return data.ragdollType();
    }

    public MaidDollData getMaidDollData() {
        return data;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        MaidDollData.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .resultOrPartial(LOGGER::error)
                .ifPresent(r -> tag.put("data", r));
        tag.putBoolean("control", isControl);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        MaidDollData.CODEC.parse(NbtOps.INSTANCE, tag.get("data"))
                .resultOrPartial(LOGGER::error)
                .ifPresent(r -> data = r);
        if(tag.contains("control", Tag.TAG_BYTE))
            isControl = tag.getBoolean("control");

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

    public boolean isControlMode() {
        return isControl;
    }
}

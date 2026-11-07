package com.gly091020.SableMaidRagdoll.maid.tlm.block;

import com.gly091020.SableMaidRagdoll.maid.tlm.TLMMaidRagdoll;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitBlockEntities;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class MaidPartBlockEntity extends AbstractPartBlockEntity {
    private @Nullable String playerName;

    public MaidPartBlockEntity(BlockPos pos, BlockState state) {
        super(TLMInitBlockEntities.MAID_PART_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (playerName != null) {
            tag.putString("playerName", playerName);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        playerName = tag.contains("playerName", Tag.TAG_STRING)
                ? tag.getString("playerName")
                : null;
        if (playerName != null && playerName.isBlank()) {
            playerName = null;
        }
    }

    public Optional<String> getPlayerName() {
        return Optional.ofNullable(playerName);
    }

    public void setPlayerName(@Nullable String playerName) {
        String normalized = playerName == null || playerName.isBlank() ? null : playerName;
        if (Objects.equals(this.playerName, normalized)) {
            return;
        }
        this.playerName = normalized;
        if (level != null) {
            setChanged();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onEditorModelLoaded(ResourceLocation modelID) {
        if (!modelID.equals(TLMMaidRagdoll.PLAYER_MAID_RAGDOLL_ID)
                && !modelID.equals(TLMMaidRagdoll.PLAYER_MAID_MODEL_ID)) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player != null) {
            setPlayerName(player.getGameProfile().getName());
        }
    }
}

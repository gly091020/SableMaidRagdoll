package com.gly091020.SableMaidRagdoll.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class CopyRagdollIDItem extends Item {
    public CopyRagdollIDItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(
            @NotNull ItemStack stack,
            @NotNull Player player,
            @NotNull LivingEntity target,
            @NotNull InteractionHand hand
    ) {
        if(!player.level().isClientSide)return InteractionResult.SUCCESS;
        return interactionResult(target);
    }

    @OnlyIn(Dist.CLIENT)
    public InteractionResult interactionResult(LivingEntity target){
        if(!(target instanceof EntityMaid maid))return InteractionResult.SUCCESS;

        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        Minecraft.getInstance().keyboardHandler.setClipboard(id.toString());
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1));

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}

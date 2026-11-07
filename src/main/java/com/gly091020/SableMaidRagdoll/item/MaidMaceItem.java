package com.gly091020.SableMaidRagdoll.item;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.client.api.ITooltipAbleItem;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MaidMaceItem extends Item implements ITooltipAbleItem {
    public MaidMaceItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(!SableMaidRagdoll.CONFIG.items.maidMace)return InteractionResult.PASS;
        if(!(entity instanceof TamableAnimal maid) || maid.getVehicle() instanceof PartSeat ||
                maid.getOwnerUUID() == null || !maid.getOwnerUUID().equals(player.getUUID()))return InteractionResult.PASS;
        if(player.level().isClientSide)return InteractionResult.SUCCESS;
        var rag = MaidRagdollTypesManager.createRagdoll(entity, entity.position().add(0, 1, 0), Vec3.ZERO, new Vec3(0, 10, 0), new Vec3(0, 10, 0), true);
        if(rag == null)return InteractionResult.SUCCESS;
        rag.getExtraData().putBoolean("explosion", true);
        addCooldown(stack, player);
        if(SableMaidRagdoll.CONFIG.sounds.drop)
            player.level().playSound(null, BlockPos.containing(player.position()), InitSounds.DROP.get(), SoundSource.PLAYERS, 1, 1f);
        return InteractionResult.SUCCESS;
    }

    private void addCooldown(ItemStack stack, Player player){
        if(player.isCreative())return;
        player.getCooldowns().addCooldown(stack.getItem(), 100);
    }

    @Override
    public void appendHelpText(List<Component> tooltips) {
        generateComponentList(tooltips, this, 1, true);
    }

    @Override
    public void appendBackgroundText(List<Component> tooltips) {
        generateComponentList(tooltips, this, 1, false);
    }
}

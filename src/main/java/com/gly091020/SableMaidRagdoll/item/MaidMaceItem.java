package com.gly091020.SableMaidRagdoll.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MaidMaceItem extends Item {
    public MaidMaceItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(!SableMaidRagdoll.CONFIG.items.maidMace)return InteractionResult.PASS;
        if(!(entity instanceof EntityMaid maid) || maid.getVehicle() instanceof PartSeat ||
                maid.getOwnerUUID() == null || !maid.getOwnerUUID().equals(player.getUUID()))return InteractionResult.PASS;
        if(player.level().isClientSide)return InteractionResult.SUCCESS;
        var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        var rag = RagdollHelper.createRagdoll((ServerLevel) player.level(), maid.position().add(0, 1, 0), ragdollID);
        if(rag == null)return InteractionResult.SUCCESS;
        rag.addEntity(maid);
        rag.getExtraData().putBoolean("explosion", true);
        ScheduleManager.scheduleDelayed((ServerLevel) player.level(), 2, () -> {
            rag.addLinearImpulse(new Vec3(0, 10, 0), true);
            rag.addAngularImpulse(new Vec3(0, 10, 0), false);
        });
        addCooldown(stack, player);
        if(SableMaidRagdoll.CONFIG.sounds.drop)
            player.level().playSound(null, BlockPos.containing(player.position()), InitSounds.DROP.get(), SoundSource.PLAYERS, 1, 1f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
        p_41423_.add(Component.translatable("item.sablemaidragdoll.maid_mace.tip1").withStyle(ChatFormatting.GRAY));
        p_41423_.add(Component.translatable("item.sablemaidragdoll.maid_mace.tip2").withStyle(ChatFormatting.GRAY));
    }

    private void addCooldown(ItemStack stack, Player player){
        if(player.isCreative())return;
        player.getCooldowns().addCooldown(stack.getItem(), 100);
    }
}

package com.gly091020.SableMaidRagdoll.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.ItemMaidTooltip;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.List;
import java.util.Optional;

public class PlayerCheatDeathItem extends Item {
    public PlayerCheatDeathItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity livingEntity, InteractionHand hand) {
        if(!(livingEntity instanceof EntityMaid maid) ||
                maid.getOwner() == null ||
                !maid.getOwner().is(player))
            return super.interactLivingEntity(stack, player, livingEntity, hand);
        if(maid.level().isClientSide) {
            player.sendSystemMessage(Component.translatable("item.sablemaidragdoll.player_cheat_death.connect", maid.getModelId()));
            return InteractionResult.SUCCESS;
        }
        stack.set(SableMaidRagdoll.MAID_MODEL.get(), maid.getModelId());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if(player.getVehicle() instanceof PartSeat){
            player.stopRiding();
            return InteractionResultHolder.success(stack);
        }
        if(player.isShiftKeyDown() && !level.isClientSide){
            stack.remove(SableMaidRagdoll.MAID_MODEL.get());
            player.sendSystemMessage(Component.translatable("item.sablemaidragdoll.player_cheat_death.clear"));
            return InteractionResultHolder.success(stack);
        }
        var id = stack.get(SableMaidRagdoll.MAID_MODEL.get());
        if(id == null)return InteractionResultHolder.pass(stack);
        if(level.isClientSide)return InteractionResultHolder.success(stack);
        if(!toBeRagdoll((ServerPlayer) player, id))return InteractionResultHolder.pass(stack);
        addCooldown(stack, player);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if(useOnContext.getPlayer() != null && useOnContext.getPlayer().getVehicle() instanceof PartSeat){
            useOnContext.getPlayer().stopRiding();
            return InteractionResult.SUCCESS;
        }
        if(useOnContext.getPlayer() != null && useOnContext.getPlayer().isShiftKeyDown() && !useOnContext.getLevel().isClientSide){
            useOnContext.getItemInHand().remove(SableMaidRagdoll.MAID_MODEL.get());
            useOnContext.getPlayer().sendSystemMessage(Component.translatable("item.sablemaidragdoll.player_cheat_death.clear"));
            return InteractionResult.SUCCESS;
        }
        var id = useOnContext.getItemInHand().get(SableMaidRagdoll.MAID_MODEL.get());
        if(id == null)return InteractionResult.PASS;
        if(useOnContext.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!toBeRagdoll((ServerPlayer) useOnContext.getPlayer(), id))return InteractionResult.PASS;
        addCooldown(useOnContext.getItemInHand(), useOnContext.getPlayer());
        return InteractionResult.SUCCESS;
    }

    private void addCooldown(ItemStack stack, Player player){
        if(player.isCreative())return;
        player.getCooldowns().addCooldown(stack.getItem(), 20);
    }

    private boolean toBeRagdoll(ServerPlayer player, String id){
        var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, id.replace(":", "/"));
        var rag = RagdollHelper.createRagdoll((ServerLevel) player.level(), player.position().add(0, 0.5, 0), new Vec3(0, -player.getYHeadRot(), 0), ragdollID);
        if(rag == null)return false;
        var motion = JOMLConversion.toJOML(player.getDeltaMovement()).mul(10);
        Vector3d forward = JOMLConversion.toJOML(player.getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,1,0));
        ScheduleManager.scheduleDelayed((ServerLevel) player.level(), 2, () -> {
            rag.addEntity(player);
            rag.addLinearImpulse(motion, false);
            rag.addAngularImpulse(axis, false);
        });
        if(SableMaidRagdoll.CONFIG.sounds.hungry)
            player.level().playSound(null, BlockPos.containing(player.position()), SableMaidRagdoll.HUNGRY.get(), SoundSource.PLAYERS, 1,
                    1f + player.level().random.nextFloat());
        return true;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
        p_41423_.add(Component.translatable("item.sablemaidragdoll.player_cheat_death.tip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var modelID = stack.get(SableMaidRagdoll.MAID_MODEL.get());
        if(modelID == null)return Optional.empty();
        return Optional.of(new ItemMaidTooltip(modelID, "", YsmMaidInfo.EMPTY));
    }
}

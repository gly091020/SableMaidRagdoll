package com.gly091020.SableMaidRagdoll.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.ItemMaidTooltip;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollBlockEntity;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.init.InitCustomStats;
import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollAdvancementEvents;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.api.control.RagdollControlManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Optional;

public class PlayerCheatDeathItem extends BlockItem {
    public PlayerCheatDeathItem() {
        super(InitBlocks.MAID_DOLL_BLOCK.get(), new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState p_40612_) {
        if(!context.getItemInHand().has(InitDataComponents.MAID_MODEL.get()))return false;
        if (context.getPlayer() != null) {
            return context.getPlayer().isShiftKeyDown() && super.canPlace(context, p_40612_);
        }
        return super.canPlace(context, p_40612_);
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
        stack.set(InitDataComponents.MAID_MODEL.get(), maid.getModelId());
        stack.set(InitDataComponents.MAID_SOUND.get(), maid.getSoundPackId());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if(!SableMaidRagdoll.CONFIG.items.playerCheatDeathItem)return InteractionResultHolder.pass(stack);
        if(player.getVehicle() instanceof PartSeat){
            player.stopRiding();
            return InteractionResultHolder.success(stack);
        }
        if(player.isShiftKeyDown() && !level.isClientSide){
            stack.remove(InitDataComponents.MAID_MODEL.get());
            player.sendSystemMessage(Component.translatable("item.sablemaidragdoll.player_cheat_death.clear"));
            return InteractionResultHolder.success(stack);
        }
        var id = stack.get(InitDataComponents.MAID_MODEL.get());
        if(id == null)return InteractionResultHolder.pass(stack);
        if(level.isClientSide)return InteractionResultHolder.success(stack);
        if(!toBeRagdoll((ServerPlayer) player, stack.getOrDefault(InitDataComponents.MAID_SOUND.get(), ""),
                id, stack))return InteractionResultHolder.pass(stack);
        addCooldown(stack, player);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if(!SableMaidRagdoll.CONFIG.items.playerCheatDeathItem)return InteractionResult.PASS;
        if(useOnContext.getPlayer() != null && useOnContext.getPlayer().getVehicle() instanceof PartSeat){
            useOnContext.getPlayer().stopRiding();
            return InteractionResult.SUCCESS;
        }
        if(useOnContext.getPlayer() != null && useOnContext.getPlayer().isShiftKeyDown()){
            return super.useOn(useOnContext);
        }
        var id = useOnContext.getItemInHand().get(InitDataComponents.MAID_MODEL.get());
        if(id == null)return InteractionResult.PASS;
        if(useOnContext.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!toBeRagdoll((ServerPlayer) useOnContext.getPlayer(),
                useOnContext.getItemInHand().getOrDefault(InitDataComponents.MAID_SOUND.get(), ""),
                id, useOnContext.getItemInHand()))return InteractionResult.PASS;
        addCooldown(useOnContext.getItemInHand(), useOnContext.getPlayer());
        return InteractionResult.SUCCESS;
    }

    private void addCooldown(ItemStack stack, Player player){
        player.awardStat(Stats.CUSTOM.get(InitCustomStats.TO_MAID.get()));
        if(player instanceof ServerPlayer serverPlayer && stack.getOrDefault(InitDataComponents.ENABLE_CONTROL, false))
            InitTrigger.MAID_EVENT.get().trigger(serverPlayer, MaidRagdollAdvancementEvents.CONTROL_MAID.getName());
        if(player.isCreative())return;
        player.getCooldowns().addCooldown(stack.getItem(), 20);
    }

    private boolean toBeRagdoll(ServerPlayer player, String soundID, String id, ItemStack stack){
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
        rag.getExtraData().putString("PCDI_soundID", soundID);
        if(SableMaidRagdoll.CONFIG.sounds.hungry)
            player.level().playSound(null, BlockPos.containing(player.position()), InitSounds.HUNGRY.get(), SoundSource.PLAYERS, 1,
                    1f + player.level().random.nextFloat());

        if(stack.getOrDefault(InitDataComponents.ENABLE_CONTROL, false))
            RagdollControlManager.start(player, rag);

        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot p_150894_, ClickAction clickAction, Player player, SlotAccess p_150897_) {
        if(other.isEmpty() && clickAction == ClickAction.SECONDARY){
            stack.set(InitDataComponents.ENABLE_CONTROL, !stack.getOrDefault(InitDataComponents.ENABLE_CONTROL, false));
            if(player.level().isClientSide)
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, other, p_150894_, clickAction, player, p_150897_);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
        p_41423_.add(p_41421_.getOrDefault(InitDataComponents.ENABLE_CONTROL, false) ?
                Component.translatable("item.sablemaidragdoll.player_cheat_death.open").withStyle(ChatFormatting.GREEN):
                Component.translatable("item.sablemaidragdoll.player_cheat_death.close").withStyle(ChatFormatting.RED));
        p_41423_.add(Component.translatable("item.sablemaidragdoll.player_cheat_death.tip").withStyle(ChatFormatting.GRAY));
        p_41423_.add(Component.translatable("item.sablemaidragdoll.player_cheat_death.tip1").withStyle(ChatFormatting.GRAY));
        p_41423_.add(Component.translatable("item.sablemaidragdoll.player_cheat_death.tip2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var modelID = stack.get(InitDataComponents.MAID_MODEL.get());
        if(modelID == null)return Optional.empty();
        return Optional.of(new ItemMaidTooltip(modelID, "", YsmMaidInfo.EMPTY));
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        var r = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if(level.getBlockEntity(pos) instanceof MaidDollBlockEntity blockEntity){
            blockEntity.setModelID(stack.getOrDefault(InitDataComponents.MAID_MODEL, ""));
            blockEntity.setSoundID(stack.getOrDefault(InitDataComponents.MAID_SOUND, ""));
            blockEntity.setControlMode(stack.getOrDefault(InitDataComponents.ENABLE_CONTROL, false));
        }
        return r;
    }
}

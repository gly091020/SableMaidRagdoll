package com.gly091020.SableMaidRagdoll.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.util.AuthorUtil;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SonicWaveItem extends Item {
    private static final double RANGE = 8.0;
    private static final double HALF_ANGLE_COS = 0.5;
    private static final double PUSH_STRENGTH = 3.0;

    public SonicWaveItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
        p_41423_.add(Component.translatable("item.sablemaidragdoll.sonic_wave.tip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if(level.isClientSide)return InteractionResultHolder.success(stack);
        if(!(level instanceof ServerLevel serverLevel))return InteractionResultHolder.success(stack);

        var look = player.getLookAngle();
        pushEntities(serverLevel, player, look);

        if(SableMaidRagdoll.CONFIG.sounds.GCJCry)
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    InitSounds.GCJ_SOUND.get(), SoundSource.PLAYERS, 1, 1);
        else if (SableMaidRagdoll.CONFIG.sounds.bigDog)
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    InitSounds.DOG_CALL.get(), SoundSource.PLAYERS, 1, 1);
        else
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1, 1);

        for(int i = 1; i <= 6; i++){
            var pos = player.position().add(look.scale(i * 1.5));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y + 1, pos.z, 1, 0, 0, 0, 0);
        }

        if(!player.isCreative())
            player.getCooldowns().addCooldown(stack.getItem(), 40);
        return InteractionResultHolder.success(stack);
    }

    private static void pushEntities(ServerLevel level, Player player, Vec3 look){
        var box = player.getBoundingBox().inflate(RANGE);
        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)){
            if(entity == player || !entity.isAlive())continue;
            if(entity.getVehicle() instanceof PartSeat)continue;
            var to = entity.position().subtract(player.position());
            double dist = to.length();
            if(dist > RANGE || dist < 0.01)continue;
            var dir = to.normalize();
            if(look.dot(dir) < HALF_ANGLE_COS)continue;
            double falloff = 1 - dist / RANGE;
            if(entity instanceof EntityMaid maid && blowMaidAway(level, maid, dir))continue;
            entity.knockback(PUSH_STRENGTH * falloff, -dir.x, -dir.z);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.35 * falloff + 0.1, 0));
            entity.hurtMarked = true;

            if(entity instanceof Player && entity.getUUID().equals(AuthorUtil.GLY))
                entity.hurt(level.damageSources().playerAttack(player), 1);
        }
    }

    private static boolean blowMaidAway(ServerLevel level, EntityMaid maid, Vec3 dir){
        var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        var ragdoll = RagdollHelper.createRagdoll(level, maid.position().add(0, 1, 0), ragdollID);
        if(ragdoll == null)return false;
        ragdoll.addEntity(maid);
        ScheduleManager.scheduleDelayed(level, 2, () -> {
            ragdoll.addLinearImpulse(dir.scale(8).add(0, 2, 0), true);
            ragdoll.addAngularImpulse(new Vec3(0, 5, 0), true);
        });
        return true;
    }
}

package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.compat.tlm.WineFoxHurtDancingManager;
import com.gly091020.SableMaidRagdoll.init.InitCustomStats;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.init.InitTags;
import com.gly091020.SableMaidRagdoll.init.InitTrigger;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.maid.api.MaidSoundType;
import com.gly091020.SableMaidRagdoll.util.AuthorUtil;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollAdvancementEvents;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.api.event.EntityHurtBySubLevelEvent;
import com.gly091020.SableRagdollLib.api.event.RagdollPartCollisionEvent;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3d;

@EventBusSubscriber(modid = SableMaidRagdoll.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onMaidHurt(LivingIncomingDamageEvent event){
        if(event.isCanceled())return;
        if(event.getEntity().level().isClientSide)return;
        if(!MaidRagdollTypesManager.isSupport(event.getEntity()))return;
        float damage = event.getAmount();
        float health = event.getEntity().getHealth();

        if (health > damage)return;
        event.getEntity().getPersistentData().putFloat("smr_last_damage", event.getAmount());
    }

    @SubscribeEvent
    public static void onMaidDie(LivingDeathEvent event){
        if(!SableMaidRagdoll.CONFIG.ragdollOnDeath)return;
        if(event.isCanceled())return;
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        if(!MaidRagdollTypesManager.isSupport(event.getEntity()))return;
        if(event.getEntity().getVehicle() instanceof PartSeat){
            event.getEntity().stopRiding();
            ScheduleManager.scheduleDelayed(level, 4, () -> onMaidDie(event));
            return;
        }
        if(event.getSource().is(DamageTypes.GENERIC_KILL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))return;
        var maidMotion = JOMLConversion.toJOML(event.getEntity().getDeltaMovement()).mul(3);

        float damage;
        if(event.getEntity().getPersistentData().contains("smr_last_damage", Tag.TAG_FLOAT)) {
            damage = event.getEntity().getPersistentData().getFloat("smr_last_damage");
            event.getEntity().getPersistentData().remove("smr_last_damage");
        }else damage = 1;
        maidMotion.mul(Math.clamp(damage / 5, 0.3, 1.5));
        if(event.getSource().getEntity() != null)
            event.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, event.getSource().getEntity().getEyePosition());
        var rag = MaidRagdollTypesManager.createRagdoll(event.getEntity(), JOMLConversion.toMojang(maidMotion), Vec3.ZERO, false);
        if(rag == null)return;

        if(SableMaidRagdoll.CONFIG.sounds.hungry)
            event.getEntity().level().playSound(null, BlockPos.containing(event.getEntity().position()), InitSounds.HUNGRY.get(), SoundSource.PLAYERS, 1,
                    1f + level.random.nextFloat());
    }

    @SubscribeEvent
    public static void onOwnerAttack(LivingIncomingDamageEvent event){
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getEntity().getVehicle() instanceof PartSeat) return;
        if(!MaidRagdollTypesManager.isSupport(event.getEntity()))return;

        float damage = event.getAmount();
        float health = event.getEntity().getHealth();
        if (health < damage) return;

        boolean ownerAttack = isOwnerAttackWithTag(event.getEntity(), event.getSource());
        boolean specialDamage = SableMaidRagdoll.CONFIG.ragdollOnSpecialDamage && event.getSource().is(InitTags.ALWAYS_TO_RAGDOLL_TAG);
        if (!ownerAttack && !specialDamage) return;

        var rag = ragdollOnDamage(event.getSource(), event.getEntity());
        if(rag == null)return;
        if (ownerAttack && event.getEntity() instanceof TamableAnimal tamableAnimal && tamableAnimal.getOwner() instanceof ServerPlayer player) {
            if(AuthorUtil.isLoveWineFoxTV(player))
                MaidRagdollTypesManager.addChatBubble(event.getEntity(), Component.translatable("text.sablemaidragdoll.please_owner"));
            if(AuthorUtil.isChicken(player))
                event.getEntity().level().playSound(null, BlockPos.containing(event.getEntity().position()), InitSounds.CHICKEN.get(), SoundSource.PLAYERS, 1, 1);
            player.awardStat(Stats.CUSTOM.get(InitCustomStats.MAID_KNOCKED_AWAY.get()));
            InitTrigger.EVENT_TRIGGER.get().trigger(player, MaidRagdollAdvancementEvents.HIT_MAID.getName());
        }
        if(CompatMods.LAOWU_WINE_FOX.isLoaded() && specialDamage && event.getSource().is(InitTags.LAOWU_HURT_DANCE))
            WineFoxHurtDancingManager.startDancing(rag);
        if(ownerAttack && SableMaidRagdoll.CONFIG.sounds.watermelonHurt)
            event.getEntity().level().playSound(null, BlockPos.containing(event.getEntity().position()), InitSounds.WATERMELON_HURT.get(), SoundSource.PLAYERS, 1, 1);
        else if(SableMaidRagdoll.CONFIG.sounds.metalPipe)
            event.getEntity().level().playSound(null, BlockPos.containing(event.getEntity().position()), InitSounds.PIPE.get(), SoundSource.PLAYERS, 1, 1);
        event.setCanceled(true);
    }

    public static boolean isOwnerAttackWithTag(Entity entity, DamageSource source) {
        if(!(entity instanceof TamableAnimal maid))return false;
        var e1 = maid.getOwner();
        var e2 = source.getEntity();
        return SableMaidRagdoll.CONFIG.ragdollOnOwnerAttack &&
                e1 instanceof Player player &&
                !player.isShiftKeyDown() &&
                e2 != null &&
                e1.is(e2) &&
                player.getMainHandItem().is(InitTags.MAID_TO_RAGDOLL_TAG);
    }

    private static Ragdoll ragdollOnDamage(DamageSource damageSource, Entity entity){
        var sourceEntity = damageSource.getEntity();
        var position = damageSource.getSourcePosition();
        Vec3 direction = entity
                .position()
                .subtract(position == null ? Vec3.ZERO : position)
                .normalize();
        var maidMotion = JOMLConversion.toJOML(direction)
                .mul(5)
                .add(0, 1, 0);
        Vector3d forward = JOMLConversion.toJOML(entity.getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,-5,0));
        if (sourceEntity != null) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, sourceEntity.getEyePosition());
        } else if (position != null) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, position);
        }
        return MaidRagdollTypesManager.createRagdoll(entity, entity.position(), JOMLConversion.toMojang(axis), JOMLConversion.toMojang(maidMotion), Vec3.ZERO, true);
    }

    @SubscribeEvent
    public static void onRegistryCommand(RegisterCommandsEvent event){
        MaidRagdollCommand.registry(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onHitEntity(EntityHurtBySubLevelEvent event){
        if(!(SableMaidRagdoll.CONFIG.maidKnockback))return;
        var r = RagdollManager.get(event.getSubLevel());
        if(r == null || !(r.getEntity() instanceof LivingEntity maid) || !(MaidRagdollTypesManager.isSupport(maid)))return;
        float damage = (float) (event.getMagnitude() * 24 - 4);
        if(damage <= 0)return;
        var rag = RagdollManager.get(event.getSubLevel());
        if(rag != null && event.getTarget().is(rag.getEntity())){
            event.setDamage(0);
            return;
        }
        event.getTarget().hurt(maid.level().damageSources().mobAttack(maid), damage);
        event.setDamage(0);
    }

    @SubscribeEvent
    public static void onPartCollision(RagdollPartCollisionEvent.Post event){
        if(event.getLevel().isClientSide)return;
        if(event.getImpactVelocity() * event.getImpactVelocity() < 9)return;
        var pos2 = event.getPos2();
        if(pos2 == null)return;
        if(event.getSelfBE().getEntity() == null)return;
        onCollision(event.getSelfBE().getEntity(), pos2, event.getSelfBE());
        MaidRagdollTypesManager.onPartCollision(event.getSelfBE().getEntity(), pos2, event.getSelfBE());
    }

    private static void onCollision(Entity entity, BlockPos pos2, AbstractPartBlockEntity blockEntity){
        var rag = RagdollManager.get(blockEntity);
        if(blockEntity.getLevel() != null &&
                (!(blockEntity.getLevel().getBlockEntity(pos2) instanceof AbstractPartBlockEntity other) ||
                        other.getEntity() != blockEntity.getEntity()) && rag != null &&
                rag.getExtraData().contains("explosion", Tag.TAG_BYTE) &&
                rag.getExtraData().getBoolean("explosion")) {
            final int level = 5;
            ScheduleManager.scheduleDelayed((ServerLevel) entity.level(), 0, () ->
                    blockEntity.getLevel().explode(entity, null, MaidExplosionDamageCalculator.INSTANCE,
                            entity.position().add(0, 1, 0), level, false, Level.ExplosionInteraction.MOB));
            // rag.remove();
            // todo:不稳定的 java.lang.RuntimeException: Body has been removed
            // fuck Sable
            rag.getExtraData().remove("explosion");

            if(SableMaidRagdoll.CONFIG.sounds.metalPipe)
                entity.level().playSound(null, BlockPos.containing(entity.position()), com.gly091020.SableMaidRagdoll.init.InitSounds.PIPE.get(), SoundSource.PLAYERS, 1, 1f);
        }

        if(blockEntity.getLevel() != null && rag != null && rag.getExtraData().contains("PCDI_soundID", Tag.TAG_STRING) && entity.invulnerableTime > 0){
            var soundID = rag.getExtraData().getString("PCDI_soundID");
            var type = MaidRagdollTypesManager.getSupportType(entity);
            if(type == null)return;
            MaidRagdollTypesManager.playSound(type.getID(), blockEntity.getLevel(), entity.position(), soundID, MaidSoundType.HURT, 1);
        }
    }

    private static class MaidExplosionDamageCalculator extends ExplosionDamageCalculator {
        public static final MaidExplosionDamageCalculator INSTANCE = new MaidExplosionDamageCalculator();
        @Override
        public boolean shouldBlockExplode(Explosion p_46094_, BlockGetter p_46095_, BlockPos p_46096_, BlockState p_46097_, float p_46098_) {
            return false;
        }
    }
}

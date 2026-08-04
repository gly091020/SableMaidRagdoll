package com.gly091020.SableMaidRagdoll;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDamageEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidHurtEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import com.gly091020.SableMaidRagdoll.compat.util.MaidRollManager;
import com.gly091020.SableMaidRagdoll.util.MaidCollisionHandler;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.api.event.EntityHurtBySubLevelEvent;
import com.gly091020.SableRagdollLib.api.event.RagdollPartCollisionEvent;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

import static com.gly091020.SableRagdollLib.api.ScheduleManager.scheduleDelayed;

@EventBusSubscriber(modid = SableMaidRagdoll.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onMaidHurt(MaidDamageEvent event){
        if(event.isCanceled())return;
        if(event.getMaid().level().isClientSide)return;
        float damage = event.getAmount();
        float health = event.getMaid().getHealth();

        if (health > damage)return;
        event.getMaid().getPersistentData().putFloat("smr_last_damage", event.getAmount());
    }

    @SubscribeEvent
    public static void onMaidDie(MaidDeathEvent event){
        if(!SableMaidRagdoll.CONFIG.ragdollOnDeath)return;
        if(event.isCanceled())return;
        if(!(event.getMaid().level() instanceof ServerLevel level))return;
        if(event.getMaid().getVehicle() instanceof PartSeat){
            event.getMaid().stopRiding();
            ScheduleManager.scheduleDelayed(level, 4, () -> onMaidDie(event));
            return;
        }
        if(event.getSource().is(DamageTypes.GENERIC_KILL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))return;
        var maidMotion = JOMLConversion.toJOML(event.getMaid().getDeltaMovement()).mul(3);

        float damage;
        if(event.getMaid().getPersistentData().contains("smr_last_damage", Tag.TAG_FLOAT)) {
            damage = event.getMaid().getPersistentData().getFloat("smr_last_damage");
            event.getMaid().getPersistentData().remove("smr_last_damage");
        }else damage = 1;
        maidMotion.mul(Math.clamp(damage / 5, 0.3, 1.5));
        if(event.getSource().getEntity() != null)
            event.getMaid().lookAt(EntityAnchorArgument.Anchor.EYES, event.getSource().getEntity().getEyePosition());
        createRagdoll(level, event.getMaid(), maidMotion, false);
        scheduleDelayed(level, 4, () -> event.getMaid().setInvisible(true));

        if(SableMaidRagdoll.CONFIG.sounds.hungry)
            event.getMaid().level().playSound(null, BlockPos.containing(event.getMaid().position()), SableMaidRagdoll.HUNGRY.get(), SoundSource.PLAYERS, 1,
                    1f + level.random.nextFloat());
    }

    @SubscribeEvent
    public static void onOwnerAttack(MaidHurtEvent event){
        if (!(event.getMaid().level() instanceof ServerLevel level)) return;
        if (event.getMaid().getVehicle() instanceof PartSeat) return;

        float damage = event.getAmount();
        float health = event.getMaid().getHealth();
        if (health < damage) return;

        if (!isOwnerAttackWithTag(event.getMaid(), event.getSource())
                && !(SableMaidRagdoll.CONFIG.ragdollOnSpecialDamage && event.getSource().is(SableMaidRagdoll.ALWAYS_TO_RAGDOLL_TAG))) return;

        ragdollOnDamage(level, event.getSource(), event.getMaid());
        if (SableMaidRagdoll.CONFIG.sounds.metalPipe)
            event.getMaid().level().playSound(null, BlockPos.containing(event.getMaid().position()), SableMaidRagdoll.PIPE.get(), SoundSource.PLAYERS, 1, 1);
        event.setCanceled(true);
    }

    private static boolean isOwnerAttackWithTag(EntityMaid maid, DamageSource source) {
        var e1 = maid.getOwner();
        var e2 = source.getEntity();
        return SableMaidRagdoll.CONFIG.ragdollOnOwnerAttack &&
                e1 instanceof Player player &&
                !player.isShiftKeyDown() &&
                e2 != null &&
                e1.is(e2) &&
                player.getMainHandItem().is(SableMaidRagdoll.MAID_TO_RAGDOLL_TAG);
    }

    /**
     * 判断是否应跳过 callresponse 在 EntityMaid#hurt 入口的来源洗白。
     * 由 MixinSquared 注入器调用：条件满足时取消 callresponse 的 handler，
     * 让真实来源走完原版流程，MaidHurtEvent 里就能拿到攻击者。
     * 这里不做血量判断，最终由 {@link #onOwnerAttack(MaidHurtEvent)} 用减免后的数值决定。
     */
    public static boolean shouldSkipCallResponseBypass(EntityMaid maid, DamageSource source) {
        if (maid.level().isClientSide) return false;
        if (maid.getVehicle() instanceof PartSeat) return false;
        return isOwnerAttackWithTag(maid, source)
                || (SableMaidRagdoll.CONFIG.ragdollOnSpecialDamage && source.is(SableMaidRagdoll.ALWAYS_TO_RAGDOLL_TAG));
    }

    private static void ragdollOnDamage(ServerLevel level, DamageSource damageSource, EntityMaid maid){
        var sourceEntity = damageSource.getEntity();
        var position = damageSource.getSourcePosition();
        Vec3 direction = maid
                .position()
                .subtract(position == null ? Vec3.ZERO : position)
                .normalize();
        var maidMotion = JOMLConversion.toJOML(direction)
                .mul(5)
                .add(0, 1, 0);
        Vector3d forward = JOMLConversion.toJOML(maid.getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,-5,0));
        if (sourceEntity != null) {
            maid.lookAt(EntityAnchorArgument.Anchor.EYES, sourceEntity.getEyePosition());
        } else if (position != null) {
            maid.lookAt(EntityAnchorArgument.Anchor.EYES, position);
        }
        createRagdoll(level, maid, maidMotion, axis, true);
    }

    private static void createRagdoll(ServerLevel level, EntityMaid maid, Vector3d maidMotion, boolean addMaid){
        Vector3d forward = JOMLConversion.toJOML(maid.getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,1,0));
        createRagdoll(level, maid, maidMotion, axis, addMaid);
    }

    private static void createRagdoll(ServerLevel level, EntityMaid maid, Vector3d maidMotion, Vector3d rotation, boolean addMaid){
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        var parts = RagdollHelper.createRagdoll(level, maid.position().add(0, 0.5, 0), new Vec3(0, -maid.getYHeadRot(), 0),
                id);
        if(parts == null)return;
        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(level, 2, () -> {
            parts.addAngularImpulse(rotation, true);
            parts.addLinearImpulse(maidMotion, true);
        });
        if(addMaid)
            parts.addEntity(maid);
    }

    @SubscribeEvent
    public static void onRegistryCommand(RegisterCommandsEvent event){
        MaidRagdollCommand.registry(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event){
        MaidRollManager.tick();
    }

    @SubscribeEvent
    public static void onHitEntity(EntityHurtBySubLevelEvent event){
        if(!(SableMaidRagdoll.CONFIG.maidKnockback))return;
        var r = RagdollManager.get(event.getSubLevel());
        if(r == null || !(r.getEntity() instanceof EntityMaid maid))return;
        float damage = (float) (event.getMagnitude() * 24 - 4);
        if(damage <= 0)return;
        event.getTarget().hurt(maid.level().damageSources().mobAttack(maid), damage);
        event.setDamage(0);
    }

    @SubscribeEvent
    public static void onPartCollision(RagdollPartCollisionEvent.Post event){
        if(!SableMaidRagdoll.CONFIG.maidEat)return;
        if(event.getLevel().isClientSide)return;
        if(event.getImpactVelocity() * event.getImpactVelocity() < 9)return;
        if(!(event.getSelfBE().getEntity() instanceof EntityMaid maid))return;
        var pos2 = event.getPos2();
        if(pos2 == null)return;

        MaidCollisionHandler.onCollision(maid, pos2, event.getSelfBE());
    }
}

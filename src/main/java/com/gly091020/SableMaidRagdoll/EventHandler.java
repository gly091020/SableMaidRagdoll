package com.gly091020.SableMaidRagdoll;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDamageEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
        if(event.isCanceled())return;
        if(!(event.getMaid().level() instanceof ServerLevel level))return;
        if(event.getSource().is(DamageTypes.GENERIC_KILL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))return;
        var container = SubLevelContainer.getContainer(level);
        if(container == null)return;
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, event.getMaid().getModelId().replace(":", "/"));
        var system = container.physicsSystem();
        var parts = RagdollHelper.createRagdoll(level, event.getMaid().position().add(0, 0.5, 0),
                id);
        if(parts == null)return;
        var maidMotion = JOMLConversion.toJOML(event.getMaid().getDeltaMovement()).mul(10);

        float damage;
        if(event.getMaid().getPersistentData().contains("smr_last_damage", Tag.TAG_FLOAT)) {
            damage = event.getMaid().getPersistentData().getFloat("smr_last_damage");
            event.getMaid().getPersistentData().remove("smr_last_damage");
        }else damage = 1;
        maidMotion.mul(Math.clamp(damage / 5, 0.3, 1.5));

        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(level, 2, () -> parts.getSublevels().forEach(subLevel -> {
            if (subLevel.isRemoved()) return;

            var f = subLevel.logicalPose().transformNormalInverse(new Vector3d(maidMotion));
            system.getPhysicsHandle(subLevel).applyLinearImpulse(f);

            Vector3d velocity = new Vector3d(maidMotion);
            double speed = velocity.length();
            Vector3d angularAxis;
            if (speed > 1.0e-4) {
                angularAxis = new Vector3d(velocity).normalize().cross(new Vector3d(0, 1, 0));
                if (angularAxis.lengthSquared() < 1.0e-6) {
                    angularAxis = new Vector3d(1, 0, 0);
                } else {
                    angularAxis.normalize();
                }
            } else {
                angularAxis = new Vector3d(1, 0, 0);
            }
            double angularStrength = speed * 0.3;
            double jitter = (level.random.nextDouble() - 0.5) * 0.2;
            angularAxis.mul(angularStrength + jitter);
            system.getPhysicsHandle(subLevel).applyAngularImpulse(angularAxis.mul(-1, 1, 1));
        }));
        scheduleDelayed(level, 4, () -> event.getMaid().setInvisible(true));
    }

    @SubscribeEvent
    public static void onRegistryCommand(RegisterCommandsEvent event){
        MaidRagdollCommand.registry(event.getDispatcher());
    }
}

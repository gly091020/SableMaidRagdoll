package com.gly091020.SableMaidRagdoll;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import com.gly091020.SableMaidRagdoll.util.MaidPartColliderBoxManager;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = SableMaidRagdoll.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        MaidRagdollCommand.registry(dispatcher);
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event){
        MaidPartColliderBoxManager.reset();
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event){
        MaidPartDefFileLoader.init();
    }

    @SubscribeEvent
    public static void onMaidDie(MaidDeathEvent event){
        if(event.isCanceled())return;
        if(!(event.getMaid().level() instanceof ServerLevel level))return;
        if(event.getSource().is(DamageTypes.GENERIC_KILL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))return;
        var container = SubLevelContainer.getContainer(level);
        if(container == null)return;
        var system = container.physicsSystem();
        var parts = MaidRagdollHelper.create(level, event.getMaid().position().add(0, 0.5, 0), event.getMaid().getModelId());
        var maidMotion = JOMLConversion.toJOML(event.getMaid().getDeltaMovement()).mul(10);
        event.getMaid().remove(Entity.RemovalReason.KILLED);
        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(level, 2, () -> parts.forEach(subLevel -> {
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
    }

    private static final List<DelayedTask> DELAYED_TASKS = new CopyOnWriteArrayList<>();

    private record DelayedTask(long targetTick, Runnable runnable) {}

    public static void scheduleDelayed(ServerLevel level, int delayTicks, Runnable runnable) {
        long target = level.getServer().getTickCount() + delayTicks;
        DELAYED_TASKS.add(new DelayedTask(target, runnable));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        long now = event.getServer().getTickCount();
        for (DelayedTask task : DELAYED_TASKS) {
            if (task.targetTick() <= now) {
                task.runnable().run();
                DELAYED_TASKS.remove(task);
            }
        }
    }
}

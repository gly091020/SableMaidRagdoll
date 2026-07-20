package com.gly091020.SableMaidRagdoll;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDamageEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidHurtEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;

import static com.github.tartaricacid.touhoulittlemaid.init.InitItems.MUTE_BAUBLE;
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
    }

    @SubscribeEvent
    public static void onOwnerAttack(MaidHurtEvent event){
        if(!SableMaidRagdoll.CONFIG.ragdollOnOwnerAttack)return;
        if(!(event.getMaid().level() instanceof ServerLevel level))return;
        float damage = event.getAmount();
        float health = event.getMaid().getHealth();
        if (health < damage)return;
        if(damage < 0.1)return;
        var e1 = event.getMaid().getOwner();
        var e2 = event.getSource().getEntity();
        if(e1 instanceof Player player && player.isShiftKeyDown())return;
        if(e1 == null || e2 == null || !e1.is(e2))return;
        if(!(e2 instanceof Player player && player.getMainHandItem().is(SableMaidRagdoll.MAID_TO_RAGDOLL)))return;
        Vec3 direction = event.getMaid()
                .position()
                .subtract(e2.position())
                .normalize();
        var maidMotion = JOMLConversion.toJOML(direction)
                .mul(5)
                .add(0, 1, 0);
        Vector3d forward = JOMLConversion.toJOML(event.getMaid().getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,-5,0));
        event.getMaid().lookAt(EntityAnchorArgument.Anchor.EYES, e2.getEyePosition());
        createRagdoll(level, event.getMaid(), maidMotion, axis, true);
        event.setCanceled(true);
        event.getMaid().level().playSound(null, BlockPos.containing(event.getMaid().position()), SableMaidRagdoll.PIPE.get(), SoundSource.PLAYERS, 1, 1);
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
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == InitCreativeTabs.MAIN_TAB.getKey()){
            if(!SableMaidRagdoll.CONFIG.cheatDeathBauble)return;
            event.insertAfter(MUTE_BAUBLE.get().getDefaultInstance(), SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}

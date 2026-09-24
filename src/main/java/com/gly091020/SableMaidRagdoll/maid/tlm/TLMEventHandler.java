package com.gly091020.SableMaidRagdoll.maid.tlm;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.FairyType;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.compat.tlm.MaidRollManager;
import com.gly091020.SableMaidRagdoll.compat.tlm.WineFoxHurtDancingManager;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.init.InitTags;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.MaidFairyPartBlockEntity;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

import static com.gly091020.SableRagdollLib.api.ScheduleManager.scheduleDelayed;

public class TLMEventHandler {
    @SubscribeEvent
    public static void onMaidFairyHurt(LivingDamageEvent.Post event){
        if(!SableMaidRagdoll.CONFIG.ragdollOnOwnerAttack)return;
        if(!(event.getEntity() instanceof EntityFairy fairy))return;
        if(fairy.getVehicle() instanceof PartSeat)return;
        if(!(fairy.level() instanceof ServerLevel serverLevel))return;

        boolean flag1 = event.getSource().getEntity() instanceof Player player && player.getMainHandItem().is(InitTags.MAID_TO_RAGDOLL_TAG);
        boolean flag2 = event.getSource().is(InitTags.ALWAYS_TO_RAGDOLL_TAG);
        if(!flag1 && !flag2)return;

        float damage = event.getNewDamage();
        float health = fairy.getHealth();
        if (health < damage) return;

        createFairyRagdoll(serverLevel, fairy, JOMLConversion.toJOML(fairy.getDeltaMovement().scale(3)));
        if(SableMaidRagdoll.CONFIG.sounds.metalPipe)
            fairy.level().playSound(null, BlockPos.containing(fairy.position()), InitSounds.PIPE.get(), SoundSource.PLAYERS, 1, 1);
    }

    public static final ResourceLocation BABY_FAIRY = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "fairy/baby_fairy");
    public static final ResourceLocation NEW_FAIRY = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "fairy/new_fairy");
    public static void createFairyRagdoll(ServerLevel level, EntityFairy fairy, Vector3d maidMotion){
        Vector3d forward = JOMLConversion.toJOML(fairy.getLookAngle());
        Vector3d axis = forward.cross(new Vector3d(0,1,0));
        var id = fairy.isBaby() ? BABY_FAIRY : NEW_FAIRY;
        var parts = RagdollHelper.createRagdoll(level, fairy.position().add(0, 0.5, 0), new Vec3(0, -fairy.getYHeadRot(), 0),
                id);
        if(parts == null)return;
        parts.getSublevels().forEach(subLevel -> {
            if(subLevel.getPlot().getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO) instanceof MaidFairyPartBlockEntity blockEntity){
                blockEntity.setFairyType(FairyType.values()[fairy.getFairyTypeOrdinal()]);
                blockEntity.setRick(fairy.getName().getString().equals("rick"));
                blockEntity.setModelType(fairy.isBaby() ? MaidFairyPartBlockEntity.ModelType.BABY : MaidFairyPartBlockEntity.ModelType.NEW);
            }
        });
        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(level, 2, () -> {
            parts.addAngularImpulse(axis, true);
            parts.addLinearImpulse(maidMotion, true);
            if(fairy.isAlive())
                parts.addEntity(fairy);
        });
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event){
        MaidRollManager.tick();
        WineFoxHurtDancingManager.tick();
    }
}

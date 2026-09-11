package com.gly091020.SableMaidRagdoll.maid.api;

import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gly091020.SableRagdollLib.api.ScheduleManager.scheduleDelayed;

public class MaidRagdollTypesManager {
    private static final List<IMaidRagdoll> TYPES = new ArrayList<>();

    public static void registry(IMaidRagdoll maidRagdoll) {
        TYPES.add(maidRagdoll);
    }

    public static List<IMaidRagdoll> getTypes() {
        return TYPES;
    }

    public static IMaidRagdoll getSupportType(Entity entity) {
        for (IMaidRagdoll type : TYPES)
            if (type.isSupportMaid(entity)) return type;
        return null;
    }

    public static IMaidRagdoll getType(String id) {
        for (IMaidRagdoll type : TYPES)
            if (Objects.equals(type.getID(), id)) return type;
        return null;
    }

    public static boolean isSupport(Entity entity) {
        return getSupportType(entity) != null;
    }

    public static Ragdoll createRagdoll(ServerLevel serverLevel, Entity entity, Vec3 position, Vec3 rotation, Vec3 linearImpulse, Vec3 angularImpulse, boolean addEntity) {
        var t = getSupportType(entity);
        if (t == null) return null;
        var ragdoll = t.toMaidRagdoll(serverLevel, entity, position, rotation);
        if (ragdoll == null) return null;
        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(serverLevel, 2, () -> {
            ragdoll.addAngularImpulse(angularImpulse, true);
            ragdoll.addLinearImpulse(linearImpulse, true);
        });
        if (addEntity)
            ragdoll.addEntity(entity);
        return ragdoll;
    }

    public static Ragdoll createRagdollFromDoll(ServerLevel serverLevel, Entity entity, Vec3 position, Vec3 rotation, Vec3 linearImpulse, Vec3 angularImpulse, boolean addEntity, MaidDollData data) {
        var t = getType(data.ragdollType());
        if (t == null) return null;
        var ragdoll = RagdollHelper.createRagdoll(serverLevel, position, rotation, t.getRagdollId(data));
        if (ragdoll == null) return null;
        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(serverLevel, 2, () -> {
            ragdoll.addAngularImpulse(angularImpulse, true);
            ragdoll.addLinearImpulse(linearImpulse, true);
        });
        if (addEntity)
            ragdoll.addEntity(entity);
        return ragdoll;
    }

    public static Ragdoll createRagdollFromDoll(Entity entity, Vec3 position, Vec3 rotation, Vec3 linearImpulse, Vec3 angularImpulse, boolean addEntity, MaidDollData data) {
        return entity.level() instanceof ServerLevel serverLevel ? createRagdollFromDoll(serverLevel, entity, position, rotation, linearImpulse, angularImpulse, addEntity, data) : null;
    }

    public static Ragdoll createRagdoll(Entity entity, Vec3 position, Vec3 rotation, Vec3 linearImpulse, Vec3 angularImpulse, boolean addEntity) {
        return entity.level() instanceof ServerLevel serverLevel ? createRagdoll(serverLevel, entity, position, rotation, linearImpulse, angularImpulse, addEntity) : null;
    }

    public static Ragdoll createRagdoll(Entity entity, Vec3 linearImpulse, Vec3 angularImpulse, boolean addEntity) {
        return createRagdoll(entity, entity.position(), new Vec3(0, -entity.getYHeadRot(), 0), linearImpulse, angularImpulse, addEntity);
    }

    public static void addChatBubble(Entity entity, Component text) {
        var t = getSupportType(entity);
        if (t == null) return;
        t.addChatBubble(entity, text);
    }

    public static void playSound(String id, Level level, Vec3 position, String soundPackID, MaidSoundType soundType, float volume) {
        var t = getType(id);
        if (t == null) return;
        t.playMaidSound(level, position, soundPackID, soundType, volume);
    }

    public static @Nullable Pair<Entity, ItemStack> releaseEntityFromItem(Level level, ItemStack stack) {
        for (IMaidRagdoll t : TYPES) {
            var r = t.releaseEntityFromItem(level, stack);
            if (r != null) return r;
        }
        return null;
    }

    public static boolean hasEntity(ItemStack stack) {
        for (IMaidRagdoll t : TYPES) {
            var r = t.hasEntity(stack);
            if (r) return true;
        }
        return false;
    }

    public static boolean isCannonSpecialItem(ItemStack stack) {
        for (IMaidRagdoll t : TYPES) {
            var r = t.isCannonSpecialItem(stack);
            if (r) return true;
        }
        return false;
    }

    public static void onCannonLaunch(MobCannonBlockEntity blockEntity, Entity entity, Vector3d force) {
        TYPES.forEach(t -> t.onCannonLaunch(blockEntity, entity, force));
    }

    @OnlyIn(Dist.CLIENT)
    public static Entity getRenderEntity(Level level, MaidDollData data) {
        var t = getType(data.ragdollType());
        if(t == null)return null;
        return t.getRenderEntity(level, data);
    }

    public static void appendCommand(LiteralArgumentBuilder<CommandSourceStack> root) {
        TYPES.forEach(t -> t.appendCommand(root));
    }

    @OnlyIn(Dist.CLIENT)
    public static TooltipComponent getTooltipImage(MaidDollData data) {
        var t = getType(data.ragdollType());
        if(t == null)return null;
        return t.getTooltipImage(data);
    }

    public static @Nullable MaidDollData getDataFromEntity(Entity entity) {
        var t = getSupportType(entity);
        if(t == null)return null;
        return t.getDataFromEntity(entity);
    }

    public static void registryNetwork(PayloadRegistrar registrar) {
        TYPES.forEach(t -> t.registryNetwork(registrar));
    }

    public static void onPartCollision(Entity entity, BlockPos pos2, AbstractPartBlockEntity blockEntity){
        var t = getSupportType(entity);
        if(t == null)return;
        t.onPartCollision(entity, pos2, blockEntity);
    }

    public static void generateCreateTabDoll(CreativeModeTab.Output output){
        TYPES.forEach(t -> t.generateCreateTabDoll(output));
    }

    public static void appendCreateTabItem(CreativeModeTab.Output output){
        TYPES.forEach(t -> t.appendCreateTabItem(output));
    }
}

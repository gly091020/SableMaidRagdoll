package com.gly091020.SableMaidRagdoll.maid.api;

import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public interface IMaidRagdoll {
    // 获取 ID
    String getID();

    // 只有这里判断通过才会继续调用相关代码
    boolean isSupportMaid(Entity entity);

    // 可用来注册部位等数据
    void init(IEventBus bus);

    // 获得布娃娃id
    @Nullable
    ResourceLocation getRagdollId(Entity entity);

    // 获得布娃娃id
    @Nullable
    ResourceLocation getRagdollId(MaidDollData data);

    // 创建布娃娃
    @Nullable
    Ragdoll toMaidRagdoll(ServerLevel serverLevel, Entity entity, Vec3 position, Vec3 rotation);

    // 创建聊天气泡
    void addChatBubble(Entity entity, Component text);

    // 播放女仆声音
    // 双端都可能调用，自行判断
    void playMaidSound(Level level, Vec3 position, String soundPackID, MaidSoundType soundType, float volume);

    // 从物品中提取
    // 释放实体，释放后物品
    // 实体不需要添加到level
    @Nullable
    Pair<Entity, ItemStack> releaseEntityFromItem(Level level, ItemStack stack);

    // 物品中是否有实体
    boolean hasEntity(ItemStack stack);

    // 是否是可用大炮升级
    boolean isCannonSpecialItem(ItemStack stack);

    // 当被大炮发射
    void onCannonLaunch(MobCannonBlockEntity blockEntity, Entity entity, Vector3d force);

    // 用于获取玩偶渲染时获取的生物
    @OnlyIn(Dist.CLIENT)
    Entity getRenderEntity(Level level, MaidDollData data);

    // 指令附加
    void appendCommand(LiteralArgumentBuilder<CommandSourceStack> root);

    // 获得渲染图片
    @OnlyIn(Dist.CLIENT)
    @Nullable
    TooltipComponent getTooltipImage(MaidDollData data);

    // 从生物获取伪装数据
    @Nullable
    MaidDollData getDataFromEntity(Entity entity);

    // 注册网络包
    void registryNetwork(PayloadRegistrar registrar);

    // 肢体碰撞时的回调
    void onPartCollision(Entity entity, BlockPos pos2, AbstractPartBlockEntity blockEntity);

    // 创建创造物品栏的替身女仆玩偶
    void generateCreateTabDoll(CreativeModeTab.Output output);

    // 创造物品栏追加
    void appendCreateTabItem(CreativeModeTab.Output output);
}

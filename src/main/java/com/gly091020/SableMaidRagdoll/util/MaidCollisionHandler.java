package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.MaidEdibleBlockManager;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntitySnackCabinet;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaidCollisionHandler {
    private static final long COLLISION_COOLDOWN_MILLIS = 100;
    private static final Map<UUID, Long> LAST_COLLISION_TIME = new HashMap<>();

    public static void onCollision(EntityMaid maid, BlockPos pos2, AbstractPartBlockEntity blockEntity){
        if(!isCollisionReady(maid))return;
        if(blockEntity.getPartData().partName().toLowerCase().contains("head")) {
            maidEatCake(maid, pos2);
            maidEatSnackCabinet(maid, pos2);
            maidEatPicnicMat(maid, pos2);
        }
    }

    private static boolean isCollisionReady(EntityMaid maid){
        long now = System.currentTimeMillis();
        Long last = LAST_COLLISION_TIME.get(maid.getUUID());
        if(last != null && now - last < COLLISION_COOLDOWN_MILLIS)return false;
        LAST_COLLISION_TIME.put(maid.getUUID(), now);
        return true;
    }

    public static void maidEatCake(EntityMaid maid, BlockPos pos2){
        var state = maid.level().getBlockState(pos2);

        MixinFunction.alwaysCanEat = true;
        IMaidEdibleBlock target = null;
        for (IMaidEdibleBlock edibleBlock: MaidEdibleBlockManager.getEdibleBlocks())
            if(edibleBlock.shouldMoveTo(maid, pos2, state))
                target = edibleBlock;
        if(target == null)return;
        IMaidEdibleBlock finalTarget = target;
        ScheduleManager.scheduleDelayed((ServerLevel) maid.level(), 0, () -> {
            var result = finalTarget.consume(maid, pos2, state);
            if (result) {
                int points = finalTarget.getFavorabilityPoints(maid, pos2, state);
                maid.getFavorabilityManager().apply(Type.STEAL_EDIBLE_BLOCK, points);
            }
        });
        MixinFunction.alwaysCanEat = false;
    }

    public static void maidEatSnackCabinet(EntityMaid maid, BlockPos pos2){
        if(!(maid.level().getBlockEntity(pos2) instanceof TileEntitySnackCabinet snackCabinet))return;
        var item = extractEdibleStack(snackCabinet);
        if(item.isEmpty())return;
        maid.eat(maid.level(), item);
        maid.getFavorabilityManager().apply(Type.HOME_MEAL);
    }

    public static void maidEatPicnicMat(EntityMaid maid, BlockPos pos2){
        if(!(maid.level().getBlockEntity(pos2) instanceof TileEntityPicnicMat be))return;
        if(!(maid.level().getBlockEntity(be.getCenterPos()) instanceof TileEntityPicnicMat picnicMat))return;
        var item = extractEdibleStack(picnicMat.getHandler());
        if(item.isEmpty())return;
        maid.eat(maid.level(), item);
        maid.getFavorabilityManager().apply(Type.HOME_MEAL);
    }

    public static ItemStack extractEdibleStack(ItemStackHandler handler){
        for (int i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                return stack.split(1);
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack extractEdibleStack(Container container){
        for (int i = 0; i < container.getContainerSize(); i++) {
            var stack = container.getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                return stack.split(1);
            }
        }
        return ItemStack.EMPTY;
    }
}

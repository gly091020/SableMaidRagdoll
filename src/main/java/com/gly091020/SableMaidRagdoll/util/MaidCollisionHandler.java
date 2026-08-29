package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.MaidEdibleBlockManager;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.network.message.PlayMaidSoundAtPosPackage;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntitySnackCabinet;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaidCollisionHandler {
    private static final long COLLISION_COOLDOWN_MILLIS = 100;
    private static final Map<UUID, Long> LAST_COLLISION_TIME = new HashMap<>();

    public static void tryActivateSwitch(Level level, Entity entity, BlockPos pos1, BlockPos pos2){
        if(level.getBlockEntity(pos2) instanceof AbstractPartBlockEntity)return;
        var p1 = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) pos1.getCenter());
        var p2 = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) pos1.getCenter());
        var dir = Direction.getNearest(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
        ScheduleManager.scheduleDelayed((ServerLevel) level, 0, () -> activateIfSwitch(level, entity, pos2.relative(dir)));
    }

    private static boolean activateIfSwitch(Level level, Entity entity, BlockPos pos){
        var state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof ButtonBlock) && !(state.getBlock() instanceof LeverBlock))return false;
        state.useWithoutItem(level, entity instanceof Player player ? player : null,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return true;
    }

    public static void onCollision(Entity entity, BlockPos pos2, AbstractPartBlockEntity blockEntity){
        if(!isCollisionReady(entity))return;
        var rag = RagdollManager.get(blockEntity);

        if(entity instanceof EntityMaid maid){
            if(SableMaidRagdoll.CONFIG.maidEat && blockEntity.getPartData().partName().toLowerCase().contains("head")) {
                maidEatCake(maid, pos2);
                maidEatSnackCabinet(maid, pos2);
                maidEatPicnicMat(maid, pos2);
            }
            if(blockEntity.getLevel() != null &&
                    (!(blockEntity.getLevel().getBlockEntity(pos2) instanceof AbstractPartBlockEntity other) ||
                            other.getEntity() != blockEntity.getEntity()) && rag != null &&
                    rag.getExtraData().contains("explosion", Tag.TAG_BYTE) &&
                    rag.getExtraData().getBoolean("explosion")) {
                var level = 2 + maid.getFavorability() / 384 * 5;
                ScheduleManager.scheduleDelayed((ServerLevel) maid.level(), 0, () ->
                        blockEntity.getLevel().explode(maid, null, MaidExplosionDamageCalculator.INSTANCE,
                                maid.position().add(0, 1, 0), level, false, Level.ExplosionInteraction.MOB));
                // rag.remove();
                // todo:不稳定的 java.lang.RuntimeException: Body has been removed
                // fuck Sable
                rag.getExtraData().remove("explosion");

                if(SableMaidRagdoll.CONFIG.sounds.metalPipe)
                    maid.level().playSound(null, BlockPos.containing(maid.position()), com.gly091020.SableMaidRagdoll.init.InitSounds.PIPE.get(), SoundSource.PLAYERS, 1, 1f);
            }
        }

        if(blockEntity.getLevel() != null && rag != null && rag.getExtraData().contains("PCDI_soundID", Tag.TAG_STRING) && entity.invulnerableTime > 0){
            var soundID = rag.getExtraData().getString("PCDI_soundID");
            PacketDistributor.sendToAllPlayers(new PlayMaidSoundAtPosPackage(
                    InitSounds.MAID_HURT.getId(), soundID,
                    entity.getX(), entity.getY(), entity.getZ(), 1, 1
                    ));
        }
    }

    private static class MaidExplosionDamageCalculator extends ExplosionDamageCalculator {
        public static final MaidExplosionDamageCalculator INSTANCE = new MaidExplosionDamageCalculator();
        @Override
        public boolean shouldBlockExplode(Explosion p_46094_, BlockGetter p_46095_, BlockPos p_46096_, BlockState p_46097_, float p_46098_) {
            return false;
        }
    }

    private static boolean isCollisionReady(Entity maid){
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

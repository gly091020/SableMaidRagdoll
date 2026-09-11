package com.gly091020.SableMaidRagdoll.block.mob_cannon;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.compat.player_ragdoll.PlayerRagdollUtil;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.item.spawn_egg.SMRDeferredSpawnEggItem;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.menu.MobCannonMenu;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import static com.gly091020.SableMaidRagdoll.init.InitBlockEntities.MOB_CANNON_BLOCK_ENTITY;
import static com.gly091020.SableMaidRagdoll.init.InitItems.*;

public class MobCannonBlockEntity extends BlockEntity implements IItemHandlerModifiable, MenuProvider {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private double xRot = 0;
    private double yRot = 90;
    private double cooldown = 0;

    public MobCannonBlockEntity(BlockPos pos, BlockState state) {
        super(MOB_CANNON_BLOCK_ENTITY.get(), pos, state);
    }

    public double getXRot() {
        return xRot;
    }

    public void setXRot(double xRot) {
        this.xRot = xRot;
        markUpdated();
    }

    public double getYRot() {
        return yRot;
    }

    public void setYRot(double yRot) {
        this.yRot = yRot;
        markUpdated();
    }

    public double getCooldown() {
        return cooldown;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
        markUpdated();
    }

    public boolean isRandomShoot(){
        return stacks.get(1).is(Items.BOW);
    }

    public Vec3 getAimVector() {
        double yaw = Math.toRadians(yRot);
        double pitch = Math.toRadians(xRot);
        if(isRandomShoot() && level != null){
            yaw += (level.random.nextFloat() - 0.5) * 2;
            pitch += (level.random.nextFloat() - 0.5) * 2;
        }
        Vec3 direction = new Vec3(
                -Math.sin(yaw) * Math.cos(pitch),
                Math.sin(pitch),
                -Math.cos(yaw) * Math.cos(pitch)
        );
        if (SableCompanion.INSTANCE.getContaining(this) instanceof SubLevel subLevel) {
            direction = subLevel.logicalPose().transformNormal(direction);
        }
        return direction;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= stacks.size()) return;
        stacks.set(slot, stack.copyWithCount(Math.min(stack.getCount(), getSlotLimit(slot))));
        markUpdated();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, stacks, provider);
        tag.putDouble("XRot", xRot);
        tag.putDouble("YRot", yRot);
        tag.putDouble("Cooldown", cooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        stacks = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks, provider);
        if (tag.contains("XRot", Tag.TAG_ANY_NUMERIC)) xRot = tag.getDouble("XRot");
        if (tag.contains("YRot", Tag.TAG_ANY_NUMERIC)) yRot = tag.getDouble("YRot");
        if (tag.contains("Cooldown", Tag.TAG_ANY_NUMERIC)) cooldown = tag.getDouble("Cooldown");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return stacks.get(i);
    }

    @Override
    public ItemStack insertItem(int i, ItemStack itemStack, boolean b) {
        if (i < 0 || i >= stacks.size() || itemStack.isEmpty() || !isItemValid(i, itemStack)) return itemStack;
        ItemStack existing = stacks.get(i);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(itemStack, existing)) return itemStack;

        int limit = Math.min(getSlotLimit(i), itemStack.getMaxStackSize()) - existing.getCount();
        if (limit <= 0) return itemStack;

        boolean over = itemStack.getCount() > limit;
        if (!b) {
            if (existing.isEmpty()) {
                stacks.set(i, over ? itemStack.copyWithCount(limit) : itemStack);
            } else {
                existing.grow(over ? limit : itemStack.getCount());
            }
            markUpdated();
        }
        return over ? itemStack.copyWithCount(itemStack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        if (i < 0 || i >= stacks.size() || i1 <= 0) return ItemStack.EMPTY;
        ItemStack existing = stacks.get(i);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int amount = Math.min(i1, existing.getMaxStackSize());
        if (existing.getCount() <= amount) {
            if (!b) {
                stacks.set(i, ItemStack.EMPTY);
                markUpdated();
                return existing;
            }
            return existing.copy();
        }

        if (!b) {
            stacks.set(i, existing.copyWithCount(existing.getCount() - amount));
            markUpdated();
        }
        return existing.copyWithCount(amount);
    }

    @Override
    public int getSlotLimit(int i) {
        if(i == 1)return 1;
        return stacks.get(i).getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int i, ItemStack itemStack) {
        var bool = i >= 0 && i < stacks.size() && !itemStack.isEmpty();
        boolean specialItem = true;
        if(i == 0)
            specialItem = itemStack.getItem() instanceof SpawnEggItem ||
                    MaidRagdollTypesManager.hasEntity(itemStack);
        if(i == 1)
            specialItem = itemStack.is(Items.BOW) ||
                    itemStack.is(Items.TNT) ||
                    itemStack.is(Items.REDSTONE) ||
                    itemStack.is(SONIC_WAVE_ITEM) ||
                    itemStack.is(MOB_CANNON_ITEM) ||
                    itemStack.is(MAID_MACE_ITEM) ||
                    MaidRagdollTypesManager.isCannonSpecialItem(itemStack);
        return bool && specialItem;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.sablemaidragdoll.mob_cannon");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MobCannonMenu(id, inventory, this);
    }

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isPlayerMode(){
        return getStackInSlot(1).is(MOB_CANNON_ITEM) && getStackInSlot(0).isEmpty();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MobCannonBlockEntity blockEntity){
        if(level.isClientSide)return;
        if(blockEntity.isPlayerMode())return;
        blockEntity.tryFire();
    }

    public void tryFire(){
        if(level == null)return;
        if (getCooldown() <= 0 && level.hasNeighborSignal(getBlockPos()) && canFire()) {
            fire();
            cooldown = 1.0;
            if(getStackInSlot(1).is(Items.REDSTONE))
                cooldown = 0.5;
            else if(getStackInSlot(1).is(SONIC_WAVE_ITEM))
                cooldown = 0.25;
            setChanged();
            if(SableMaidRagdoll.CONFIG.sounds.drop)
                level.playSound(null, getBlockPos(), InitSounds.CANNON_LAUNCH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            else level.playSound(null, getBlockPos(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (getCooldown() > 0) {
            cooldown = Math.max(0, getCooldown() - 1 / 20d);
            setChanged();
        }
    }

    public void fire() {
        if(!(level instanceof ServerLevel))return;
        var force = JOMLConversion.toJOML(getAimVector());
        if(getStackInSlot(1).is(Items.TNT))
            force.mul(3);
        var entity = getEntity(JOMLConversion.toMojang(force));
        if(entity == null)return;
        MaidRagdollTypesManager.onCannonLaunch(this, entity, force);
        var mj = JOMLConversion.toMojang(force);
        entity.setDeltaMovement(mj);
        toRagdoll(entity, mj);
    }

    public boolean canFire(){
        if(isPlayerMode())return true;
        var launchStack = getStackInSlot(0);
        var maid = MaidRagdollTypesManager.hasEntity(launchStack);
        var spawnEgg = launchStack.getItem() instanceof SpawnEggItem;
        return maid || spawnEgg;
    }

    public Vec3 getReallyPos(){
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) getBlockPos().getCenter().add(0, 0.5, 0));
    }

    @Nullable
    public Entity getEntity(Vec3 force){
        if(!(level instanceof ServerLevel serverLevel))return null;
        var pos = getReallyPos();
        var launchStack = getStackInSlot(0);
        Entity entity = null;
        if(MaidRagdollTypesManager.hasEntity(launchStack)) {
            var pair = MaidRagdollTypesManager.releaseEntityFromItem(level, launchStack);
            if(pair != null) {
                entity = pair.getFirst();
                setStackInSlot(0, pair.getSecond());
            }
        }else if(launchStack.getItem() instanceof SpawnEggItem spawnEggItem){
            entity = spawnEggItem.getType(launchStack).spawn(serverLevel, getBlockPos(), MobSpawnType.SPAWN_EGG);
            if(launchStack.getItem() instanceof SMRDeferredSpawnEggItem smrDeferredSpawnEggItem)
                smrDeferredSpawnEggItem.afterSpawn(null, entity);
        }
        if(entity == null){
            return tryGetPlayer(pos);
        }

        serverLevel.addFreshEntity(entity);
        entity.moveTo(pos);
        entity.setXRot((float) xRot);
        entity.setYRot((float) yRot);
        entity.setYHeadRot((float) yRot);
        entity.setDeltaMovement(force);
        return entity;
    }

    public Entity tryGetPlayer(Vec3 pos){
        if(!isPlayerMode())return null;
        if(level == null)return null;
        var p = level.getNearestPlayer(pos.x, pos.y, pos.z, 5, false);
        if(p != null)p.moveTo(pos);
        return p;
    }

    public void toRagdoll(Entity entity, Vec3 force){
        var rag = MaidRagdollTypesManager.createRagdoll(entity, force.scale(10), new Vec3(-10, 0, 0), true);
        if(isExplosion() && rag != null)
            rag.getExtraData().putBoolean("explosion", true);
        if(rag != null)return;
        if(CompatMods.PLAYER_RAGDOLL.isLoaded() && entity instanceof ServerPlayer player){
            PlayerRagdollUtil.launch(player, force.scale(25));
            return;
        }
        if(CompatMods.PLAYER_RAGDOLL.isLoaded() && entity instanceof LivingEntity livingEntity){
            PlayerRagdollUtil.launchMob(livingEntity, force.scale(25));
        }
    }

    public boolean isExplosion(){
        return getStackInSlot(1).is(MAID_MACE_ITEM);
    }
}

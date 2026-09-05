package com.gly091020.SableMaidRagdoll.menu;

import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableMaidRagdoll.init.InitMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MobCannonMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 2;
    private static final int SLOT_X_0 = 79;
    private static final int SLOT_Y_0 = 31;
    private static final int SLOT_X_1 = 152;
    private static final int SLOT_Y_1 = 59;

    private final MobCannonBlockEntity blockEntity;
    private final IItemHandlerModifiable itemHandler;

    public MobCannonMenu(int id, Inventory inventory) {
        super(InitMenus.MOB_CANNON.get(), id);
        this.blockEntity = null;
        this.itemHandler = new ItemStackHandler(SLOT_COUNT);
        addCannonSlots();
        addPlayerInventory(inventory);
    }

    public MobCannonMenu(int id, Inventory inventory, MobCannonBlockEntity blockEntity) {
        super(InitMenus.MOB_CANNON.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity;
        addCannonSlots();
        addPlayerInventory(inventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null
                || !blockEntity.isRemoved()
                && player.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack copy = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < SLOT_COUNT) {
            if (!moveItemStackTo(moving, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(moving, 0, SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private void addPlayerInventory(Inventory inventory) {
        int startX = (354 - 162) / 2 - 88;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, startX + col * 18, 212 + row * 18 - 126));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, startX + col * 18, 304 - 160));
        }
    }

    private void addCannonSlots() {
        addSlot(new SlotItemHandler(itemHandler, 0, SLOT_X_0, SLOT_Y_0));
        addSlot(new SlotItemHandler(itemHandler, 1, SLOT_X_1, SLOT_Y_1));
    }
}

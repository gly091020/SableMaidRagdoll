package com.gly091020.SableMaidRagdoll.compat.curios_api;

import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.gly091020.SableMaidRagdoll.item.PlayerCheatDeathItem;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MaidDollICurioItem implements ICurioItem {
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        if(entity.getVehicle() != null)return;
        if(!(entity instanceof ServerPlayer serverPlayer))return;
        var data = stack.get(InitDataComponents.MAID_DOLL_DATA);
        if(data == null)return;

        if(entity.tickCount <= 10)return;
        if(entity.tickCount % 4 == 0)
            PlayerCheatDeathItem.toRagdoll(serverPlayer, data, stack.getOrDefault(InitDataComponents.CONTROL_MODE, false));
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return stack.has(InitDataComponents.MAID_DOLL_DATA);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        var entity = slotContext.entity();
        if(!entity.level().isClientSide && entity.getVehicle() instanceof PartSeat)
            entity.stopRiding();
    }
}

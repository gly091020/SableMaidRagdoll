package com.gly091020.SableMaidRagdoll.compat.curios_api;

import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class MaidRagdollCuriosAPIUtil {
    public static void init(IEventBus bus){
        NeoForge.EVENT_BUS.register(EventHandler.class);
        bus.addListener(MaidRagdollCuriosAPIUtil::onRegister);
    }

    private static void onRegister(FMLCommonSetupEvent event){
        CuriosApi.registerCurio(InitItems.PLAYER_CHEAT_DEATH_ITEM.get(), new MaidDollICurioItem());
    }

    private static class EventHandler{
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onStopRidding(EntityMountEvent event){
            if(!(event.getEntity() instanceof LivingEntity livingEntity) || !(livingEntity.getVehicle() instanceof PartSeat))return;
            var inv = CuriosApi.getCuriosInventory(livingEntity);
            if(inv.isEmpty())return;
            var curios = inv.get().findCurios(InitItems.PLAYER_CHEAT_DEATH_ITEM.get());
            if(curios.isEmpty())return;

            if(event.isMounting() && !(event.getEntityBeingMounted() instanceof PartSeat))
                event.setCanceled(true);
            else if (event.isDismounting() && event.getEntityBeingMounted() instanceof PartSeat)
                event.setCanceled(true);
        }
    }
}

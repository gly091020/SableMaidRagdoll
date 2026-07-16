package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.item.CheatDeathBauble;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = SableMaidRagdoll.MODID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                SableMaidRagdoll.MAID_PART_BLOCK_ENTITY.get(),
                (context) -> new MaidPartRenderer()
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderEntity(RenderLivingEvent.Pre<EntityMaid, ?> event){
        if(!(event.getEntity() instanceof EntityMaid entityMaid))return;
        if(entityMaid.tickCount % 20 == 0 && CheatDeathBauble.isCheatDeath(entityMaid))
            entityMaid.spawnRestoreHealthParticle(entityMaid.getRandom().nextInt(3) + 7);
    }
}

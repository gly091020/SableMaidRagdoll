package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = SableMaidRagdoll.MODID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                SableMaidRagdoll.MAID_PART_BLOCK_ENTITY.get(),
                (context) -> new MaidPartRenderer(context.getEntityRenderer().getItemInHandRenderer())
        );
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        MaidPartRenderCache.clear();
        GeoMaidModelRenderer.clear();
    }
}

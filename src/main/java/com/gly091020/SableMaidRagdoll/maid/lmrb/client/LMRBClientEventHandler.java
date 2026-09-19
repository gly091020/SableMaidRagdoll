package com.gly091020.SableMaidRagdoll.maid.lmrb.client;

import com.gly091020.SableMaidRagdoll.maid.lmrb.client.renderer.LittleMaidPartRenderCache;
import com.gly091020.SableMaidRagdoll.maid.lmrb.client.renderer.LittleMaidPartRenderer;
import com.gly091020.SableMaidRagdoll.maid.lmrb.init.LMRBInitBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@OnlyIn(Dist.CLIENT)
public class LMRBClientEventHandler {
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                LMRBInitBlockEntities.LITTLE_MAID_PART_BLOCK_ENTITY.get(),
                context -> new LittleMaidPartRenderer()
        );
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        LittleMaidPartRenderCache.clear();
    }
}

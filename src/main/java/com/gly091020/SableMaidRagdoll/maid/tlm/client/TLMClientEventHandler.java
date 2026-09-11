package com.gly091020.SableMaidRagdoll.maid.tlm.client;

import com.gly091020.SableMaidRagdoll.client.MobCannonAimManager;
import com.gly091020.SableMaidRagdoll.maid.tlm.client.renderer.MaidFairyPartRenderer;
import com.gly091020.SableMaidRagdoll.maid.tlm.client.renderer.MaidPartRenderCache;
import com.gly091020.SableMaidRagdoll.maid.tlm.client.renderer.MaidPartRenderer;
import com.gly091020.SableMaidRagdoll.maid.tlm.client.screen.EmojiSelectScreen;
import com.gly091020.SableMaidRagdoll.maid.tlm.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import static com.gly091020.SableMaidRagdoll.client.SableMaidRagdollClient.OPEN_EMOJI;

@OnlyIn(Dist.CLIENT)
public class TLMClientEventHandler {
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                TLMInitBlockEntities.MAID_PART_BLOCK_ENTITY.get(),
                (context) -> new MaidPartRenderer(context.getEntityRenderer().getItemInHandRenderer())
        );
        BlockEntityRenderers.register(
                TLMInitBlockEntities.MAID_FAIRY_PART_BLOCK_ENTITY.get(),
                context -> new MaidFairyPartRenderer()
        );
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        GeoMaidModelRenderer.clear();
        MaidPartRenderCache.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        while (OPEN_EMOJI.consumeClick()) {
            if (!MobCannonAimManager.isAiming()) {
                EmojiSelectScreen.tryOpen();
            }
        }
    }
}

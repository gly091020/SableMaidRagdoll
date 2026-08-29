package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableMaidRagdoll.init.InitItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(value = Dist.CLIENT, modid = SableMaidRagdoll.MODID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                InitBlockEntities.MAID_PART_BLOCK_ENTITY.get(),
                (context) -> new MaidPartRenderer(context.getEntityRenderer().getItemInHandRenderer())
        );
        BlockEntityRenderers.register(
                InitBlockEntities.MAID_FAIRY_PART_BLOCK_ENTITY.get(),
                context -> new MaidFairyPartRenderer()
        );
        BlockEntityRenderers.register(
                InitBlockEntities.MAID_DOLL_BLOCK_ENTITY.get(),
                MaidDollRenderer::new
        );
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        MaidPartRenderCache.clear();
        GeoMaidModelRenderer.clear();
    }

    @SubscribeEvent
    public static void onRegistryItemExtension(RegisterClientExtensionsEvent event){
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new PlayerCheatDeathItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().getEntityModels());
            }
        }, InitItems.PLAYER_CHEAT_DEATH_ITEM.get());
    }
}

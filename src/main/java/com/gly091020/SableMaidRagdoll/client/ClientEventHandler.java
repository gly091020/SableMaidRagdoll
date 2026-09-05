package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.client.renderer.block.*;
import com.gly091020.SableMaidRagdoll.client.renderer.item.PlayerCheatDeathItemRenderer;
import com.gly091020.SableMaidRagdoll.client.screen.EmojiSelectScreen;
import com.gly091020.SableMaidRagdoll.client.screen.MobCannonScreen;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableMaidRagdoll.init.InitMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

import static com.gly091020.SableMaidRagdoll.client.SableMaidRagdollClient.AIM_CANNON;
import static com.gly091020.SableMaidRagdoll.client.SableMaidRagdollClient.OPEN_EMOJI;

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
        BlockEntityRenderers.register(
                InitBlockEntities.MOB_CANNON_BLOCK_ENTITY.get(),
                MobCannonRenderer::new
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
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new MobCannonItemRenderer();
            }
        }, InitItems.MOB_CANNON_ITEM.get());
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(InitMenus.MOB_CANNON.get(), MobCannonScreen::new);
    }

    @SubscribeEvent
    public static void onMoveCamera(CalculatePlayerTurnEvent event){
        if (MobCannonAimManager.isAiming()) {
            MobCannonAimManager.onMouseTurn(Minecraft.getInstance());
            event.setMouseSensitivity(-1 / 3f);  // 禁止鼠标转动
        }
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_EMOJI);
        event.register(AIM_CANNON);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        var mc = Minecraft.getInstance();
        if (AIM_CANNON.consumeClick() && !MobCannonAimManager.isAiming()) {
            MobCannonAimManager.tryStart(mc);
        }
        MobCannonAimManager.tick(mc, AIM_CANNON.isDown());
        while (OPEN_EMOJI.consumeClick()) {
            if (!MobCannonAimManager.isAiming()) {
                EmojiSelectScreen.tryOpen();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        MobCannonAimManager.render(event.getGuiGraphics());
    }
}

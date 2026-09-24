package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.client.model.MaidDollDefaultModel;
import com.gly091020.SableMaidRagdoll.client.renderer.block.MaidDollRenderer;
import com.gly091020.SableMaidRagdoll.client.renderer.block.MobCannonItemRenderer;
import com.gly091020.SableMaidRagdoll.client.renderer.block.MobCannonRenderer;
import com.gly091020.SableMaidRagdoll.client.renderer.RagdollWandHandRenderer;
import com.gly091020.SableMaidRagdoll.client.renderer.item.PlayerCheatDeathItemRenderer;
import com.gly091020.SableMaidRagdoll.client.renderer.item.RagdollWandItemRenderer;
import com.gly091020.SableMaidRagdoll.client.screen.MobCannonScreen;
import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableMaidRagdoll.init.InitMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
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
    private static RagdollWandItemRenderer ragdollWandRenderer;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
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
        ragdollWandRenderer = new RagdollWandItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ragdollWandRenderer;
            }
        }, InitItems.RAGDOLL_WAND_ITEM.get());
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null || ragdollWandRenderer == null) return;
        var wand = InitItems.RAGDOLL_WAND_ITEM.get();
        if (player.isUsingItem() && player.getUseItem().is(wand)) {
            // 蓄力时双手都由我们自己画，只在拿着魔杖的那只手上渲染一次
            event.setCanceled(true);
            if (player.getUsedItemHand() != event.getHand()) return;
            var stack = player.getUseItem();
            RagdollWandHandRenderer.render(ragdollWandRenderer, event, stack, player.getMainArm(), true,
                    RagdollWandItemRenderer.getCharge(stack, event.getPartialTick()));
            return;
        }
        // 空闲时原版不会为非空手画手臂，这里只接管拿着魔杖的那只手
        InteractionHand hand = player.getMainHandItem().is(wand) ? InteractionHand.MAIN_HAND
                : player.getOffhandItem().is(wand) ? InteractionHand.OFF_HAND : null;
        if (hand == null || event.getHand() != hand) return;
        event.setCanceled(true);
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        RagdollWandHandRenderer.render(ragdollWandRenderer, event, player.getItemInHand(hand), arm, false, 0.0F);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MaidDollDefaultModel.LAYER_LOCATION, MaidDollDefaultModel::createBodyLayer);
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
        RagdollWandTargetGlow.tick();
        if (AIM_CANNON.consumeClick() && !MobCannonAimManager.isAiming()) {
            MobCannonAimManager.tryStart(mc);
        }
        MobCannonAimManager.tick(mc, AIM_CANNON.isDown());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        MobCannonAimManager.render(event.getGuiGraphics());
    }
}

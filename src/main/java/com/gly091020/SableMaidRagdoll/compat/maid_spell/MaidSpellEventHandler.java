package com.gly091020.SableMaidRagdoll.compat.maid_spell;

import com.github.yimeng261.maidspell.compat.irons_spellbooks.entity.winefox.MagicalWinefoxBossEntity;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

public class MaidSpellEventHandler {
    private static final String TRANS_KEY = "entity.touhou_little_maid_spell.stellar_witch.bossbar";
    private static final ResourceLocation LEFT = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "textures/gui/stellar_witch/left.png");
    private static final ResourceLocation RIGHT = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "textures/gui/stellar_witch/right.png");
    private static final int IMAGE_WIDTH = 100;
    private static final int IMAGE_HEIGHT = 85;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderWineFoxBossBar(CustomizeGuiOverlayEvent.BossEventProgress event){
        if(!(event.getBossEvent().getName().getContents() instanceof TranslatableContents contents &&
                contents.getKey().equals(TRANS_KEY)))return;
        if(!iFoxMode())return;
        var graphics = event.getGuiGraphics();
        int centerX = (int) (graphics.guiWidth() / 2f * (1 / 0.7));
        int originY = Math.max(0, event.getY() - 12) - 40;
        int space = (int) (IMAGE_WIDTH * 1.5);

        graphics.pose().pushPose();
        graphics.pose().scale(0.7f, 0.7f, 1);
        graphics.blit(LEFT, centerX - space, originY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
        graphics.blit(RIGHT, centerX + space - IMAGE_WIDTH, originY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
        graphics.pose().popPose();
    }

    private static boolean iFoxMode() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.isAlive() && minecraft.level != null) {
            double rangeSqr = 9216.0F;
            return !minecraft.level.getEntitiesOfClass(MagicalWinefoxBossEntity.class, minecraft.player.getBoundingBox().inflate(96.0F), (boss) ->
                    !boss.isRemoved() &&
                    boss.isBattleActive() &&
                            boss.getVehicle() instanceof PartSeat &&
                            boss.distanceToSqr(minecraft.player) <= rangeSqr).isEmpty();
        } else {
            return false;
        }
    }
}

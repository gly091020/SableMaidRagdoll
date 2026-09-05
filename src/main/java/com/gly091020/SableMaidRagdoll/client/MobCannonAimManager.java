package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableMaidRagdoll.network.ServerboundMobCannonAimPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public final class MobCannonAimManager {
    private static BlockPos aimPos;
    private static double aimYRot;
    private static double aimXRot;
    private static double baseYRot;
    private static double baseXRot;
    private static double lastSentYRot;
    private static double lastSentXRot;
    private static boolean aiming;
    private static boolean sent;

    private MobCannonAimManager() {
    }

    public static boolean isAiming() {
        return aiming;
    }

    public static boolean isCannonTarget(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null || mc.hitResult == null) return false;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK || !(mc.hitResult instanceof BlockHitResult blockHit)) return false;
        return mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof MobCannonBlockEntity;
    }

    public static boolean tryStart(Minecraft mc) {
        if (aiming) return true;
        if (!isCannonTarget(mc)) return false;
        if(mc.player == null)return false;
        if(mc.level == null)return false;
        if (!(mc.hitResult instanceof BlockHitResult blockHit)) return false;
        if (!(mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof MobCannonBlockEntity blockEntity)) return false;
        aimPos = blockHit.getBlockPos().immutable();
        baseYRot = blockEntity.getYRot();
        baseXRot = blockEntity.getXRot();
        aimYRot = baseYRot;
        aimXRot = Math.clamp(baseXRot, -60, 90);
        aiming = true;
        sent = false;
        send(mc);
        return true;
    }

    public static void onMouseTurn(Minecraft mc) {
        if (!aiming || mc.player == null) return;

        double sensitivity = mc.options.sensitivity().get();
        double factor = Math.pow(sensitivity * 0.6 + 0.2, 3) * 8.0 * 0.15;
        boolean invertY = mc.options.invertYMouse().get();
        double dx = mc.mouseHandler.getXVelocity();
        double dy = mc.mouseHandler.getYVelocity();

        double yRot = Mth.wrapDegrees(aimYRot + dx * factor);
        double xRot = Math.clamp(aimXRot + dy * (invertY ? 1.0 : -1.0) * factor, -60, 90);
        if (mc.player.isShiftKeyDown()) {
            yRot = Math.round(yRot / 5) * 5;
            xRot = Math.round(xRot / 5) * 5;
        }
        aimYRot = yRot;
        aimXRot = xRot;

        if (mc.level != null && mc.level.getBlockEntity(aimPos) instanceof MobCannonBlockEntity blockEntity) {
            if (blockEntity.getYRot() != aimYRot) blockEntity.setYRot(aimYRot);
            if (blockEntity.getXRot() != aimXRot) blockEntity.setXRot(aimXRot);
        }

        boolean changed = !sent || Math.abs(aimYRot - lastSentYRot) > 0.01 || Math.abs(aimXRot - lastSentXRot) > 0.01;
        if (changed) {
            send(mc);
        }
    }

    public static void tick(Minecraft mc, boolean keyDown) {
        if (!aiming) return;
        if (mc.player == null || mc.level == null || mc.screen != null || !keyDown) {
            finish(mc);
            return;
        }
        if (!(mc.level.getBlockEntity(aimPos) instanceof MobCannonBlockEntity)) {
            finish(mc);
            return;
        }
        if (mc.player.distanceToSqr(aimPos.getCenter()) > 64) {
            finish(mc);
        }
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!aiming) return;
        Minecraft mc = Minecraft.getInstance();
        int x = mc.getWindow().getGuiScaledWidth() / 2;
        int y = (int) (mc.getWindow().getGuiScaledHeight() * 0.6);
        guiGraphics.drawCenteredString(mc.font,
                Component.translatable("hud.sablemaidragdoll.mob_cannon_aim.title"), x, y, 0xFFFFFF);
        y += 12;
        guiGraphics.drawCenteredString(mc.font,
                Component.translatable("hud.sablemaidragdoll.mob_cannon_aim.angle",
                        String.format(Locale.ROOT, "%.1f", aimXRot),
                        String.format(Locale.ROOT, "%.1f", aimYRot)), x, y, 0xFFFF55);
        y += 12;
        guiGraphics.drawCenteredString(mc.font,
                Component.translatable("hud.sablemaidragdoll.mob_cannon_aim.snap"), x, y, 0xFFAA00);
        y += 12;
        guiGraphics.drawCenteredString(mc.font,
                Component.translatable("hud.sablemaidragdoll.mob_cannon_aim.release"), x, y, 0xAAAAAA);
    }

    private static void send(Minecraft mc) {
        lastSentYRot = aimYRot;
        lastSentXRot = aimXRot;
        sent = true;
        if (mc.getConnection() != null) {
            PacketDistributor.sendToServer(new ServerboundMobCannonAimPacket(aimPos, aimXRot, aimYRot));
        }
    }

    private static void finish(Minecraft mc) {
        aiming = false;
        send(mc);
        aimPos = null;
        sent = false;
    }
}

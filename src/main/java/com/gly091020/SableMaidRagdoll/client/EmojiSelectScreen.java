package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.touhoulittlemaid.client.resource.listener.EmojiReloadListener;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.network.ServerboundEmojiSelectPacket;
import com.gly091020.SableMaidRagdoll.util.RagdollChatBubbleRenderer;
import com.gly091020.SableMaidRagdoll.util.RagdollEmoji;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * 表情选择界面：玩家以女仆布娃娃形态按下表情键（默认右 Alt）时打开。
 * 点击表情即选择并关闭；点击底部"清除表情"按钮可清除当前表情。
 */
public class EmojiSelectScreen extends Screen {
    private static final int CELL_SIZE = 48;
    private static final int GAP = 8;
    private static final int PADDING = 16;
    private static final int TOP = 38;

    private final List<EmojiReloadListener.EmojiResource> emojis;
    /** 当前选中的表情（高亮用），可能为 null */
    private ResourceLocation currentEmoji;
    /** 滚动偏移（行数） */
    private int scroll = 0;
    private int columns = 8;
    private int visibleRows = 4;
    private int maxScroll = 0;

    public EmojiSelectScreen() {
        super(Component.translatable("screen.sablemaidragdoll.emoji_select.title"));
        this.emojis = RagdollChatBubbleRenderer.getEmojis();
        var player = Minecraft.getInstance().player;
        this.currentEmoji = player == null ? null : RagdollEmoji.getEmoji(player);
        if (this.currentEmoji != null && this.currentEmoji.equals(SableMaidRagdoll.EMPTY_EMOJI)) {
            this.currentEmoji = null;
        }
    }

    public static void tryOpen() {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!RagdollEmoji.isRagdollOfType(mc.player, SableMaidRagdoll.RAGDOLL_TYPE)) {
            return;
        }
        mc.setScreen(new EmojiSelectScreen());
    }

    @Override
    protected void init() {
        this.columns = Math.max(1, (this.width - 2 * PADDING + GAP) / (CELL_SIZE + GAP));
        int areaHeight = this.height - TOP - 52;
        this.visibleRows = Math.max(1, areaHeight / (CELL_SIZE + GAP));
        int totalRows = Mth.positiveCeilDiv(this.emojis.size(), this.columns);
        this.maxScroll = Math.max(0, totalRows - this.visibleRows);
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll);

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.sablemaidragdoll.emoji_select.clear"),
                        button -> selectEmoji(null))
                .bounds(this.width / 2 - 60, this.height - 30, 120, 20)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics p_283688_, int p_296369_, int p_296477_, float p_294317_) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        for (int i = 0; i < this.emojis.size(); i++) {
            int col = i % this.columns;
            int row = i / this.columns - this.scroll;
            int x = PADDING + col * (CELL_SIZE + GAP);
            int y = TOP + row * (CELL_SIZE + GAP);
            if (y + CELL_SIZE < TOP - GAP || y > this.height - 44) continue;

            var emoji = this.emojis.get(i);
            boolean hovered = mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;
            boolean selected = this.currentEmoji != null && this.currentEmoji.equals(emoji.location());

            guiGraphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, hovered ? 0x66FFFFFF : 0x44000000);
            if (selected) {
                guiGraphics.fill(x, y, x + CELL_SIZE, y + 2, 0xFFFFFFFF);
                guiGraphics.fill(x, y + CELL_SIZE - 2, x + CELL_SIZE, y + CELL_SIZE, 0xFFFFFFFF);
                guiGraphics.fill(x, y, x + 2, y + CELL_SIZE, 0xFFFFFFFF);
                guiGraphics.fill(x + CELL_SIZE - 2, y, x + CELL_SIZE, y + CELL_SIZE, 0xFFFFFFFF);
            }

            // 按比例缩放到格子内绘制表情贴图
            int w = emoji.width();
            int h = emoji.height();
            float scale = Math.min((CELL_SIZE - 16F) / w, (CELL_SIZE - 16F) / h);
            int dw = Math.max(1, Math.round(w * scale));
            int dh = Math.max(1, Math.round(h * scale));
            int dx = x + (CELL_SIZE - dw) / 2;
            int dy = y + (CELL_SIZE - dh) / 2;
            if (emoji.isGif()) {
                RagdollChatBubbleRenderer.registerGifImage(emoji.location());
            }
            guiGraphics.blit(emoji.location(), dx, dy, 0, 0, dw, dh, dw, dh);
        }

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("screen.sablemaidragdoll.emoji_select.tip"),
                this.width / 2, this.height - 50, 0x808080);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < this.emojis.size(); i++) {
                int col = i % this.columns;
                int row = i / this.columns - this.scroll;
                int x = PADDING + col * (CELL_SIZE + GAP);
                int y = TOP + row * (CELL_SIZE + GAP);
                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    this.selectEmoji(this.emojis.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScroll > 0) {
            this.scroll = Mth.clamp(this.scroll - (int) Math.signum(verticalAmount), 0, this.maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /** 选择表情（null 表示清除），发给服务端后关闭界面。 */
    private void selectEmoji(EmojiReloadListener.EmojiResource emoji) {
        var mc = Minecraft.getInstance();
        ResourceLocation location = emoji == null ? SableMaidRagdoll.EMPTY_EMOJI : emoji.location();
        // 附件会自动同步到客户端，无需本地再写一份
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new ServerboundEmojiSelectPacket(location)));
        }
        this.onClose();
    }
}

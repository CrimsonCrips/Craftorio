package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DevToolsHelpPanel {

    private static final long ANIM_DURATION_MS = 250L;
    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_MARGIN = 8;
    public static final int BUTTON_SIZE = 20;

    private boolean visible = false;
    private boolean opening = false;
    private long animStartMillis = 0L;
    private float animFromProgress = 0f;

    public Button createButton(int screenWidth, int topY, Runnable onBeforeToggle) {
        return createButton(screenWidth, topY, false, onBeforeToggle);
    }

    public Button createButton(int screenWidth, int topY, boolean alignLeft, Runnable onBeforeToggle) {
        int x = alignLeft ? 6 : screenWidth - BUTTON_SIZE - 6;
        return Button.builder(Component.literal("?"), b -> {
            onBeforeToggle.run();
            toggle();
        }).bounds(x, topY, BUTTON_SIZE, BUTTON_SIZE).build();
    }

    public void toggle() {
        this.animFromProgress = currentProgress();
        this.visible = !this.visible;
        this.opening = this.visible;
        this.animStartMillis = System.currentTimeMillis();
    }

    public void closeIfOpen() {
        if (this.visible) {
            toggle();
        }
    }

    private float currentProgress() {
        long elapsed = System.currentTimeMillis() - this.animStartMillis;
        float t = Mth.clamp(elapsed / (float) ANIM_DURATION_MS, 0f, 1f);
        float eased = easeOutCubic(t);
        float target = this.opening ? 1f : 0f;
        return this.animFromProgress + (target - this.animFromProgress) * eased;
    }

    private static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    public void render(GuiGraphics graphics, Font font, int screenWidth, int panelTopY, List<Component> rawLines) {
        render(graphics, font, screenWidth, panelTopY, false, rawLines);
    }

    public void render(GuiGraphics graphics, Font font, int screenWidth, int panelTopY, boolean alignLeft, List<Component> rawLines) {
        float progress = currentProgress();
        if (progress <= 0f) return;

        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component raw : rawLines) {
            lines.addAll(font.split(raw, PANEL_WIDTH - 12));
        }

        int panelHeight = lines.size() * font.lineHeight + 14;
        int panelX;
        if (alignLeft) {
            panelX = (int) Mth.lerp(progress, -PANEL_WIDTH, PANEL_MARGIN);
        } else {
            int onScreenX = screenWidth - PANEL_WIDTH - PANEL_MARGIN;
            panelX = (int) Mth.lerp(progress, screenWidth, onScreenX);
        }

        graphics.fill(panelX, panelTopY, panelX + PANEL_WIDTH, panelTopY + panelHeight, 0xE0101010);
        graphics.fill(panelX, panelTopY, panelX + PANEL_WIDTH, panelTopY + 1, 0xFFFFFFFF);
        graphics.fill(panelX, panelTopY + panelHeight - 1, panelX + PANEL_WIDTH, panelTopY + panelHeight, 0xFFFFFFFF);
        graphics.fill(panelX, panelTopY, panelX + 1, panelTopY + panelHeight, 0xFFFFFFFF);
        graphics.fill(panelX + PANEL_WIDTH - 1, panelTopY, panelX + PANEL_WIDTH, panelTopY + panelHeight, 0xFFFFFFFF);

        int textY = panelTopY + 7;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, panelX + 6, textY, 0xFFFFFF, false);
            textY += font.lineHeight;
        }
    }
}

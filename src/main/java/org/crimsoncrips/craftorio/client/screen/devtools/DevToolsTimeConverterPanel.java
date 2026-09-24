package org.crimsoncrips.craftorio.client.screen.devtools;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DevToolsTimeConverterPanel {

    private static final long ANIM_DURATION_MS = 250L;
    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_HEIGHT = 52;
    private static final int PANEL_MARGIN = 8;
    public static final int BUTTON_SIZE = 20;

    private boolean visible = false;
    private boolean opening = false;
    private long animStartMillis = 0L;
    private float animFromProgress = 0f;

    private final EditBox hoursBox;
    private final EditBox minutesBox;
    private final Button applyButton;
    private final Font font;

    public DevToolsTimeConverterPanel(Font font, EditBox targetSecondsBox) {
        this.font = font;
        this.hoursBox = new EditBox(font, 0, 0, 64, 16, Component.literal("hours"));
        this.hoursBox.setMaxLength(256);
        this.minutesBox = new EditBox(font, 0, 0, 64, 16, Component.literal("minutes"));
        this.minutesBox.setMaxLength(256);
        this.applyButton = Button.builder(Component.translatable("misc.craftorio.dev_tools_time_convert_apply"), b -> {
            int hours = parseIntSafe(hoursBox.getValue());
            int minutes = parseIntSafe(minutesBox.getValue());
            targetSecondsBox.setValue(String.valueOf(hours * 3600 + minutes * 60));
        }).bounds(0, 0, PANEL_WIDTH - 12, 16).build();
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public List<AbstractWidget> widgets() {
        return List.of(hoursBox, minutesBox, applyButton);
    }

    public Button createToggleButton(int screenWidth, int topY, Runnable onBeforeToggle) {
        return Button.builder(Component.translatable("misc.craftorio.dev_tools_time_convert_button"), b -> {
            onBeforeToggle.run();
            toggle();
        }).bounds(screenWidth - BUTTON_SIZE - 6, topY, BUTTON_SIZE, BUTTON_SIZE).build();
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

    private boolean lastOpen = false;

    public void updateAndRender(GuiGraphics graphics, int screenWidth, int panelTopY) {
        float progress = currentProgress();

        boolean interactable = progress > 0.05f;
        this.lastOpen = interactable;
        for (AbstractWidget widget : widgets()) {
            widget.visible = interactable;
            widget.active = interactable;
        }

        if (progress <= 0f) return;

        int onScreenX = screenWidth - PANEL_WIDTH - PANEL_MARGIN;
        int panelX = (int) Mth.lerp(progress, screenWidth, onScreenX);

        graphics.fill(panelX, panelTopY, panelX + PANEL_WIDTH, panelTopY + PANEL_HEIGHT, 0xE0101010);
        graphics.fill(panelX, panelTopY, panelX + PANEL_WIDTH, panelTopY + 1, 0xFFFFFFFF);
        graphics.fill(panelX, panelTopY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelTopY + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(panelX, panelTopY, panelX + 1, panelTopY + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(panelX + PANEL_WIDTH - 1, panelTopY, panelX + PANEL_WIDTH, panelTopY + PANEL_HEIGHT, 0xFFFFFFFF);

        int boxesY = panelTopY + 7;
        hoursBox.setPosition(panelX + 6, boxesY);
        minutesBox.setPosition(panelX + 6 + 64 + 6, boxesY);
        applyButton.setPosition(panelX + 6, boxesY + 16 + 6);
    }

    public void renderHints(GuiGraphics graphics) {
        if (!lastOpen) return;
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, font, hoursBox, "Hour");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, font, minutesBox, "Minutes");
    }
}

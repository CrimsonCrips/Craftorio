package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.CraftorioToastManager;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ActiveEffectsScreen extends Screen {

    private static final int ROW_HEIGHT = 36;
    private static final int ROW_GAP = 4;
    private static final int ICON_SIZE = 24;
    private static final int ICON_GAP = 8;
    private static final int SIDE_MARGIN = 30;
    private static final int TOP_MARGIN = 32;
    private static final int BOTTOM_MARGIN = 40;
    private static final float SCROLL_SPEED = 20f;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 6;

    private final Screen parent;
    private final List<CraftorioEffects> effects = new ArrayList<>();

    private int viewportLeft;
    private int viewportRight;
    private int viewportTop;
    private int viewportBottom;
    private float scrollY = 0f;
    private int maxScroll = 0;

    public ActiveEffectsScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.active_effects_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Player player = this.minecraft.player;
        this.effects.clear();
        if (player != null) {
            this.effects.addAll(CraftorioMisc.getCraftorioEffects(player));
        }

        this.viewportLeft = SIDE_MARGIN;
        this.viewportRight = this.width - SIDE_MARGIN;
        this.viewportTop = TOP_MARGIN;
        this.viewportBottom = this.height - BOTTOM_MARGIN;

        int viewportHeight = Math.max(0, this.viewportBottom - this.viewportTop);
        int contentHeight = this.effects.isEmpty() ? 0 : this.effects.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        this.maxScroll = Math.max(0, contentHeight - viewportHeight);
        this.scrollY = Mth.clamp(this.scrollY, 0, this.maxScroll);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, 12, 0xFFFFFF);

        if (this.effects.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.no_active_effects"),
                    this.width / 2, (this.viewportTop + this.viewportBottom) / 2, 0xFFFFFF);
            return;
        }

        graphics.enableScissor(this.viewportLeft, this.viewportTop, this.viewportRight, this.viewportBottom);

        int y = this.viewportTop - Math.round(this.scrollY);
        for (CraftorioEffects effect : this.effects) {
            renderRow(graphics, effect, y, mouseX, mouseY);
            y += ROW_HEIGHT + ROW_GAP;
        }

        graphics.disableScissor();

        if (this.maxScroll > 0) {
            renderScrollbar(graphics);
        }
    }

    private void renderRow(GuiGraphics graphics, CraftorioEffects effect, int top, int mouseX, int mouseY) {
        if (top + ROW_HEIGHT < this.viewportTop || top > this.viewportBottom) return;

        int left = this.viewportLeft;
        int width = this.viewportRight - this.viewportLeft;

        graphics.blitSprite(CraftorioToastManager.DEFAULT_SPRITE, left, top, width, ROW_HEIGHT);

        boolean hovered = mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + ROW_HEIGHT
                && mouseY >= this.viewportTop && mouseY <= this.viewportBottom;
        if (hovered) {
            graphics.fill(left, top, left + width, top + ROW_HEIGHT, 0x30FFFFFF);
        }

        int iconX = left + ICON_GAP;
        int iconY = top + (ROW_HEIGHT - ICON_SIZE) / 2;
        if (effect.getIcon() != null) {
            graphics.blit(effect.getIcon(), iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        int textX = iconX + ICON_SIZE + ICON_GAP;

        graphics.drawString(this.font, effect.getActualName(), textX, top + 7, ClientEvents.activeEffectColor(effect), true);

        String duration = Component.translatable("misc.craftorio.contract_time_remaining",
                CraftorioMisc.ticksToTimeString(effect.getTime())).getString();
        graphics.drawString(this.font, duration, textX, top + ROW_HEIGHT - this.font.lineHeight - 6, 0xAAAAAA, true);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int trackX = this.viewportRight + SCROLLBAR_GAP;
        int trackHeight = this.viewportBottom - this.viewportTop;
        graphics.fill(trackX, this.viewportTop, trackX + SCROLLBAR_WIDTH, this.viewportBottom, 0x40FFFFFF);

        int contentHeight = trackHeight + this.maxScroll;
        int barHeight = Mth.clamp(trackHeight * trackHeight / contentHeight, 20, trackHeight);
        int barY = this.viewportTop + Math.round(this.scrollY / this.maxScroll * (trackHeight - barHeight));
        graphics.fill(trackX, barY, trackX + SCROLLBAR_WIDTH, barY + barHeight, 0xA0FFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        this.scrollY = Mth.clamp(this.scrollY - (float) scrollY * SCROLL_SPEED, 0, this.maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;

        if (mouseX < this.viewportLeft || mouseX > this.viewportRight || mouseY < this.viewportTop || mouseY > this.viewportBottom) {
            return false;
        }

        int y = this.viewportTop - Math.round(this.scrollY);
        for (CraftorioEffects effect : this.effects) {
            if (mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                this.minecraft.setScreen(new EffectDetailsScreen(this, effect));
                return true;
            }
            y += ROW_HEIGHT + ROW_GAP;
        }

        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

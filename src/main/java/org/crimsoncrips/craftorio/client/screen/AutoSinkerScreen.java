package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.inventory.AutoSinkerMenu;
import org.crimsoncrips.craftorio.networking.SetAutoSinkerOwnerPacket;
import org.crimsoncrips.craftorio.networking.SetAutoSinkerThresholdPacket;

@OnlyIn(Dist.CLIENT)
public class AutoSinkerScreen extends AbstractContainerScreen<AutoSinkerMenu> {

    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 14;
    private static final int SLIDER_WIDTH = 120;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SMALL_BUTTON_WIDTH = 20;
    private static final int SMALL_BUTTON_HEIGHT = 20;

    private ThresholdSlider slider;
    private boolean userAdjustedThreshold = false;

    public AutoSinkerScreen(AutoSinkerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 150;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int centerX = this.leftPos + this.imageWidth / 2;
        int sliderY = this.topPos + 76;

        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> nudge(-1))
                .bounds(centerX - SLIDER_WIDTH / 2 - SMALL_BUTTON_WIDTH - 2, sliderY, SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT).build());

        this.slider = new ThresholdSlider(centerX - SLIDER_WIDTH / 2, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, this.menu.getThresholdPercent());
        this.addRenderableWidget(this.slider);

        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> nudge(1))
                .bounds(centerX + SLIDER_WIDTH / 2 + 2, sliderY, SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.set_owner_button"), b -> setOwner())
                .bounds(centerX - 50, this.topPos + 100, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - 50, this.topPos + this.imageHeight - 26, 100, 20).build());
    }

    private void setOwner() {
        PacketDistributor.sendToServer(new SetAutoSinkerOwnerPacket());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!userAdjustedThreshold) {
            this.slider.syncFromServer(this.menu.getThresholdPercent());
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.slider != null && this.slider.isFocused() && this.slider.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void nudge(int delta) {
        int newPercent = Mth.clamp(this.slider.getPercent() + delta, 1, 100);
        this.slider.setPercent(newPercent);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xE0202020);
        graphics.renderOutline(x, y, this.imageWidth, this.imageHeight, 0xFF808080);

        int barX = x + (this.imageWidth - BAR_WIDTH) / 2;
        int barY = y + 34;

        int fillPercent = this.menu.getFillPercent();
        int filledWidth = BAR_WIDTH * fillPercent / 100;

        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF404040);
        graphics.fill(barX, barY, barX + filledWidth, barY + BAR_HEIGHT, fillColor(fillPercent));
        graphics.renderOutline(barX, barY, BAR_WIDTH, BAR_HEIGHT, 0xFFFFFFFF);
    }

    private static int fillColor(int fillPercent) {
        if (fillPercent >= 90) return 0xFFE05050;
        if (fillPercent >= 60) return 0xFFE0C050;
        return 0xFF50C050;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.leftPos + this.imageWidth / 2;

        graphics.drawCenteredString(this.font, this.getTitle(), centerX, this.topPos + 8, 0xFFFFFF);

        String fillLabel = "Fill: " + this.menu.getFillPercent() + "/100 stacks";
        graphics.drawCenteredString(this.font, fillLabel, centerX, this.topPos + 22, 0xFFFFFF);

        String thresholdLabel = "Auto-sink at: " + this.slider.getPercent() + " stack(s)";
        graphics.drawCenteredString(this.font, thresholdLabel, centerX, this.topPos + 60, 0xFFFFFF);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class ThresholdSlider extends AbstractSliderButton {
        private int lastSentPercent;

        ThresholdSlider(int x, int y, int width, int height, int initialPercent) {
            super(x, y, width, height, Component.empty(), Mth.clamp(initialPercent, 1, 100) / 100.0);
            this.lastSentPercent = getPercent();
            updateMessage();
        }

        int getPercent() {
            return Mth.clamp((int) Math.round(this.value * 100.0), 1, 100);
        }

        void setPercent(int percent) {
            AutoSinkerScreen.this.userAdjustedThreshold = true;
            this.value = Mth.clamp(percent, 1, 100) / 100.0;
            updateMessage();
            applyValue();
        }

        void syncFromServer(int percent) {
            int clamped = Mth.clamp(percent, 1, 100);
            if (clamped == getPercent()) return;
            this.value = clamped / 100.0;
            this.lastSentPercent = clamped;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.empty());
        }

        @Override
        protected void applyValue() {
            AutoSinkerScreen.this.userAdjustedThreshold = true;
            int percent = getPercent();
            if (percent == lastSentPercent) return;
            lastSentPercent = percent;
            PacketDistributor.sendToServer(new SetAutoSinkerThresholdPacket(percent));
        }
    }
}

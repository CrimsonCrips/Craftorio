package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.crimsoncrips.craftorio.inventory.AutoValueCondenserMenu;

@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class AutoValueCondenserScreen extends AbstractContainerScreen<AutoValueCondenserMenu> {

    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 14;

    public AutoValueCondenserScreen(AutoValueCondenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 176;
        this.inventoryLabelY = 82;
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

        for (Slot slot : this.menu.slots) {
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF404040);
            graphics.renderOutline(slotX, slotY, 18, 18, 0xFF808080);
        }
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

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

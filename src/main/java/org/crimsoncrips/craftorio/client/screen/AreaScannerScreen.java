package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.inventory.AreaScannerMenu;
import org.crimsoncrips.craftorio.networking.ScanAreaPacket;
import org.crimsoncrips.craftorio.networking.SetAreaScannerRegionPacket;

@OnlyIn(Dist.CLIENT)
public class AreaScannerScreen extends AbstractContainerScreen<AreaScannerMenu> {

    private static final int FIELD_WIDTH = 56;
    private static final int FIELD_HEIGHT = 16;
    private static final int FIELD_GAP = 4;

    private EditBox offsetXBox;
    private EditBox offsetYBox;
    private EditBox offsetZBox;
    private EditBox sizeXBox;
    private EditBox sizeYBox;
    private EditBox sizeZBox;

    public AreaScannerScreen(AreaScannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 150;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int fieldsStartX = this.leftPos + (this.imageWidth - (FIELD_WIDTH * 3 + FIELD_GAP * 2)) / 2;

        int offsetY = this.topPos + 36;
        this.offsetXBox = addNumberField(fieldsStartX, offsetY, true, this.menu.getOffsetX());
        this.offsetYBox = addNumberField(fieldsStartX + FIELD_WIDTH + FIELD_GAP, offsetY, true, this.menu.getOffsetY());
        this.offsetZBox = addNumberField(fieldsStartX + (FIELD_WIDTH + FIELD_GAP) * 2, offsetY, true, this.menu.getOffsetZ());

        int sizeY = this.topPos + 76;
        this.sizeXBox = addNumberField(fieldsStartX, sizeY, false, this.menu.getSizeX());
        this.sizeYBox = addNumberField(fieldsStartX + FIELD_WIDTH + FIELD_GAP, sizeY, false, this.menu.getSizeY());
        this.sizeZBox = addNumberField(fieldsStartX + (FIELD_WIDTH + FIELD_GAP) * 2, sizeY, false, this.menu.getSizeZ());

        int centerX = this.leftPos + this.imageWidth / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.scan_area_button"), b -> scan())
                .bounds(centerX - 50, this.topPos + 98, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> {
            updateRegion();
            this.onClose();
        }).bounds(centerX - 50, this.topPos + 124, 100, 20).build());
    }

    private EditBox addNumberField(int x, int y, boolean allowNegative, int initialValue) {
        EditBox box = new EditBox(this.font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
        box.setMaxLength(6);
        if (allowNegative) {
            box.setFilter(s -> s.isEmpty() || s.equals("-") || s.matches("-?\\d*"));
        } else {
            box.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        }
        box.setValue(String.valueOf(initialValue));
        this.addRenderableWidget(box);
        return box;
    }

    private int readField(EditBox box, int fallback) {
        try {
            return Integer.parseInt(box.getValue().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void updateRegion() {
        int offsetX = readField(this.offsetXBox, this.menu.getOffsetX());
        int offsetY = readField(this.offsetYBox, this.menu.getOffsetY());
        int offsetZ = readField(this.offsetZBox, this.menu.getOffsetZ());
        int sizeX = Math.max(1, readField(this.sizeXBox, this.menu.getSizeX()));
        int sizeY = Math.max(1, readField(this.sizeYBox, this.menu.getSizeY()));
        int sizeZ = Math.max(1, readField(this.sizeZBox, this.menu.getSizeZ()));

        PacketDistributor.sendToServer(new SetAreaScannerRegionPacket(offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ));
    }

    private void scan() {
        PacketDistributor.sendToServer(new ScanAreaPacket());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xE0202020);
        graphics.renderOutline(x, y, this.imageWidth, this.imageHeight, 0xFF808080);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.leftPos + this.imageWidth / 2;

        graphics.drawCenteredString(this.font, this.getTitle(), centerX, this.topPos + 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.scan_offset_label"), centerX, this.topPos + 24, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.scan_size_label"), centerX, this.topPos + 64, 0xFFFFFF);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

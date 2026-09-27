package org.crimsoncrips.craftorio.client.screen.devtools;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.IntConsumer;

@OnlyIn(Dist.CLIENT)
public class DevToolsDropdown extends AbstractWidget {

    private static final int ROW_HEIGHT = 13;
    private static final int PADDING = 6;
    private static final int SCREEN_MARGIN = 3;

    private static DevToolsDropdown open;

    private final Font font;
    private final List<Component> options;
    private final IntConsumer onChange;
    private int selected;
    private boolean expanded;

    private int listX;
    private int listY;
    private int columnWidth;
    private int rowsPerColumn;

    public DevToolsDropdown(Font font, int x, int y, int width, int height, List<Component> options, int selected, IntConsumer onChange) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.options = options;
        this.selected = Math.max(0, Math.min(selected, options.size() - 1));
        this.onChange = onChange;
    }

    public void setSelected(int index) {
        this.selected = Math.max(0, Math.min(index, this.options.size() - 1));
    }

    public int getSelected() {
        return this.selected;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void collapse() {
        this.expanded = false;
        if (open == this) {
            open = null;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.expanded) {
            collapse();
            return;
        }
        if (open != null) {
            open.expanded = false;
        }
        this.expanded = true;
        open = this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused() || this.expanded;
        int fill = this.active ? (hovered ? 0xFF6F6F6F : 0xFF4A4A4A) : 0xFF2A2A2A;
        graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, fill);
        graphics.renderOutline(getX(), getY(), this.width, this.height, this.expanded ? 0xFFFFFFFF : 0xFF000000);

        String label = this.font.plainSubstrByWidth(this.options.get(this.selected).getString(), this.width - PADDING * 2 - 8);
        graphics.drawString(this.font, label, getX() + PADDING, getY() + (this.height - this.font.lineHeight) / 2 + 1, this.active ? 0xFFFFFF : 0x808080, true);
        graphics.drawString(this.font, this.expanded ? "^" : "v", getX() + this.width - PADDING - 4, getY() + (this.height - this.font.lineHeight) / 2 + 1, 0xE0E0E0, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    private void layout(int screenWidth, int screenHeight) {
        int widest = 0;
        for (Component option : this.options) {
            widest = Math.max(widest, this.font.width(option.getString()));
        }
        this.columnWidth = Math.max(this.width, widest + PADDING * 2);

        int below = screenHeight - (getY() + this.height) - SCREEN_MARGIN;
        int above = getY() - SCREEN_MARGIN;
        boolean placeBelow = below >= this.options.size() * ROW_HEIGHT || below >= above;
        int available = Math.max(ROW_HEIGHT, placeBelow ? below : above);

        this.rowsPerColumn = Math.max(1, Math.min(this.options.size(), available / ROW_HEIGHT));
        int columns = (this.options.size() + this.rowsPerColumn - 1) / this.rowsPerColumn;
        int totalWidth = columns * this.columnWidth;
        int totalHeight = this.rowsPerColumn * ROW_HEIGHT;

        this.listX = Math.max(SCREEN_MARGIN, Math.min(getX(), screenWidth - totalWidth - SCREEN_MARGIN));
        this.listY = placeBelow ? getY() + this.height : getY() - totalHeight;
    }

    public void renderExpanded(GuiGraphics graphics, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!this.expanded) return;

        layout(screenWidth, screenHeight);
        int columns = (this.options.size() + this.rowsPerColumn - 1) / this.rowsPerColumn;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.fill(this.listX - 1, this.listY - 1, this.listX + columns * this.columnWidth + 1, this.listY + this.rowsPerColumn * ROW_HEIGHT + 1, 0xFF000000);
        graphics.fill(this.listX, this.listY, this.listX + columns * this.columnWidth, this.listY + this.rowsPerColumn * ROW_HEIGHT, 0xF0202020);

        for (int i = 0; i < this.options.size(); i++) {
            int column = i / this.rowsPerColumn;
            int row = i % this.rowsPerColumn;
            int x = this.listX + column * this.columnWidth;
            int y = this.listY + row * ROW_HEIGHT;

            boolean hovered = mouseX >= x && mouseX < x + this.columnWidth && mouseY >= y && mouseY < y + ROW_HEIGHT;
            if (i == this.selected) {
                graphics.fill(x, y, x + this.columnWidth, y + ROW_HEIGHT, 0x60FFFF55);
            }
            if (hovered) {
                graphics.fill(x, y, x + this.columnWidth, y + ROW_HEIGHT, 0x50FFFFFF);
            }
            graphics.drawString(this.font, this.options.get(i), x + PADDING, y + 3, hovered ? 0xFFFFFF : 0xD0D0D0, false);
        }
        graphics.pose().popPose();
    }

    private boolean handleExpandedClick(double mouseX, double mouseY) {
        for (int i = 0; i < this.options.size(); i++) {
            int column = i / this.rowsPerColumn;
            int row = i % this.rowsPerColumn;
            int x = this.listX + column * this.columnWidth;
            int y = this.listY + row * ROW_HEIGHT;
            if (mouseX >= x && mouseX < x + this.columnWidth && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                if (i != this.selected) {
                    this.selected = i;
                    this.onChange.accept(i);
                }
                break;
            }
        }
        collapse();
        return true;
    }

    public static boolean handleClicks(List<DevToolsDropdown> dropdowns, double mouseX, double mouseY) {
        for (DevToolsDropdown dropdown : dropdowns) {
            if (dropdown.expanded && dropdown.visible) {
                return dropdown.handleExpandedClick(mouseX, mouseY);
            }
        }
        return false;
    }

    public static void renderAll(List<DevToolsDropdown> dropdowns, GuiGraphics graphics, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        for (DevToolsDropdown dropdown : dropdowns) {
            if (dropdown.visible) {
                dropdown.renderExpanded(graphics, mouseX, mouseY, screenWidth, screenHeight);
            } else if (dropdown.expanded) {
                dropdown.collapse();
            }
        }
    }
}

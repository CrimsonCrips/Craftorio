package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;

import java.util.Comparator;
import java.util.List;

public class AllContractsScreen extends Screen {

    private static final int TOP_MARGIN = 40;
    private static final int BOTTOM_MARGIN = 40;
    private static final int SIDE_MARGIN = 40;
    private static final int ROW_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 4;

    private final Screen parent;
    private List<CraftorioContract> contracts = List.of();

    private int scroll = 0;
    private boolean draggingScrollbar = false;
    private double dragStartMouseY = 0;
    private int dragStartScroll = 0;

    public AllContractsScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.all_contracts_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.minecraft.level != null) {
            List<Holder.Reference<CraftorioContract>> all = List.copyOf(CraftorioMisc.getAllContracts(this.minecraft.level.registryAccess()));
            this.contracts = all.stream()
                    .map(Holder.Reference::value)
                    .sorted(Comparator.comparing(CraftorioContract::getMinPointThreshold))
                    .toList();
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private int listTop() { return TOP_MARGIN; }

    private int listBottom() { return this.height - BOTTOM_MARGIN; }

    private int listLeft() { return SIDE_MARGIN; }

    private int listRight() { return this.width - SIDE_MARGIN; }

    private int maxScroll() {
        return Math.max(0, this.contracts.size() * ROW_HEIGHT - (listBottom() - listTop()));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, 16, 0xFFFFFF);

        if (this.contracts.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.no_contracts"), this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        int top = listTop();
        int bottom = listBottom();
        int left = listLeft();
        int right = listRight();

        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());

        graphics.enableScissor(left, top, right, bottom);

        int y = top - this.scroll;
        for (CraftorioContract contract : this.contracts) {
            if (y + ROW_HEIGHT >= top && y <= bottom) {
                boolean hovered = mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT;
                if (hovered) {
                    graphics.fill(left, y, right, y + ROW_HEIGHT, 0x40FFFFFF);
                }

                String name = contract.getActualName();
                String threshold = CraftorioMisc.bigIntFormat(contract.getMinPointThreshold(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());

                graphics.drawString(this.font, name, left + 4, y + (ROW_HEIGHT - this.font.lineHeight) / 2, 0xFFFFFF, false);
                String thresholdText = threshold;
                graphics.drawString(this.font, thresholdText, right - 4 - this.font.width(thresholdText), y + (ROW_HEIGHT - this.font.lineHeight) / 2, 0xAAAAAA, false);
            }
            y += ROW_HEIGHT;
        }

        graphics.disableScissor();
        renderScrollbar(graphics, right + 2, top, bottom);
    }

    private void renderScrollbar(GuiGraphics graphics, int x, int top, int bottom) {
        int contentHeight = this.contracts.size() * ROW_HEIGHT;
        int trackHeight = bottom - top;
        if (contentHeight <= trackHeight) return;

        int thumbHeight = Math.max(10, trackHeight * trackHeight / contentHeight);
        int maxScroll = maxScroll();
        int thumbY = top + (maxScroll == 0 ? 0 : this.scroll * (trackHeight - thumbHeight) / maxScroll);

        graphics.fill(x, top, x + SCROLLBAR_WIDTH, bottom, 0x40FFFFFF);
        graphics.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        this.scroll = Mth.clamp(this.scroll - (int) Math.round(scrollY * ROW_HEIGHT), 0, maxScroll());
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int top = listTop();
            int bottom = listBottom();
            int contentHeight = this.contracts.size() * ROW_HEIGHT;
            int trackHeight = bottom - top;

            if (contentHeight > trackHeight) {
                int thumbHeight = Math.max(10, trackHeight * trackHeight / contentHeight);
                int maxScroll = maxScroll();
                int thumbY = top + (maxScroll == 0 ? 0 : this.scroll * (trackHeight - thumbHeight) / maxScroll);
                int scrollbarX = listRight() + 2;

                if (mouseX >= scrollbarX - 1 && mouseX < scrollbarX + SCROLLBAR_WIDTH + 1 && mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
                    this.draggingScrollbar = true;
                    this.dragStartMouseY = mouseY;
                    this.dragStartScroll = this.scroll;
                    return true;
                }
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0 && mouseX >= listLeft() && mouseX < listRight() && mouseY >= listTop() && mouseY < listBottom()) {
            int index = (int) ((mouseY - listTop() + this.scroll) / ROW_HEIGHT);
            if (index >= 0 && index < this.contracts.size()) {
                this.minecraft.setScreen(new ContractDetailsScreen(this, this.contracts.get(index)));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            int top = listTop();
            int bottom = listBottom();
            int trackHeight = bottom - top;
            int contentHeight = this.contracts.size() * ROW_HEIGHT;
            int maxScroll = maxScroll();
            int thumbHeight = Math.max(10, trackHeight * trackHeight / Math.max(1, contentHeight));
            int scrollRange = trackHeight - thumbHeight;

            if (maxScroll > 0 && scrollRange > 0) {
                double deltaMouseY = mouseY - this.dragStartMouseY;
                int newScroll = this.dragStartScroll + (int) Math.round(deltaMouseY * maxScroll / (double) scrollRange);
                this.scroll = Mth.clamp(newScroll, 0, maxScroll);
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

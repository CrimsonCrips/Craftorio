package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class ValueBrowserScreen extends Screen {

    private static final int COLS = 9;
    private static final int ROWS = 16;
    private static final int PAGE_SIZE = COLS * ROWS;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 4;
    private static final int CELL_SIZE = SLOT_SIZE + SLOT_GAP;
    private static final int GRID_TOP = 50;

    private boolean sortByValuable = true;
    private EditBox searchBox;
    private String searchQuery = "";
    private List<Item> filtered = List.of();
    private int page = 0;

    public ValueBrowserScreen() {
        super(Component.literal("Item Values"));
    }

    private List<Item> buildCatalog() {
        Player player = this.minecraft.player;
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            items.add(item);
        }

        Comparator<Item> byValue = Comparator.comparing(item -> CraftorioMisc.checkValue(new ItemStack(item), player, false));
        items.sort(sortByValuable ? byValue.reversed() : byValue);
        return items;
    }

    private void updateFiltered() {
        String query = this.searchQuery.strip().toLowerCase(Locale.ROOT);
        List<Item> catalog = buildCatalog();

        if (query.isEmpty()) {
            this.filtered = catalog;
            return;
        }

        List<Item> matches = new ArrayList<>();
        for (Item item : catalog) {
            String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (name.contains(query) || path.contains(query)) {
                matches.add(item);
            }
        }
        this.filtered = matches;
    }

    private int totalPages() {
        return Math.max(1, (this.filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    @Override
    protected void init() {
        super.init();

        this.updateFiltered();

        this.searchBox = new EditBox(this.font, this.width / 2 - 70, 28, 140, 16, Component.literal("Search"));
        this.searchBox.setValue(this.searchQuery);
        this.searchBox.setResponder(this::onSearchChanged);

        this.refreshWidgets();
    }

    private void onSearchChanged(String value) {
        this.searchQuery = value;
        this.page = 0;
        this.updateFiltered();
        this.refreshWidgets();
    }

    private void toggleSort() {
        this.sortByValuable = !this.sortByValuable;
        this.page = 0;
        this.updateFiltered();
        this.refreshWidgets();
    }

    private void refreshWidgets() {
        this.clearWidgets();

        this.addRenderableWidget(this.searchBox);

        int gridWidth = COLS * CELL_SIZE - SLOT_GAP;
        int startX = (this.width - gridWidth) / 2;

        int firstIndex = this.page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int index = firstIndex + i;
            if (index >= this.filtered.size()) break;

            int col = i % COLS;
            int row = i / COLS;

            this.addRenderableWidget(new ValueEntryButton(
                    startX + col * CELL_SIZE,
                    GRID_TOP + row * CELL_SIZE,
                    this.filtered.get(index)
            ));
        }

        int footerY = GRID_TOP + ROWS * CELL_SIZE + 8;
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(Component.literal("<<"), b -> this.setPage(0))
                .bounds(centerX - 90, footerY, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.setPage(this.page - 1))
                .bounds(centerX - 54, footerY, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.setPage(this.page + 1))
                .bounds(centerX + 20, footerY, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">>"), b -> this.setPage(this.totalPages() - 1))
                .bounds(centerX + 56, footerY, 34, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.literal(this.sortByValuable ? "Sorted: Most Valuable" : "Sorted: Most Valueless"),
                        b -> this.toggleSort())
                .bounds(centerX - 70, 8, 140, 16).build());
    }

    private void setPage(int newPage) {
        this.page = Mth.clamp(newPage, 0, this.totalPages() - 1);
        this.refreshWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int footerY = GRID_TOP + ROWS * CELL_SIZE + 8;
        int centerX = this.width / 2;
        guiGraphics.drawCenteredString(this.font, Component.literal((this.page + 1) + " / " + this.totalPages()), centerX, footerY + 6, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class ValueEntryButton extends AbstractButton {

        private final ItemStack displayStack;

        ValueEntryButton(int x, int y, Item item) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, CommonComponents.EMPTY);
            this.displayStack = new ItemStack(item);

            BigInteger value = CraftorioMisc.checkValue(this.displayStack, ValueBrowserScreen.this.minecraft.player, false);
            this.setTooltip(Tooltip.create(CraftorioMisc.CraftorioTextEffects.capAwareLine(
                    this.displayStack.getHoverName().getString() + " - ", value
            )));
        }

        @Override
        public void onPress() {
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.isHovered()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x80FFFFFF);
            }

            guiGraphics.renderItem(this.displayStack, this.getX() + 1, this.getY() + 1);
            guiGraphics.renderItemDecorations(ValueBrowserScreen.this.font, this.displayStack, this.getX() + 1, this.getY() + 1);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioShop;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;


@OnlyIn(Dist.CLIENT)
public class ShopScreen extends Screen {

    private static final ResourceLocation LOCKED_TEXTURE = Craftorio.getGuiTexture("locked.png");

    private static final int COLS = 9;
    private static final int ROWS = 16;
    private static final int PAGE_SIZE = COLS * ROWS;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 4;
    private static final int CELL_SIZE = SLOT_SIZE + SLOT_GAP;
    private static final int GRID_TOP = 50;


    private static List<Item> catalog;

    private final boolean allUnlocked;
    private final Set<ResourceLocation> unlockedItems;

    private EditBox searchBox;
    private String searchQuery = "";
    private List<Item> filtered = List.of();
    private int page = 0;

    public ShopScreen(boolean allUnlocked, Set<ResourceLocation> unlockedItems) {
        super(Component.literal("Shop"));
        this.allUnlocked = allUnlocked;
        this.unlockedItems = unlockedItems;
    }

    private boolean isUnlocked(Item item) {
        return this.allUnlocked || this.unlockedItems.contains(BuiltInRegistries.ITEM.getKey(item));
    }

    private static List<Item> getCatalog(Player player) {
        if (catalog == null) {
            List<Item> items = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                if (CraftorioShop.getUnitPrice(player, item,true).signum() <= 0) continue;
                items.add(item);
            }
            items.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
            catalog = items;
        }
        return catalog;
    }

    private void updateFiltered() {
        String query = this.searchQuery.strip().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            this.filtered = getCatalog(this.minecraft.player);
            return;
        }

        List<Item> matches = new ArrayList<>();
        for (Item item : getCatalog(this.minecraft.player)) {
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
        this.updateFiltered();
        this.page = 0;
        this.refreshWidgets();
    }

    private void refreshWidgets() {
        this.clearWidgets();

        // clearWidgets() drops the search box too - re-add it every rebuild
        // so it keeps working and doesn't lose focus/cursor state.
        this.addRenderableWidget(this.searchBox);

        int gridWidth = COLS * CELL_SIZE - SLOT_GAP;
        int startX = (this.width - gridWidth) / 2;

        int firstIndex = this.page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int index = firstIndex + i;
            if (index >= this.filtered.size()) break;

            int col = i % COLS;
            int row = i / COLS;

            this.addRenderableWidget(new ShopItemButton(
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
    }

    private void setPage(int newPage) {
        this.page = Mth.clamp(newPage, 0, this.totalPages() - 1);
        this.refreshWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        guiGraphics.drawCenteredString(this.font, this.getTitle(), centerX, 8, 0xFFFFFF);

        BigInteger points = CraftorioMisc.getPoints(this.minecraft.player);
        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, 20, true, 0xFFAA00, points, " points");

        int footerY = GRID_TOP + ROWS * CELL_SIZE + 8;
        guiGraphics.drawCenteredString(this.font, Component.literal((this.page + 1) + " / " + this.totalPages()), centerX, footerY + 6, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class ShopItemButton extends AbstractButton {

        private final Item item;
        private final ItemStack displayStack;
        private final boolean locked;

        ShopItemButton(int x, int y, Item item) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, CommonComponents.EMPTY);
            this.item = item;
            this.displayStack = new ItemStack(item);
            this.locked = !ShopScreen.this.isUnlocked(item);
            Player player = ShopScreen.this.minecraft.player;

            BigInteger unmodified_price = CraftorioShop.getUnitPrice(player, item,false);
            BigInteger price = CraftorioShop.getUnitPrice(player, item,true);
            double shop_multiplier = Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt();
            for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
                shop_multiplier += shopEffect.getMultiplier();
            }

            MutableComponent tooltipComponent = CraftorioMisc.CraftorioTextEffects.capAwareLine(
                    this.displayStack.getHoverName().getString() + " - ", price, " (", unmodified_price, " * " + shop_multiplier + ")"
            );
            if (this.locked) {
                tooltipComponent.append(Component.literal(" (Locked)"));
            }
            this.setTooltip(Tooltip.create(tooltipComponent));
        }

        @Override
        public void onPress() {
            // Locked items just refuse to do anything - no screen change, no packet.
            if (this.locked) return;

            ShopScreen.this.minecraft.setScreen(new ShopPurchaseScreen(this.item, ShopScreen.this));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.isHovered()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x80FFFFFF);
            }



            guiGraphics.renderItem(this.displayStack, this.getX() + 1, this.getY() + 1);
            guiGraphics.renderItemDecorations(ShopScreen.this.font, this.displayStack, this.getX() + 1, this.getY() + 1);

            if (this.locked) {
                int lockSize = SLOT_SIZE - 10;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                guiGraphics.blit(LOCKED_TEXTURE, this.getX(), this.getY(), 0.0F, 0.0F, lockSize, lockSize, lockSize, lockSize);
                guiGraphics.pose().popPose();
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }

}

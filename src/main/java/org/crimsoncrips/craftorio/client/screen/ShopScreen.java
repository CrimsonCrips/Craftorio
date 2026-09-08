package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioShop;
import org.crimsoncrips.craftorio.server.CraftorioShopCatalog;
import org.crimsoncrips.craftorio.server.CraftorioShopCatalog.CatalogEntry;

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


    private static List<CatalogEntry> catalog;

    private final boolean allUnlocked;
    private final Set<ResourceLocation> unlockedItems;

    private EditBox searchBox;
    private String searchQuery = "";
    private List<CatalogEntry> filtered = List.of();
    private int page = 0;

    public ShopScreen(boolean allUnlocked, Set<ResourceLocation> unlockedItems) {
        super(Component.literal("Shop"));
        this.allUnlocked = allUnlocked;
        this.unlockedItems = unlockedItems;
    }

    private boolean isUnlocked(CatalogEntry entry) {
        // Enchanted books and potions don't have a natural "pick it up first"
        // moment the way regular items do, so they're always available.
        if (CraftorioShopCatalog.isVariantKey(entry.key())) {
            return true;
        }
        return this.allUnlocked || this.unlockedItems.contains(entry.key());
    }

    private static List<CatalogEntry> getCatalog(Player player) {
        if (catalog == null) {
            List<CatalogEntry> entries = new ArrayList<>(CraftorioShopCatalog.buildFullCatalog(player.registryAccess()));
            entries.removeIf(entry -> CraftorioShop.getUnitPrice(player, entry.stack(), true).signum() <= 0);
            entries.sort(Comparator.comparing(entry -> entry.key().toString()));
            catalog = entries;
        }
        return catalog;
    }

    private void updateFiltered() {
        String query = this.searchQuery.strip().toLowerCase(Locale.ROOT);
        List<CatalogEntry> full = getCatalog(this.minecraft.player);

        if (query.isEmpty()) {
            this.filtered = full;
            return;
        }

        String[] tokens = query.split("\\s+");
        List<CatalogEntry> matches = new ArrayList<>();
        entryLoop:
        for (CatalogEntry entry : full) {
            for (String token : tokens) {
                if (!matchesToken(entry, token)) continue entryLoop;
            }
            matches.add(entry);
        }
        this.filtered = matches;
    }

    private boolean matchesToken(CatalogEntry entry, String token) {
        if (token.isEmpty()) return true;

        char prefix = token.charAt(0);
        if (prefix == '#') {
            String tagQuery = token.substring(1);
            if (tagQuery.isEmpty()) return true;
            return entry.stack().getTags().anyMatch(tag -> tag.location().toString().contains(tagQuery));
        }

        if (prefix == '@') {
            String modQuery = token.substring(1);
            if (modQuery.isEmpty()) return true;
            return entry.key().getNamespace().toLowerCase(Locale.ROOT).contains(modQuery);
        }

        String name = entry.stack().getHoverName().getString().toLowerCase(Locale.ROOT);
        String path = entry.key().getPath().toLowerCase(Locale.ROOT);
        return name.contains(token) || path.contains(token);
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
        int maxPointsWidth = (int) (this.width * 0.7);
        CraftorioMisc.CraftorioTextEffects.drawCenteredLineFit(guiGraphics, this.font, centerX, 20, true, 0xFFAA00, maxPointsWidth, points, " points");

        int footerY = GRID_TOP + ROWS * CELL_SIZE + 8;
        guiGraphics.drawCenteredString(this.font, Component.literal((this.page + 1) + " / " + this.totalPages()), centerX, footerY + 6, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class ShopItemButton extends AbstractButton {

        private final CatalogEntry entry;
        private final boolean locked;
        private final BigInteger price;
        private final BigInteger unmodifiedPrice;
        private final double shopMultiplier;
        private final boolean needsLiveTooltip;

        ShopItemButton(int x, int y, CatalogEntry entry) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, CommonComponents.EMPTY);
            this.entry = entry;
            this.locked = !ShopScreen.this.isUnlocked(entry);
            Player player = ShopScreen.this.minecraft.player;

            this.unmodifiedPrice = CraftorioShop.getUnitPrice(player, entry.stack(), false);
            this.price = CraftorioShop.getUnitPrice(player, entry.stack(), true);
            double multiplier = Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt();
            for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
                multiplier += shopEffect.getMultiplier();
            }
            this.shopMultiplier = multiplier;

            BigInteger cap = CraftorioMisc.pointThreshold();
            this.needsLiveTooltip = this.price.equals(cap) || this.price.equals(cap.negate())
                    || this.unmodifiedPrice.equals(cap) || this.unmodifiedPrice.equals(cap.negate());

            this.setTooltip(Tooltip.create(buildTooltipComponent()));
        }

        private MutableComponent buildTooltipComponent() {
            MutableComponent tooltipComponent = CraftorioMisc.CraftorioTextEffects.capAwareLine(
                    this.entry.stack().getHoverName().getString() + " - ", this.price, " (", this.unmodifiedPrice, " * " + this.shopMultiplier + ")"
            );
            if (this.locked) {
                tooltipComponent.append(Component.literal(" (Locked)"));
            }
            return tooltipComponent;
        }

        @Override
        public void onPress() {
            // Locked items just refuse to do anything - no screen change, no packet.
            if (this.locked) return;

            ShopScreen.this.minecraft.setScreen(new ShopPurchaseScreen(this.entry, ShopScreen.this));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.needsLiveTooltip) {
                this.setTooltip(Tooltip.create(buildTooltipComponent()));
            }

            if (this.isHovered()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x80FFFFFF);
            }

            guiGraphics.renderItem(this.entry.stack(), this.getX() + 1, this.getY() + 1);
            guiGraphics.renderItemDecorations(ShopScreen.this.font, this.entry.stack(), this.getX() + 1, this.getY() + 1);

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

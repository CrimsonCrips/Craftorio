package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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
import java.util.Set;
import java.util.stream.Stream;


@OnlyIn(Dist.CLIENT)
public class ShopScreen extends CatalogScreen<CatalogEntry> {

    private static final ResourceLocation LOCKED_TEXTURE = Craftorio.getGuiTexture("locked.png");

    private static List<CatalogEntry> catalog;

    private final boolean allUnlocked;
    private final Set<ResourceLocation> unlockedItems;

    public ShopScreen(boolean allUnlocked, Set<ResourceLocation> unlockedItems) {
        super(Component.translatable("misc.craftorio.shop_title"));
        this.allUnlocked = allUnlocked;
        this.unlockedItems = unlockedItems;
    }

    private boolean isUnlocked(CatalogEntry entry) {
        if (CraftorioShopCatalog.isVariantKey(entry.key())) {
            return true;
        }
        return this.allUnlocked || this.unlockedItems.contains(entry.key());
    }

    @Override
    protected List<CatalogEntry> buildCatalog() {
        if (catalog == null) {
            Player player = this.minecraft.player;
            List<CatalogEntry> entries = new ArrayList<>(CraftorioShopCatalog.buildFullCatalog(player.registryAccess()));
            entries.removeIf(entry -> CraftorioShop.getUnitPrice(player, entry.stack(), true).signum() <= 0);
            entries.sort(Comparator.comparing(entry -> entry.key().toString()));
            catalog = entries;
        }
        return catalog;
    }

    @Override
    protected String getSearchName(CatalogEntry entry) {
        return entry.stack().getHoverName().getString();
    }

    @Override
    protected String getSearchNamespace(CatalogEntry entry) {
        return entry.key().getNamespace();
    }

    @Override
    protected Stream<ResourceLocation> getSearchTags(CatalogEntry entry) {
        return entry.stack().getTags().map(TagKey::location);
    }

    @Override
    protected AbstractWidget createEntryWidget(int x, int y, CatalogEntry entry) {
        return new ShopItemButton(x, y, entry);
    }

    @Override
    protected void renderHeader(GuiGraphics guiGraphics, int centerX) {
        guiGraphics.drawCenteredString(this.font, this.getTitle(), centerX, 8, 0xFFFFFF);

        BigInteger points = CraftorioMisc.getPoints(this.minecraft.player);
        int maxPointsWidth = (int) (this.width * 0.7);
        String pointsSuffix = Component.translatable("misc.craftorio.points_suffix").getString();
        CraftorioMisc.CraftorioTextEffects.drawCenteredLineFit(guiGraphics, this.font, centerX, 20, true, 0xFFAA00, maxPointsWidth, points, pointsSuffix);
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
                tooltipComponent.append(Component.translatable("misc.craftorio.locked_suffix"));
            }
            return tooltipComponent;
        }

        @Override
        public void onPress() {
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

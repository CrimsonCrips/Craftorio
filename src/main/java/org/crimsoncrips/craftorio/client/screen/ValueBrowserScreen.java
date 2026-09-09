package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ValueBrowserScreen extends CatalogScreen<Item> {

    private static List<Item> baseCatalog;
    private static Map<Item, BigInteger> valueCache;

    private boolean sortByValuable = true;

    public ValueBrowserScreen() {
        super(Component.translatable("misc.craftorio.value_browser_title"));
    }

    private static void ensureCatalogBuilt(Player player) {
        if (baseCatalog != null) return;

        List<Item> items = new ArrayList<>();
        Map<Item, BigInteger> values = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            items.add(item);
            values.put(item, CraftorioMisc.checkValue(new ItemStack(item), player, false));
        }

        baseCatalog = items;
        valueCache = values;
    }

    @Override
    protected List<Item> buildCatalog() {
        ensureCatalogBuilt(this.minecraft.player);

        List<Item> sorted = new ArrayList<>(baseCatalog);
        sorted.sort(sortByValuable
                ? (a, b) -> valueCache.get(b).compareTo(valueCache.get(a))
                : (a, b) -> valueCache.get(a).compareTo(valueCache.get(b)));
        return sorted;
    }

    @Override
    protected String getSearchName(Item item) {
        return new ItemStack(item).getHoverName().getString();
    }

    @Override
    protected String getSearchNamespace(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }

    @Override
    protected Stream<ResourceLocation> getSearchTags(Item item) {
        return new ItemStack(item).getTags().map(TagKey::location);
    }

    @Override
    protected AbstractWidget createEntryWidget(int x, int y, Item item) {
        return new ValueEntryButton(x, y, item);
    }

    private void toggleSort() {
        this.sortByValuable = !this.sortByValuable;
        this.page = 0;
        this.updateFiltered();
        this.refreshWidgets();
    }

    @Override
    protected void addExtraWidgets() {
        int centerX = this.width / 2;
        this.addRenderableWidget(Button.builder(
                        Component.translatable(this.sortByValuable ? "misc.craftorio.sorted_most_valuable" : "misc.craftorio.sorted_most_valueless"),
                        b -> this.toggleSort())
                .bounds(centerX - 70, 8, 140, 16).build());
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

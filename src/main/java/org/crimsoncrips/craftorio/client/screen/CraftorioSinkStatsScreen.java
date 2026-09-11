package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class CraftorioSinkStatsScreen extends CatalogScreen<Item> {

    private final Map<ResourceLocation, Long> sinkCounts;

    public CraftorioSinkStatsScreen() {
        super(Component.translatable("misc.craftorio.sink_stats_title"));
        this.sinkCounts = CraftorioMisc.getItemsSinked(Minecraft.getInstance().player);
    }

    private long countFor(Item item) {
        return this.sinkCounts.getOrDefault(BuiltInRegistries.ITEM.getKey(item), 0L);
    }

    @Override
    protected List<Item> buildCatalog() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            items.add(item);
        }
        items.sort((a, b) -> Long.compare(countFor(b), countFor(a)));
        return items;
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
        return new SinkCountEntryButton(x, y, item);
    }

    @OnlyIn(Dist.CLIENT)
    private class SinkCountEntryButton extends AbstractButton {

        private final ItemStack displayStack;

        SinkCountEntryButton(int x, int y, Item item) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, CommonComponents.EMPTY);
            this.displayStack = new ItemStack(item);

            long count = countFor(item);
            this.setTooltip(Tooltip.create(Component.literal(this.displayStack.getHoverName().getString())
                    .append(Component.translatable("misc.craftorio.times_sinked_suffix", count))));
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
            guiGraphics.renderItemDecorations(CraftorioSinkStatsScreen.this.font, this.displayStack, this.getX() + 1, this.getY() + 1);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CraftorioSinkStatsScreen extends Screen {

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int LIST_WIDTH = 280;
    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 33;
    private static final int FOOTER_HEIGHT = 36;

    private final Map<ResourceLocation, Long> sinkCounts;

    public CraftorioSinkStatsScreen() {
        super(Component.translatable("misc.craftorio.sink_stats_title"));
        this.sinkCounts = CraftorioMisc.getItemsSinked(Minecraft.getInstance().player);
    }

    private long countFor(Item item) {
        return this.sinkCounts.getOrDefault(BuiltInRegistries.ITEM.getKey(item), 0L);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new SinkStatsList(this.minecraft));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class SinkStatsList extends ObjectSelectionList<SinkStatsList.Row> {

        SinkStatsList(Minecraft minecraft) {
            super(minecraft, CraftorioSinkStatsScreen.this.width,
                    CraftorioSinkStatsScreen.this.height - LIST_TOP - FOOTER_HEIGHT, LIST_TOP, ROW_HEIGHT);

            List<Item> items = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                if (countFor(item) <= 0) continue;
                items.add(item);
            }
            items.sort((a, b) -> Long.compare(countFor(b), countFor(a)));

            for (Item item : items) {
                this.addEntry(new Row(item));
            }
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH;
        }

        @OnlyIn(Dist.CLIENT)
        class Row extends ObjectSelectionList.Entry<Row> {
            private final Item item;
            private final Component name;

            Row(Item item) {
                this.item = item;
                this.name = new ItemStack(item).getHoverName();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.blitSprite(SLOT_SPRITE, left, top, 18, 18);
                guiGraphics.renderItem(new ItemStack(this.item), left + 1, top + 1);

                int textY = top + height / 2 - CraftorioSinkStatsScreen.this.font.lineHeight / 2;
                int color = index % 2 == 0 ? 0xFFFFFF : 0xAAAAAA;
                guiGraphics.drawString(CraftorioSinkStatsScreen.this.font, this.name, left + 22, textY, color, false);

                String countText = String.valueOf(countFor(this.item));
                guiGraphics.drawString(CraftorioSinkStatsScreen.this.font, countText,
                        left + width - CraftorioSinkStatsScreen.this.font.width(countText) - 4, textY, color, false);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}

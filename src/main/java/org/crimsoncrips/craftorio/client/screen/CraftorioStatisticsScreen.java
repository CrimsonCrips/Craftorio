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

import static org.crimsoncrips.craftorio.CraftorioMisc.getHighestPoints;

@OnlyIn(Dist.CLIENT)
public class CraftorioStatisticsScreen extends Screen {

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int LIST_WIDTH = 280;
    private static final int ROW_HEIGHT = 20;
    private static final int TAB_TOP = 24;
    private static final int TAB_HEIGHT = 16;
    private static final int LIST_TOP = TAB_TOP + TAB_HEIGHT + 6;
    private static final int FOOTER_HEIGHT = 36;

    private enum Tab { ITEM_SINKS, GENERAL }

    private final Map<ResourceLocation, Long> sinkCounts;
    private final Map<ResourceLocation, Long> overallSinkCounts;
    private final boolean hasRebirthed;

    private Tab currentTab = Tab.ITEM_SINKS;
    private SinkStatsList sinkStatsList;
    private Button itemSinksTabButton;
    private Button generalTabButton;

    public CraftorioStatisticsScreen() {
        super(Component.translatable("misc.craftorio.stats_title"));
        this.sinkCounts = CraftorioMisc.getItemsSinked(Minecraft.getInstance().player);
        this.overallSinkCounts = CraftorioMisc.getOverallItemsSinked(Minecraft.getInstance().player);
        this.hasRebirthed = CraftorioMisc.getLife(Minecraft.getInstance().player) > 1;
    }

    private long countFor(Item item) {
        return this.sinkCounts.getOrDefault(BuiltInRegistries.ITEM.getKey(item), 0L);
    }

    private long overallCountFor(Item item) {
        return this.overallSinkCounts.getOrDefault(BuiltInRegistries.ITEM.getKey(item), 0L);
    }

    @Override
    protected void init() {
        super.init();

        int tabWidth = 140;
        int tabsLeft = this.width / 2 - tabWidth;

        this.itemSinksTabButton = Button.builder(Component.translatable("misc.craftorio.stats_tab_items"), b -> selectTab(Tab.ITEM_SINKS))
                .bounds(tabsLeft, TAB_TOP, tabWidth, TAB_HEIGHT).build();
        this.addRenderableWidget(this.itemSinksTabButton);

        this.generalTabButton = Button.builder(Component.translatable("misc.craftorio.stats_tab_general"), b -> selectTab(Tab.GENERAL))
                .bounds(tabsLeft + tabWidth, TAB_TOP, tabWidth, TAB_HEIGHT).build();
        this.addRenderableWidget(this.generalTabButton);

        this.sinkStatsList = new SinkStatsList(this.minecraft);
        this.addRenderableWidget(this.sinkStatsList);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());

        refreshTabVisibility();
    }

    private void selectTab(Tab tab) {
        this.currentTab = tab;
        refreshTabVisibility();
    }

    private void refreshTabVisibility() {
        boolean showList = this.currentTab == Tab.ITEM_SINKS;
        this.sinkStatsList.visible = showList;
        this.sinkStatsList.active = showList;
        this.itemSinksTabButton.active = this.currentTab != Tab.ITEM_SINKS;
        this.generalTabButton.active = this.currentTab != Tab.GENERAL;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        if (this.currentTab == Tab.GENERAL) {
            renderGeneralStats(guiGraphics);
        }
    }

    private void renderGeneralStats(GuiGraphics guiGraphics) {
        int labelX = this.width / 2 - LIST_WIDTH / 2;
        int y = LIST_TOP + 10;
        int lineHeight = this.font.lineHeight + 8;

        guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.life_label")
                .copy().append(String.valueOf(CraftorioMisc.getLife(this.minecraft.player))), labelX, y, 0xFFFFFF, false);
        y += lineHeight;

        Component highestPointsLine = Component.translatable("misc.craftorio.highest_points_label")
                .copy().append(CraftorioMisc.bigIntFormat(getHighestPoints(this.minecraft.player)));
        if (hasRebirthed) {
            highestPointsLine = highestPointsLine.copy().append(Component.translatable("misc.craftorio.highest_points_overall_suffix",
                    CraftorioMisc.getOverallHighestPoints(this.minecraft.player).toString()));
        }
        guiGraphics.drawString(this.font, highestPointsLine, labelX, y, 0xFFFFFF, false);
        y += lineHeight;

        Component contractsLine = Component.translatable("misc.craftorio.contracts_fulfilled_label")
                .copy().append(String.valueOf(CraftorioMisc.getContractsCompleted(this.minecraft.player)));
        if (hasRebirthed) {
            contractsLine = contractsLine.copy().append(Component.translatable("misc.craftorio.contracts_overall_suffix",
                    String.valueOf(CraftorioMisc.getOverallContractsCompleted(this.minecraft.player))));
        }
        guiGraphics.drawString(this.font, contractsLine, labelX, y, 0xFFFFFF, false);
        y += lineHeight;

        String multiplierText = String.format("%.2f", CraftorioMisc.getHighestMultiplier(this.minecraft.player));
        guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.highest_multiplier_label")
                .copy().append(multiplierText), labelX, y, 0xFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class SinkStatsList extends ObjectSelectionList<SinkStatsList.Row> {

        SinkStatsList(Minecraft minecraft) {
            super(minecraft, CraftorioStatisticsScreen.this.width,
                    CraftorioStatisticsScreen.this.height - LIST_TOP - FOOTER_HEIGHT, LIST_TOP, ROW_HEIGHT);

            List<Item> items = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                if (countFor(item) <= 0 && overallCountFor(item) <= 0) continue;
                items.add(item);
            }
            items.sort((a, b) -> Long.compare(countFor(b) + overallCountFor(b), countFor(a) + overallCountFor(a)));

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

                int textY = top + height / 2 - CraftorioStatisticsScreen.this.font.lineHeight / 2;
                int color = index % 2 == 0 ? 0xFFFFFF : 0xAAAAAA;
                guiGraphics.drawString(CraftorioStatisticsScreen.this.font, this.name, left + 22, textY, color, false);

                String countText = hasRebirthed
                        ? countFor(this.item) + "(" + overallCountFor(this.item) + ")"
                        : String.valueOf(countFor(this.item));
                guiGraphics.drawString(CraftorioStatisticsScreen.this.font, countText,
                        left + width - CraftorioStatisticsScreen.this.font.width(countText) - 4, textY, color, false);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}

package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class DevToolsPickerScreen extends Screen {

    public record Option(Component label, String detail, int indent, boolean selectable, Runnable onPick) {}

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_WIDTH = 340;
    private static final int LIST_TOP = 48;
    private static final int FOOTER_HEIGHT = 36;
    private static final int INDENT_WIDTH = 10;

    private final Screen parent;
    private final List<Option> options;
    private String filter = "";
    private EditBox searchBox;
    private PickerList list;

    public DevToolsPickerScreen(Component title, Screen parent, List<Option> options) {
        super(title);
        this.parent = parent;
        this.options = options;
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 24, 200, 16, Component.literal("search"));
        this.searchBox.setMaxLength(128);
        this.searchBox.setValue(this.filter);
        this.searchBox.setResponder(value -> {
            this.filter = value;
            this.list.refill(value);
        });
        this.addRenderableWidget(this.searchBox);

        this.list = new PickerList(this.minecraft);
        this.list.refill(this.filter);
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.searchBox, "search");
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class PickerList extends ObjectSelectionList<PickerList.Row> {

        PickerList(Minecraft minecraft) {
            super(minecraft, DevToolsPickerScreen.this.width, DevToolsPickerScreen.this.height - LIST_TOP - FOOTER_HEIGHT, LIST_TOP, ROW_HEIGHT);
        }

        void refill(String query) {
            String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            this.clearEntries();
            for (Option option : DevToolsPickerScreen.this.options) {
                if (!needle.isEmpty()
                        && !option.label().getString().toLowerCase(Locale.ROOT).contains(needle)
                        && !option.detail().toLowerCase(Locale.ROOT).contains(needle)) {
                    continue;
                }
                this.addEntry(new Row(option));
            }
            this.setScrollAmount(0);
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH;
        }

        @OnlyIn(Dist.CLIENT)
        class Row extends ObjectSelectionList.Entry<Row> {
            private final Option option;

            Row(Option option) {
                this.option = option;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partialTick) {
                var font = DevToolsPickerScreen.this.font;
                if (hovering && this.option.selectable()) {
                    guiGraphics.fill(left, top, left + width, top + height, 0x30FFFFFF);
                }

                int textY = top + height / 2 - font.lineHeight / 2;
                int color = this.option.selectable() ? 0xFFFFFF : 0x777777;
                int labelX = left + 4 + this.option.indent() * INDENT_WIDTH;

                int detailWidth = font.width(this.option.detail());
                int labelSpace = Math.max(20, width - (labelX - left) - detailWidth - 12);
                String labelText = font.plainSubstrByWidth(this.option.label().getString(), labelSpace);
                guiGraphics.drawString(font, labelText, labelX, textY, color, false);
                guiGraphics.drawString(font, this.option.detail(), left + width - detailWidth - 4, textY, 0x808080, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0 && this.option.selectable()) {
                    this.option.onPick().run();
                }
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.option.label());
            }
        }
    }
}

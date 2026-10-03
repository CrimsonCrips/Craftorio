package org.crimsoncrips.craftorio.client.screen.devtools.contract_creator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsTagPicker;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContractCreatorTagScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 300;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 40;

    private final Screen returnTo;
    private final List<ContractCreatorDraft.TagEntry> entries = new ArrayList<>();

    public ContractCreatorTagScreen(Screen returnTo) {
        super(Component.translatable("misc.craftorio.dev_tools_contract_tags_title"));
        this.returnTo = returnTo;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.entries.clear();
        this.entries.addAll(ContractCreatorDraft.tagEntries());

        int left = (this.width - PANEL_WIDTH) / 2;
        int y = LIST_TOP;
        for (int i = 0; i < this.entries.size(); i++) {
            int index = i;
            EditBox amountBox = new EditBox(this.font, left + PANEL_WIDTH - 104, y, 60, 16, Component.literal("amount"));
            amountBox.setMaxLength(9);
            amountBox.setValue(String.valueOf(this.entries.get(i).amount()));
            amountBox.setResponder(value -> {
                int amount;
                try {
                    amount = Math.max(1, Integer.parseInt(value.trim()));
                } catch (NumberFormatException e) {
                    return;
                }
                this.entries.set(index, new ContractCreatorDraft.TagEntry(this.entries.get(index).tag(), amount));
                ContractCreatorDraft.setTagEntries(this.entries);
            });
            this.addRenderableWidget(amountBox);
            this.addRenderableWidget(Button.builder(Component.literal("X"), b -> {
                this.entries.remove(index);
                ContractCreatorDraft.setTagEntries(this.entries);
                this.rebuildWidgets();
            }).bounds(left + PANEL_WIDTH - 40, y, 20, 16).build());
            y += ROW_HEIGHT;
        }

        y += 6;
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_add_tag"), b ->
                DevToolsTagPicker.open(this.minecraft, this, tag -> {
                    List<ContractCreatorDraft.TagEntry> updated = new ArrayList<>(ContractCreatorDraft.tagEntries());
                    updated.add(new ContractCreatorDraft.TagEntry(tag, 1));
                    ContractCreatorDraft.setTagEntries(updated);
                })).bounds(this.width / 2 - 100, y, 95, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 + 5, y, 95, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.returnTo);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.dev_tools_contract_tags_hint"), this.width / 2, 24, 0x888888);

        int left = (this.width - PANEL_WIDTH) / 2;
        int y = LIST_TOP;
        if (this.entries.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.dev_tools_contract_no_tags"), this.width / 2, y + 4, 0xAAAAAA);
        }
        for (ContractCreatorDraft.TagEntry entry : this.entries) {
            ResourceLocation id = ResourceLocation.tryParse(entry.tag());
            boolean known = id != null && BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, id)).isPresent();
            String label = this.font.plainSubstrByWidth("#" + entry.tag(), PANEL_WIDTH - 112);
            graphics.drawString(this.font, label, left, y + 4, known ? 0xFFFFFF : 0xFF5555, false);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

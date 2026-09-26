package org.crimsoncrips.craftorio.client.screen.devtools.contract_creator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContractCreatorPropertiesScreen extends Screen {

    private static final String[] LABEL_KEYS = {
            "dev_tools_label_reward_points", "dev_tools_label_weight", "dev_tools_label_required_mod",
            "dev_tools_label_id", "dev_tools_label_mod_id", "dev_tools_label_card_texture"
    };

    private final Screen parent;
    private final ContractCreatorMenu menu;

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 320;
    private final int panelHeight = 230;

    private final List<EditBox> hintedBoxes = new ArrayList<>();
    private final List<String> hintTexts = new ArrayList<>();

    public ContractCreatorPropertiesScreen(Screen parent, ContractCreatorMenu menu) {
        super(Component.translatable("misc.craftorio.contract_creator_details_title"));
        this.parent = parent;
        this.menu = menu;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.hintedBoxes.clear();
        this.hintTexts.clear();

        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        int fieldX = panelLeft + 132;
        int fieldWidth = panelWidth - 142;
        int y = panelTop + 24;
        int rowHeight = 22;

        newBox(fieldX, y, fieldWidth, ContractCreatorDraft.BASE_VALUE, "e.g. 1000 or 1e6");
        y += rowHeight;
        newBox(fieldX, y, fieldWidth, ContractCreatorDraft.WEIGHT, "e.g. 10");
        y += rowHeight;
        newBox(fieldX, y, fieldWidth, ContractCreatorDraft.REQUIRED_MOD, "e.g. create");
        y += rowHeight;
        newBox(fieldX, y, fieldWidth, ContractCreatorDraft.ID, "e.g. my_contract");
        y += rowHeight;
        newBox(fieldX, y, fieldWidth, ContractCreatorDraft.MOD_ID, "e.g. yourmodid");
        y += rowHeight;
        newBox(fieldX, y, fieldWidth, ContractCreatorDraft.CARD_TEXTURE, "e.g. craftorio:default");
        y += rowHeight + 8;

        Component requirementsLabel = Component.translatable("misc.craftorio.dev_tools_contract_item_requirements");
        Component rewardsLabel = Component.translatable("misc.craftorio.dev_tools_contract_item_rewards");
        int requirementsWidth = this.font.width(requirementsLabel) + 12;
        int rewardsWidth = this.font.width(rewardsLabel) + 12;
        int x = panelLeft + (panelWidth - requirementsWidth - rewardsWidth - 6) / 2;
        this.addRenderableWidget(Button.builder(requirementsLabel, b ->
                        this.minecraft.setScreen(new ContractCreatorBountyScreen(this.menu, this.minecraft.player.getInventory(), this)))
                .bounds(x, y, requirementsWidth, 18).build());
        this.addRenderableWidget(Button.builder(rewardsLabel, b ->
                        this.minecraft.setScreen(new ContractCreatorRewardScreen(this.menu, this.minecraft.player.getInventory(), this)))
                .bounds(x + requirementsWidth + 6, y, rewardsWidth, 18).build());
        y += 24;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(panelLeft + panelWidth / 2 - 40, y, 80, 18).build());
    }

    private void newBox(int x, int y, int width, String key, String hint) {
        EditBox box = new EditBox(this.font, x, y, width, 16, Component.literal(""));
        box.setMaxLength(256);
        box.setValue(ContractCreatorDraft.get(key));
        box.setResponder(value -> ContractCreatorDraft.set(key, value));
        this.addRenderableWidget(box);
        this.hintedBoxes.add(box);
        this.hintTexts.add(hint);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xE0202020);
        guiGraphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF808080);
        guiGraphics.drawCenteredString(this.font, this.title, panelLeft + panelWidth / 2, panelTop + 8, 0xFFFFFF);

        int y = panelTop + 24;
        for (String key : LABEL_KEYS) {
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio." + key), panelLeft + 8, y + 4, 0xAAAAAA, false);
            y += 22;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < hintedBoxes.size(); i++) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, hintedBoxes.get(i), hintTexts.get(i));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

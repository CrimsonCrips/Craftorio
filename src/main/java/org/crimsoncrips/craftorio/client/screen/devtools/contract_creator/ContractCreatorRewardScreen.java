package org.crimsoncrips.craftorio.client.screen.devtools.contract_creator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.client.state.ClientContractCreatorDraftState;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;
import org.crimsoncrips.craftorio.networking.devtools.ClearContractCreatorGridPacket;
import org.crimsoncrips.craftorio.networking.devtools.CopyInventoryToContractCreatorPacket;
import org.crimsoncrips.craftorio.networking.devtools.SetContractCreatorViewPacket;

import java.util.List;

public class ContractCreatorRewardScreen extends AbstractContainerScreen<ContractCreatorMenu> {

    private static final String REWARD_ROLLS_KEY = "contract_reward_rolls";

    private EditBox rewardRollsBox;

    private final Screen returnTo;

    public ContractCreatorRewardScreen(ContractCreatorMenu menu, Inventory playerInventory, Screen returnTo) {
        super(menu, playerInventory, Component.translatable("misc.craftorio.contract_creator_reward_title"));
        this.returnTo = returnTo;
        this.imageWidth = ContractCreatorMenu.PANEL_WIDTH;
        this.imageHeight = ContractCreatorMenu.GRID_TOP + ContractCreatorMenu.GRID_ROWS * 18 + ContractCreatorMenu.PLAYER_INV_GAP + 78;
        this.inventoryLabelY = ContractCreatorMenu.GRID_TOP + ContractCreatorMenu.GRID_ROWS * 18 + ContractCreatorMenu.PLAYER_INV_GAP - 4;
    }

    @Override
    protected void init() {
        super.init();
        this.menu.showRewardView();
        PacketDistributor.sendToServer(new SetContractCreatorViewPacket(true, this.menu.getPage()));

        int panelLeft = (this.width - this.imageWidth) / 2;
        int panelTop = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.returnTo))
                .bounds(panelLeft + 8, panelTop + 6, 52, 14).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_copy_from_inventory"), b -> PacketDistributor.sendToServer(new CopyInventoryToContractCreatorPacket()))
                .bounds(panelLeft + 8, panelTop + 26, 130, 16).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_browse_items"), b -> this.minecraft.setScreen(new ContractCreatorItemBrowserScreen(this)))
                .bounds(panelLeft + 142, panelTop + 26, 130, 16).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_clear_page"), b -> PacketDistributor.sendToServer(new ClearContractCreatorGridPacket()))
                .bounds(panelLeft + (this.imageWidth - 80) / 2, panelTop + 44, 80, 14).build());

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(panelLeft + (this.imageWidth - 80) / 2 - 24, panelTop + 44, 20, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(panelLeft + (this.imageWidth + 80) / 2 + 4, panelTop + 44, 20, 14).build());

        int rollsY = ContractCreatorMenu.GRID_TOP + ContractCreatorMenu.GRID_ROWS * 18 + 4;
        this.rewardRollsBox = new EditBox(this.font, panelLeft + 90, panelTop + rollsY, this.imageWidth - 100, 16, Component.literal(""));
        this.rewardRollsBox.setMaxLength(256);
        this.rewardRollsBox.setValue(ClientContractCreatorDraftState.get(REWARD_ROLLS_KEY, "0"));
        this.rewardRollsBox.setResponder(value -> ClientContractCreatorDraftState.set(REWARD_ROLLS_KEY, value));
        this.addRenderableWidget(this.rewardRollsBox);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.returnTo);
    }

    public static String getRewardRollsValue() {
        return ClientContractCreatorDraftState.get(REWARD_ROLLS_KEY, "0");
    }

    public static void setRewardRollsValue(String value) {
        ClientContractCreatorDraftState.set(REWARD_ROLLS_KEY, value);
    }

    private void changePage(int direction) {
        int step = hasShiftDown() ? 10 : 1;
        this.menu.setPage(this.menu.getPage() + direction * step);
        PacketDistributor.sendToServer(new SetContractCreatorViewPacket(true, this.menu.getPage()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int gridLeft = (this.width - this.imageWidth) / 2 + ContractCreatorMenu.GRID_LEFT;
        int gridTop = (this.height - this.imageHeight) / 2 + ContractCreatorMenu.GRID_TOP;
        if (scrollY != 0 && mouseX >= gridLeft && mouseX < gridLeft + ContractCreatorMenu.GRID_COLUMNS * 18
                && mouseY >= gridTop && mouseY < gridTop + ContractCreatorMenu.GRID_ROWS * 18) {
            changePage(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(i, j, i + this.imageWidth, j + this.imageHeight, 0xE0202020);
        guiGraphics.renderOutline(i, j, this.imageWidth, this.imageHeight, 0xFF808080);

        guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_rewards"), i + 8, j + ContractCreatorMenu.GRID_TOP - 10, 0xAAAAAA, false);
        Component pageLabel = Component.translatable("misc.craftorio.dev_tools_page", this.menu.getPage() + 1, ContractCreatorMenu.PAGES);
        guiGraphics.drawString(this.font, pageLabel, i + this.imageWidth - 8 - this.font.width(pageLabel), j + ContractCreatorMenu.GRID_TOP - 10, 0xAAAAAA, false);

        Slot destroySlot = this.menu.getDestroySlot();
        guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_destroy_slot"), i + destroySlot.x - 15, j + destroySlot.y - 10, 0xFF5555, false);

        for (Slot slot : this.menu.slots) {
            if (!slot.isActive()) continue;
            int slotX = i + slot.x - 1;
            int slotY = j + slot.y - 1;
            if (slot == destroySlot) {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF551A1A);
                guiGraphics.renderOutline(slotX, slotY, 18, 18, 0xFFAA3333);
            } else {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF404040);
                guiGraphics.renderOutline(slotX, slotY, 18, 18, 0xFF808080);
            }
        }

        int rollsY = j + ContractCreatorMenu.GRID_TOP + ContractCreatorMenu.GRID_ROWS * 18 + 4;
        guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_reward_rolls"), i + 8, rollsY + 4, 0xAAAAAA, false);

        List<FormattedCharSequence> infoLines = this.font.split(Component.translatable("misc.craftorio.dev_tools_reward_rolls_info"), this.imageWidth - 16);
        int infoY = rollsY + 20;
        for (var line : infoLines) {
            guiGraphics.drawString(this.font, line, i + 8, infoY, 0x888888, false);
            infoY += this.font.lineHeight;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY - 10, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.drawCenteredString(this.font, this.title, i + this.imageWidth / 2, j + 8, 0xFFFFFF);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

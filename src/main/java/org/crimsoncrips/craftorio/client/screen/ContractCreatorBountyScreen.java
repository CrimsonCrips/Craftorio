package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;
import org.crimsoncrips.craftorio.networking.ClearContractCreatorGridPacket;
import org.crimsoncrips.craftorio.networking.CopyInventoryToContractCreatorPacket;
import org.crimsoncrips.craftorio.networking.LoadContractIntoCreatorPacket;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.networking.SetContractCreatorViewPacket;

import java.util.ArrayList;
import java.util.List;

public class ContractCreatorBountyScreen extends AbstractContainerScreen<ContractCreatorMenu> {

    public ContractCreatorBountyScreen(ContractCreatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable("misc.craftorio.contract_creator_bounty_title"));
        this.imageWidth = ContractCreatorMenu.PANEL_WIDTH;
        this.imageHeight = ContractCreatorMenu.GRID_TOP + ContractCreatorMenu.GRID_ROWS * 18 + ContractCreatorMenu.PLAYER_INV_GAP + 78;
        this.inventoryLabelY = ContractCreatorMenu.GRID_TOP + ContractCreatorMenu.GRID_ROWS * 18 + ContractCreatorMenu.PLAYER_INV_GAP - 4;
    }

    @Override
    protected void init() {
        super.init();
        this.menu.showBountyView();
        PacketDistributor.sendToServer(new SetContractCreatorViewPacket(false));

        int panelLeft = (this.width - this.imageWidth) / 2;
        int panelTop = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> {
            if (this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
            this.minecraft.setScreen(new DevToolsScreen(null));
        }).bounds(panelLeft + 8, panelTop + 6, 52, 14).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_next"), b -> this.minecraft.setScreen(new ContractCreatorRewardScreen(this.menu, this.minecraft.player.getInventory(), this.title)))
                .bounds(panelLeft + this.imageWidth - 60, panelTop + 6, 52, 14).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_copy_from_inventory"), b -> PacketDistributor.sendToServer(new CopyInventoryToContractCreatorPacket()))
                .bounds(panelLeft + 8, panelTop + 26, 130, 16).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_browse_items"), b -> this.minecraft.setScreen(new ContractCreatorItemBrowserScreen(this)))
                .bounds(panelLeft + 142, panelTop + 26, 130, 16).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_clear"), b -> PacketDistributor.sendToServer(new ClearContractCreatorGridPacket()))
                .bounds(panelLeft + (this.imageWidth - 130) / 2, panelTop + 44, 130, 14).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_edit_contracts"), b -> openEditPicker())
                .bounds(this.width - 108, this.height - 28, 100, 20).build());
    }

    private void openEditPicker() {
        if (this.minecraft.level == null) return;

        Registry<CraftorioContract> registry = this.minecraft.level.registryAccess().registryOrThrow(CraftorioContract.REGISTRY_KEY);
        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        for (Holder.Reference<CraftorioContract> holder : registry.holders().toList()) {
            ResourceLocation id = holder.key().location();
            CraftorioContract contract = holder.value();
            options.add(new DevToolsPickerScreen.Option(Component.literal(contract.getActualName()), id.toString(), 0, true, () -> {
                ContractCreatorDetailsScreen.loadIntoDraft(id, contract);
                PacketDistributor.sendToServer(new LoadContractIntoCreatorPacket(id));
                this.minecraft.setScreen(this);
            }));
        }

        this.minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_contract"), this, options));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(i, j, i + this.imageWidth, j + this.imageHeight, 0xE0202020);
        guiGraphics.renderOutline(i, j, this.imageWidth, this.imageHeight, 0xFF808080);

        guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_bounty_items"), i + 8, j + ContractCreatorMenu.GRID_TOP - 10, 0xAAAAAA, false);

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

package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItem;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItemReward;

@OnlyIn(Dist.CLIENT)
public class ContractDetailsScreen extends Screen {

    private static final int TOP_MARGIN = 50;
    private static final int BOTTOM_MARGIN = 40;
    private static final int MAX_ROW_HEIGHT = 22;
    private static final int ROW_ICON_SIZE = 16;

    private final Screen parent;
    private final CraftorioShipmentContract contract;

    public ContractDetailsScreen(Screen parent, CraftorioShipmentContract contract) {
        super(Component.literal(contract.getName()));
        this.parent = parent;
        this.contract = contract;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void onClose() {
        if (this.parent instanceof ContractRevealScreen revealScreen) {
            revealScreen.skipIntro();
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.contract.getName(), this.width / 2, 16, 0xFFFFFF);

        int leftColX = this.width / 4;
        int rightColX = this.width * 3 / 4;
        int columnWidth = this.width / 2 - 20;

        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.contract_bounty"), leftColX, TOP_MARGIN - 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.contract_item_rewards"), rightColX, TOP_MARGIN - 14, 0xFFFFFF);

        int availableHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;

        renderBountyColumn(graphics, leftColX, columnWidth, availableHeight);
        renderRewardColumn(graphics, rightColX, columnWidth, availableHeight);
    }

    private void renderBountyColumn(GuiGraphics graphics, int centerX, int columnWidth, int availableHeight) {
        var bounty = this.contract.getItemBounty();
        if (bounty.isEmpty()) return;

        int rowHeight = Math.min(MAX_ROW_HEIGHT, Math.max(10, availableHeight / bounty.size()));
        int y = TOP_MARGIN;

        for (CraftorioShipmentItem item : bounty) {
            ItemStack stack = item.getIconStack();
            int iconX = centerX - columnWidth / 2;
            graphics.renderItem(stack, iconX, y);

            String line = Component.translatable("misc.craftorio.contract_bounty_line",
                    item.getItemsGiven(), item.getAmountRequired(), item.getDisplayName().getString()).getString();
            graphics.drawString(this.font, line, iconX + ROW_ICON_SIZE + 6, y + (ROW_ICON_SIZE - this.font.lineHeight) / 2, 0xCCCCCC, false);

            y += rowHeight;
        }
    }

    private void renderRewardColumn(GuiGraphics graphics, int centerX, int columnWidth, int availableHeight) {
        var rewards = this.contract.getRewards();
        int rowCount = rewards.size() + 1;
        int rowHeight = Math.min(MAX_ROW_HEIGHT, Math.max(10, availableHeight / rowCount));
        int y = TOP_MARGIN;
        int iconX = centerX - columnWidth / 2;

        for (CraftorioShipmentItemReward reward : rewards) {
            ItemStack stack = new ItemStack(reward.getRewardingItem());
            graphics.renderItem(stack, iconX, y);

            String line = Component.translatable("misc.craftorio.contract_reward_line",
                    reward.getAmountGiving(), reward.getRewardingItem().getDescription().getString()).getString();
            graphics.drawString(this.font, line, iconX + ROW_ICON_SIZE + 6, y + (ROW_ICON_SIZE - this.font.lineHeight) / 2, 0xCCCCCC, false);

            y += rowHeight;
        }

        String pointLine = Component.translatable("misc.craftorio.contract_point_reward",
                CraftorioMisc.bigIntFormat(this.contract.getPointRewards(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt())).getString();
        graphics.drawString(this.font, pointLine, iconX, y + (ROW_ICON_SIZE - this.font.lineHeight) / 2, 0xFFAA00, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

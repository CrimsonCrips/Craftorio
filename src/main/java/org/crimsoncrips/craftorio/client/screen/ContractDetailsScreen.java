package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItem;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItemReward;

import java.math.BigInteger;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContractDetailsScreen extends Screen {

    private static final int TOP_MARGIN = 70;
    private static final int BOTTOM_MARGIN = 40;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_ICON_SIZE = 16;
    private static final int SCROLLBAR_WIDTH = 4;

    private final Screen parent;
    private final CraftorioShipmentContract contract;

    private int bountyScroll = 0;
    private int rewardScroll = 0;

    private boolean draggingBountyScrollbar = false;
    private boolean draggingRewardScrollbar = false;
    private double dragStartMouseY = 0;
    private int dragStartScroll = 0;

    private boolean showRemainingOnly = false;
    private Button remainingToggleButton;

    public ContractDetailsScreen(Screen parent, CraftorioShipmentContract contract) {
        super(Component.literal(contract.getActualName()));
        this.parent = parent;
        this.contract = contract;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());

        this.remainingToggleButton = this.addRenderableWidget(Button.builder(remainingToggleLabel(), b -> toggleRemainingOnly())
                .bounds(leftColX() - 65, 30, 130, 16).build());
    }

    private void toggleRemainingOnly() {
        this.showRemainingOnly = !this.showRemainingOnly;
        this.bountyScroll = 0;
        this.remainingToggleButton.setMessage(remainingToggleLabel());
    }

    private Component remainingToggleLabel() {
        return Component.translatable(this.showRemainingOnly
                ? "misc.craftorio.contract_show_all_button"
                : "misc.craftorio.contract_show_remaining_button");
    }

    private List<CraftorioShipmentItem> bountyRows() {
        List<CraftorioShipmentItem> bounty = this.contract.getItemBounty();
        if (!this.showRemainingOnly) return bounty;
        return bounty.stream().filter(item -> !item.isComplete()).toList();
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

        graphics.drawCenteredString(this.font, this.contract.getActualName(), this.width / 2, 16, 0xFFFFFF);

        int leftColX = this.width / 4;
        int rightColX = this.width * 3 / 4;
        int columnWidth = this.width / 2 - 20;

        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.contract_bounty"), leftColX, TOP_MARGIN - 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.contract_item_rewards"), rightColX, TOP_MARGIN - 14, 0xFFFFFF);

        int availableHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;

        ItemStack hovered = renderBountyColumn(graphics, leftColX, columnWidth, availableHeight, mouseX, mouseY);
        if (hovered == null) {
            hovered = renderRewardColumn(graphics, rightColX, columnWidth, availableHeight, mouseX, mouseY);
        }

        if (hovered != null) {
            graphics.renderTooltip(this.font, hovered, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        int availableHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        boolean leftSide = mouseX < this.width / 2.0;

        if (leftSide) {
            int maxScroll = maxScroll(bountyRows().size(), availableHeight);
            this.bountyScroll = Mth.clamp(this.bountyScroll - (int) Math.round(scrollY * ROW_HEIGHT), 0, maxScroll);
        } else {
            int maxScroll = maxScroll(this.contract.getRewards().size() + 1, availableHeight);
            this.rewardScroll = Mth.clamp(this.rewardScroll - (int) Math.round(scrollY * ROW_HEIGHT), 0, maxScroll);
        }
        return true;
    }

    private int maxScroll(int rowCount, int availableHeight) {
        return Math.max(0, rowCount * ROW_HEIGHT - availableHeight);
    }

    private int leftColX() { return this.width / 4; }

    private int rightColX() { return this.width * 3 / 4; }

    private int columnWidth() { return this.width / 2 - 20; }

    private int availableHeight() { return this.height - TOP_MARGIN - BOTTOM_MARGIN; }

    private int bountyScrollbarX() { return leftColX() + columnWidth() / 2 + 2; }

    private int rewardScrollbarX() { return rightColX() + columnWidth() / 2 + 2; }

    private int[] thumbBounds(int top, int bottom, int contentHeight, int scroll) {
        int trackHeight = bottom - top;
        if (contentHeight <= trackHeight) return null;

        int maxScroll = contentHeight - trackHeight;
        int thumbHeight = Math.max(10, trackHeight * trackHeight / contentHeight);
        int thumbY = top + scroll * (trackHeight - thumbHeight) / Math.max(1, maxScroll);
        return new int[]{thumbY, thumbHeight, maxScroll};
    }

    private boolean isOnScrollbar(double mouseX, double mouseY, int x, int thumbY, int thumbHeight) {
        return mouseX >= x - 1 && mouseX < x + SCROLLBAR_WIDTH + 1 && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int top = TOP_MARGIN;
            int bottom = TOP_MARGIN + availableHeight();

            int[] bountyThumb = thumbBounds(top, bottom, bountyRows().size() * ROW_HEIGHT, this.bountyScroll);
            if (bountyThumb != null && isOnScrollbar(mouseX, mouseY, bountyScrollbarX(), bountyThumb[0], bountyThumb[1])) {
                this.draggingBountyScrollbar = true;
                this.dragStartMouseY = mouseY;
                this.dragStartScroll = this.bountyScroll;
                return true;
            }

            int rewardRowCount = this.contract.getRewards().size() + 1;
            int[] rewardThumb = thumbBounds(top, bottom, rewardRowCount * ROW_HEIGHT, this.rewardScroll);
            if (rewardThumb != null && isOnScrollbar(mouseX, mouseY, rewardScrollbarX(), rewardThumb[0], rewardThumb[1])) {
                this.draggingRewardScrollbar = true;
                this.dragStartMouseY = mouseY;
                this.dragStartScroll = this.rewardScroll;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingBountyScrollbar || this.draggingRewardScrollbar) {
            int top = TOP_MARGIN;
            int bottom = TOP_MARGIN + availableHeight();
            int trackHeight = bottom - top;

            int contentHeight = this.draggingBountyScrollbar
                    ? bountyRows().size() * ROW_HEIGHT
                    : (this.contract.getRewards().size() + 1) * ROW_HEIGHT;

            int maxScroll = contentHeight - trackHeight;
            int thumbHeight = Math.max(10, trackHeight * trackHeight / Math.max(1, contentHeight));
            int scrollRange = trackHeight - thumbHeight;

            if (maxScroll > 0 && scrollRange > 0) {
                double deltaMouseY = mouseY - this.dragStartMouseY;
                int newScroll = this.dragStartScroll + (int) Math.round(deltaMouseY * maxScroll / (double) scrollRange);
                newScroll = Mth.clamp(newScroll, 0, maxScroll);

                if (this.draggingBountyScrollbar) {
                    this.bountyScroll = newScroll;
                } else {
                    this.rewardScroll = newScroll;
                }
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingBountyScrollbar = false;
        this.draggingRewardScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isHoveringIcon(int iconX, int iconY, int mouseX, int mouseY) {
        return mouseX >= iconX && mouseX < iconX + ROW_ICON_SIZE && mouseY >= iconY && mouseY < iconY + ROW_ICON_SIZE;
    }

    private ItemStack renderBountyColumn(GuiGraphics graphics, int centerX, int columnWidth, int availableHeight, int mouseX, int mouseY) {
        var bounty = bountyRows();
        if (bounty.isEmpty()) {
            if (this.showRemainingOnly && !this.contract.getItemBounty().isEmpty()) {
                graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.contract_bounty_complete"), centerX, TOP_MARGIN, 0x55FF55);
            }
            return null;
        }

        int top = TOP_MARGIN;
        int bottom = TOP_MARGIN + availableHeight;
        int iconX = centerX - columnWidth / 2;
        this.bountyScroll = Mth.clamp(this.bountyScroll, 0, maxScroll(bounty.size(), availableHeight));

        graphics.enableScissor(iconX, top, iconX + columnWidth, bottom);

        ItemStack hovered = null;
        int y = top - this.bountyScroll;
        for (CraftorioShipmentItem item : bounty) {
            if (y + ROW_HEIGHT >= top && y <= bottom) {
                ItemStack stack = item.getIconStack();
                graphics.renderItem(stack, iconX, y);

                if (mouseY >= top && mouseY < bottom && isHoveringIcon(iconX, y, mouseX, mouseY)) {
                    hovered = stack;
                }

                String line = Component.translatable("misc.craftorio.contract_bounty_line",
                        item.getItemsGiven(), item.getAmountRequired(), item.getDisplayName().getString()).getString();
                graphics.drawString(this.font, line, iconX + ROW_ICON_SIZE + 6, y + (ROW_ICON_SIZE - this.font.lineHeight) / 2, 0xCCCCCC, false);
            }
            y += ROW_HEIGHT;
        }

        graphics.disableScissor();
        renderScrollbar(graphics, iconX + columnWidth + 2, top, bottom, bounty.size() * ROW_HEIGHT, this.bountyScroll);

        return hovered;
    }

    private ItemStack renderRewardColumn(GuiGraphics graphics, int centerX, int columnWidth, int availableHeight, int mouseX, int mouseY) {
        var rewards = this.contract.getRewards();
        int rowCount = rewards.size() + 1;
        int top = TOP_MARGIN;
        int bottom = TOP_MARGIN + availableHeight;
        int iconX = centerX - columnWidth / 2;
        this.rewardScroll = Mth.clamp(this.rewardScroll, 0, maxScroll(rowCount, availableHeight));

        graphics.enableScissor(iconX, top, iconX + columnWidth, bottom);

        ItemStack hovered = null;
        int y = top - this.rewardScroll;
        for (CraftorioShipmentItemReward reward : rewards) {
            if (y + ROW_HEIGHT >= top && y <= bottom) {
                ItemStack stack = reward.getRewardingStack();
                graphics.renderItem(stack, iconX, y);

                if (mouseY >= top && mouseY < bottom && isHoveringIcon(iconX, y, mouseX, mouseY)) {
                    hovered = stack;
                }

                String line = Component.translatable("misc.craftorio.contract_reward_line",
                        reward.getAmountGiving(), stack.getHoverName().getString()).getString();
                graphics.drawString(this.font, line, iconX + ROW_ICON_SIZE + 6, y + (ROW_ICON_SIZE - this.font.lineHeight) / 2, 0xCCCCCC, false);
            }
            y += ROW_HEIGHT;
        }

        if (y + ROW_HEIGHT >= top && y <= bottom) {
            graphics.drawString(this.font, pointRewardLine(), iconX, y + (ROW_ICON_SIZE - this.font.lineHeight) / 2, 0xFFAA00, false);
        }

        graphics.disableScissor();
        renderScrollbar(graphics, iconX + columnWidth + 2, top, bottom, rowCount * ROW_HEIGHT, this.rewardScroll);

        return hovered;
    }

    private void renderScrollbar(GuiGraphics graphics, int x, int top, int bottom, int contentHeight, int scroll) {
        int[] thumb = thumbBounds(top, bottom, contentHeight, scroll);
        if (thumb == null) return;

        graphics.fill(x, top, x + SCROLLBAR_WIDTH, bottom, 0x40FFFFFF);
        graphics.fill(x, thumb[0], x + SCROLLBAR_WIDTH, thumb[0] + thumb[1], 0xFFAAAAAA);
    }

    private String pointRewardLine() {
        BigInteger baseValue = this.contract.getBasePointValue();
        String baseText = CraftorioMisc.bigIntFormat(baseValue, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());

        Player player = this.minecraft.player;
        if (player == null) {
            return Component.translatable("misc.craftorio.contract_point_reward", baseText).getString();
        }

        BigInteger totalValue = this.contract.getMultipliedPointValue(player);
        String totalText = CraftorioMisc.bigIntFormat(totalValue, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        float multiplierValue = CraftorioMisc.getCraftorioMultiplier(player);

        String rewardText = totalText;
        if (multiplierValue != 0) {
            rewardText += " (" + baseText + " * " + (multiplierValue + 1) + "x)";
        }

        return Component.translatable("misc.craftorio.contract_point_reward", rewardText).getString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

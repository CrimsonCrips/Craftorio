package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.networking.ClaimItemPurchasePacket;
import org.crimsoncrips.craftorio.server.CraftorioClaimItemShop;

import java.math.BigInteger;

@OnlyIn(Dist.CLIENT)
public class ClaimItemPurchaseScreen extends Screen {

    private final ItemStack displayStack = new ItemStack(CraftorioItems.CLAIM_ITEM.get());

    private int quantity = 1;
    private EditBox quantityBox;
    private Component errorMessage = CommonComponents.EMPTY;

    public ClaimItemPurchaseScreen() {
        super(Component.literal("Buy Claim Chunk Items"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.quantityBox = new EditBox(this.font, centerX - 50, centerY - 10, 100, 20, Component.literal("Quantity"));
        this.quantityBox.setValue(String.valueOf(quantity));
        this.quantityBox.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.quantityBox.setResponder(this::onQuantityTyped);
        this.addRenderableWidget(this.quantityBox);
        this.setInitialFocus(this.quantityBox);

        this.addRenderableWidget(Button.builder(Component.literal("Max-"), b -> setQuantity(0))
                .bounds(centerX - 94, centerY + 40, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> setQuantity(quantity - 1))
                .bounds(centerX - 46, centerY + 40, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> setQuantity(quantity + 1))
                .bounds(centerX + 2, centerY + 40, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Max+"), b -> setQuantity(maxAffordable()))
                .bounds(centerX + 50, centerY + 40, 44, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Buy"), b -> this.confirm())
                .bounds(centerX - 50, centerY + 66, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.minecraft.setScreen(null))
                .bounds(centerX + 2, centerY + 66, 48, 20).build());
    }

    private int maxAffordable() {
        Player player = this.minecraft.player;
        if (player == null) return 0;

        long cap = CraftorioMisc.expandCapabilityWithPoints(CraftorioMisc.getPoints(player), CraftorioMisc.getLandAmount(player));
        return (int) Math.min(cap, Integer.MAX_VALUE);
    }

    private void setQuantity(int newQuantity) {
        this.quantity = Math.max(0, newQuantity);
        this.quantityBox.setValue(String.valueOf(this.quantity));
    }

    private void onQuantityTyped(String value) {
        try {
            this.quantity = Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            this.quantity = 0;
        }
    }

    private void confirm() {
        if (quantity <= 0) {
            this.errorMessage = Component.literal("Enter a valid quantity").withColor(0xFF5555);
            return;
        }

        PacketDistributor.sendToServer(new ClaimItemPurchasePacket(quantity));
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.renderItem(this.displayStack, centerX - 8, centerY - 40);
        guiGraphics.drawCenteredString(this.font, this.displayStack.getHoverName(), centerX, centerY - 58, 0xFFFFFF);

        Player player = this.minecraft.player;
        if (player != null) {
            BigInteger totalCost = CraftorioClaimItemShop.getCost(player, quantity);

            CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 20, true, 0xFFAA00,
                    totalCost, " points total");
        }

        if (!this.errorMessage.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.errorMessage, centerX, centerY + 92, 0xFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

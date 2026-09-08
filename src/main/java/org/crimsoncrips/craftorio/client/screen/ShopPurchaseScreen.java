package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.ShopPurchasePacket;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioShop;
import org.crimsoncrips.craftorio.server.CraftorioShopCatalog.CatalogEntry;

import java.math.BigInteger;


@OnlyIn(Dist.CLIENT)
public class ShopPurchaseScreen extends Screen {

    private final CatalogEntry entry;
    private final Screen parent;

    private int quantity = 1;
    private EditBox quantityBox;
    private Component errorMessage = CommonComponents.EMPTY;

    public ShopPurchaseScreen(CatalogEntry entry, Screen parent) {
        super(Component.literal("Purchase " + entry.stack().getHoverName().getString()));
        this.entry = entry;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.quantityBox = new EditBox(this.font, centerX - 50, centerY + 6, 100, 20, Component.literal("Quantity"));
        this.quantityBox.setValue(String.valueOf(quantity));
        this.quantityBox.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.quantityBox.setResponder(this::onQuantityTyped);
        this.addRenderableWidget(this.quantityBox);
        this.setInitialFocus(this.quantityBox);

        this.addRenderableWidget(Button.builder(Component.literal("Max-"), b -> setQuantity(0))
                .bounds(centerX - 94, centerY + 34, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> setQuantity(quantity - 1))
                .bounds(centerX - 46, centerY + 34, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> setQuantity(quantity + 1))
                .bounds(centerX + 2, centerY + 34, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Max+"), b -> setQuantity(maxAffordable()))
                .bounds(centerX + 50, centerY + 34, 44, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Buy"), b -> this.confirm())
                .bounds(centerX - 50, centerY + 60, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.minecraft.setScreen(this.parent))
                .bounds(centerX + 2, centerY + 60, 48, 20).build());
    }

    private int maxAffordable() {
        Player player = this.minecraft.player;
        if (player == null) return 0;

        BigInteger unitPrice = CraftorioShop.getUnitPrice(player, this.entry.stack(), true);
        if (unitPrice.signum() <= 0) return 0;

        BigInteger max = CraftorioMisc.getPoints(player).divide(unitPrice);
        return max.min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
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

        PacketDistributor.sendToServer(new ShopPurchasePacket(this.entry.key(), quantity));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Player player = this.minecraft.player;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.renderItem(this.entry.stack(), centerX - 8, centerY - 40);
        guiGraphics.drawCenteredString(this.font, this.entry.stack().getHoverName(), centerX, centerY - 58, 0xFFFFFF);

        BigInteger unmodified_price = CraftorioShop.getUnitPrice(player, this.entry.stack(), false);
        BigInteger price = CraftorioShop.getUnitPrice(player, this.entry.stack(), true);
        double shop_multiplier = Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt();
        for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
            shop_multiplier += shopEffect.getMultiplier();
        }

        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 20, true, 0xFFAA00,
                price, " (", unmodified_price, " * " + shop_multiplier + ")", " points each");

        BigInteger totalPrice = price.multiply(BigInteger.valueOf(quantity));
        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 8, true, 0xFFDD55,
                totalPrice, " points total");

        if (!this.errorMessage.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.errorMessage, centerX, centerY + 86, 0xFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

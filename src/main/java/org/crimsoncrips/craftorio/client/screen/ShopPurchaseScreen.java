package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.ShopPurchasePacket;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioShop;

import java.math.BigInteger;


@OnlyIn(Dist.CLIENT)
public class ShopPurchaseScreen extends Screen {

    private final Item item;
    private final Screen parent;

    private EditBox quantityBox;
    private Component errorMessage = CommonComponents.EMPTY;

    public ShopPurchaseScreen(Item item, Screen parent) {
        super(Component.literal("Purchase " + new ItemStack(item).getHoverName().getString()));
        this.item = item;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.quantityBox = new EditBox(this.font, centerX - 50, centerY - 10, 100, 20, Component.literal("Quantity"));
        this.quantityBox.setValue("1");
        this.quantityBox.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.addRenderableWidget(this.quantityBox);
        this.setInitialFocus(this.quantityBox);

        this.addRenderableWidget(Button.builder(Component.literal("Buy"), b -> this.confirm())
                .bounds(centerX - 50, centerY + 16, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.minecraft.setScreen(this.parent))
                .bounds(centerX + 2, centerY + 16, 48, 20).build());
    }

    private void confirm() {
        int quantity;
        try {
            quantity = Integer.parseInt(this.quantityBox.getValue().trim());
        } catch (NumberFormatException e) {
            quantity = 0;
        }

        if (quantity <= 0) {
            this.errorMessage = Component.literal("Enter a valid quantity").withColor(0xFF5555);
            return;
        }

        PacketDistributor.sendToServer(new ShopPurchasePacket(BuiltInRegistries.ITEM.getKey(this.item), quantity));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Player player = this.minecraft.player;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        ItemStack stack = new ItemStack(this.item);
        guiGraphics.renderItem(stack, centerX - 8, centerY - 40);
        guiGraphics.drawCenteredString(this.font, stack.getHoverName(), centerX, centerY - 58, 0xFFFFFF);

        BigInteger unmodified_price = CraftorioShop.getUnitPrice(player, item,false);
        BigInteger price = CraftorioShop.getUnitPrice(player, item,true);
        double shop_multiplier = Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt();
        for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
            shop_multiplier += shopEffect.getMultiplier();
        }

        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 20, true, 0xFFAA00,
                price, " (", unmodified_price, " * " + shop_multiplier + ")", " points each");

        if (!this.errorMessage.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.errorMessage, centerX, centerY + 40, 0xFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

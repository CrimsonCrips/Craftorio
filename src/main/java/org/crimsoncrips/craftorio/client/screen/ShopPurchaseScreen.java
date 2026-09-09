package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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
public class ShopPurchaseScreen extends QuantityPurchaseScreen {

    private final CatalogEntry entry;
    private final Screen parent;

    public ShopPurchaseScreen(CatalogEntry entry, Screen parent) {
        super(Component.translatable("misc.craftorio.purchase_title", entry.stack().getHoverName()), entry.stack());
        this.entry = entry;
        this.parent = parent;
    }

    @Override
    protected int maxAffordable() {
        Player player = this.minecraft.player;
        if (player == null) return 0;

        BigInteger unitPrice = CraftorioShop.getUnitPrice(player, this.entry.stack(), true);
        if (unitPrice.signum() <= 0) return 0;

        BigInteger max = CraftorioMisc.getPoints(player).divide(unitPrice);
        return max.min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
    }

    @Override
    protected void onConfirm(int quantity) {
        PacketDistributor.sendToServer(new ShopPurchasePacket(this.entry.key(), quantity));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void onCancel() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void renderPriceInfo(GuiGraphics guiGraphics, int centerX, int centerY) {
        Player player = this.minecraft.player;

        BigInteger unmodified_price = CraftorioShop.getUnitPrice(player, this.entry.stack(), false);
        BigInteger price = CraftorioShop.getUnitPrice(player, this.entry.stack(), true);
        double shop_multiplier = Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt();
        for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
            shop_multiplier += shopEffect.getMultiplier();
        }

        String pointsEachSuffix = Component.translatable("misc.craftorio.points_each_suffix").getString();
        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 20, true, 0xFFAA00,
                price, " (", unmodified_price, " * " + shop_multiplier + ")", pointsEachSuffix);

        BigInteger totalPrice = price.multiply(BigInteger.valueOf(quantity));
        String pointsTotalSuffix = Component.translatable("misc.craftorio.points_total_suffix").getString();
        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 8, true, 0xFFDD55,
                totalPrice, pointsTotalSuffix);
    }
}

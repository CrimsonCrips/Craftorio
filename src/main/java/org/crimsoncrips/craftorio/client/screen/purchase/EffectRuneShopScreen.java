package org.crimsoncrips.craftorio.client.screen.purchase;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.networking.shop.EffectRunePurchasePacket;
import org.crimsoncrips.craftorio.server.shop.CraftorioEffectRuneShop;

import java.math.BigInteger;

@OnlyIn(Dist.CLIENT)
public class EffectRuneShopScreen extends QuantityPurchaseScreen {

    private final boolean mystery;

    public EffectRuneShopScreen(boolean mystery, int initialQuantity) {
        super(Component.translatable("misc.craftorio.effect_rune_shop_title"),
                new ItemStack(mystery ? CraftorioItems.MYSTERY_EFFECT_RUNE.get() : CraftorioItems.EFFECT_RUNE.get()));
        this.mystery = mystery;
        this.quantity = Math.max(1, initialQuantity);
    }

    public EffectRuneShopScreen() {
        this(false, 1);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(
                        Component.translatable(this.mystery ? "misc.craftorio.effect_rune_shop_type_mystery" : "misc.craftorio.effect_rune_shop_type_normal"),
                        b -> this.minecraft.setScreen(new EffectRuneShopScreen(!this.mystery, this.quantity)))
                .bounds(centerX - 60, centerY - 90, 120, 20).build());
    }

    @Override
    protected int maxAffordable() {
        Player player = this.minecraft.player;
        if (player == null) return 0;
        return CraftorioEffectRuneShop.maxAffordableCount(player, this.mystery);
    }

    @Override
    protected void onConfirm(int quantity) {
        PacketDistributor.sendToServer(new EffectRunePurchasePacket(this.mystery, quantity));
        this.minecraft.setScreen(null);
    }

    @Override
    protected void renderPriceInfo(GuiGraphics guiGraphics, int centerX, int centerY) {
        Player player = this.minecraft.player;
        if (player == null) return;

        BigInteger cost = CraftorioEffectRuneShop.price(Math.max(1, this.quantity), this.mystery);
        String pointsSuffix = Component.translatable("misc.craftorio.points_suffix").getString();

        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 20, true, 0xFFAA00,
                cost, pointsSuffix);
    }
}

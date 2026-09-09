package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
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
public class ClaimItemPurchaseScreen extends QuantityPurchaseScreen {

    public ClaimItemPurchaseScreen() {
        super(Component.translatable("misc.craftorio.claim_shop_title"), new ItemStack(CraftorioItems.CLAIM_ITEM.get()));
    }

    @Override
    protected int maxAffordable() {
        Player player = this.minecraft.player;
        if (player == null) return 0;

        long cap = CraftorioMisc.expandCapabilityWithPoints(CraftorioMisc.getPoints(player), CraftorioMisc.getLandAmount(player));
        return (int) Math.min(cap, Integer.MAX_VALUE);
    }

    @Override
    protected void onConfirm(int quantity) {
        PacketDistributor.sendToServer(new ClaimItemPurchasePacket(quantity));
        this.minecraft.setScreen(null);
    }

    @Override
    protected void renderPriceInfo(GuiGraphics guiGraphics, int centerX, int centerY) {
        Player player = this.minecraft.player;
        if (player == null) return;

        BigInteger totalCost = CraftorioClaimItemShop.getCost(player, quantity);
        String pointsTotalSuffix = Component.translatable("misc.craftorio.points_total_suffix").getString();

        CraftorioMisc.CraftorioTextEffects.drawCenteredLine(guiGraphics, this.font, centerX, centerY - 20, true, 0xFFAA00,
                totalCost, pointsTotalSuffix);
    }
}

package org.crimsoncrips.craftorio.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;

import java.math.BigInteger;

public final class CraftorioClaimItemShop {

    private CraftorioClaimItemShop() {}

    public static BigInteger getCost(Player player, long quantity) {
        BigInteger rawCost = CraftorioMisc.pointsToExpand(quantity, CraftorioMisc.getLandAmount(player));
        return CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.EXPANSION_COST, rawCost).max(BigInteger.ZERO);
    }

    public static void purchase(ServerPlayer player, int quantity) {
        if (quantity <= 0) return;

        BigInteger totalPrice = getCost(player, quantity);
        BigInteger points = CraftorioMisc.getPoints(player);
        if (points.compareTo(totalPrice) < 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack claimItemStack = new ItemStack(CraftorioItems.CLAIM_ITEM.get());
        CraftorioMisc.setPoints(points.subtract(totalPrice), player);
        CraftorioMisc.giveItemsSplitByStack(player, claimItemStack, quantity);

        player.sendSystemMessage(Component.translatable(
                "misc.craftorio.purchased_items", quantity, claimItemStack.getHoverName()
        ).withStyle(ChatFormatting.GREEN));
    }
}

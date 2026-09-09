package org.crimsoncrips.craftorio.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;

import java.math.BigInteger;

public final class CraftorioClaimItemShop {

    private CraftorioClaimItemShop() {}

    public static BigInteger getCost(Player player, long quantity) {
        return CraftorioMisc.pointsToExpand(quantity, CraftorioMisc.getLandAmount(player));
    }

    public static void purchase(ServerPlayer player, int quantity) {
        if (quantity <= 0) return;

        BigInteger totalPrice = getCost(player, quantity);
        BigInteger points = CraftorioMisc.getPoints(player);
        if (points.compareTo(totalPrice) < 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
            return;
        }

        CraftorioMisc.setPoints(points.subtract(totalPrice), player);
        giveItems(player, quantity);

        ItemStack claimItemStack = new ItemStack(CraftorioItems.CLAIM_ITEM.get());
        player.sendSystemMessage(Component.translatable(
                "misc.craftorio.purchased_items", quantity, claimItemStack.getHoverName()
        ).withStyle(ChatFormatting.GREEN));
    }

    private static void giveItems(ServerPlayer player, int quantity) {
        int remaining = quantity;
        while (remaining > 0) {
            ItemStack stack = new ItemStack(CraftorioItems.CLAIM_ITEM.get());
            int amount = Math.min(remaining, stack.getMaxStackSize());
            stack.setCount(amount);
            player.addItem(stack);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
            remaining -= amount;
        }
    }
}

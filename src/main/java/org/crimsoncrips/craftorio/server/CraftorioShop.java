package org.crimsoncrips.craftorio.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;

import java.math.BigDecimal;
import java.math.BigInteger;


public class CraftorioShop {

    public static boolean isEnabled() {
        return Craftorio.SERVER_CONFIG.SHOP_MODE.get() != CraftorioShopMode.DISABLED;
    }


    public static BigInteger getUnitPrice(Player player, Item item,boolean applyCostIncrease) {
        BigInteger bigInteger = CraftorioMisc.checkValue(new ItemStack(item, 1), player, false);

        BigDecimal result = new BigDecimal(BigInteger.valueOf(Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt()));

        for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
            result = result.add(BigDecimal.valueOf(shopEffect.getMultiplier()));
        }

        return applyCostIncrease ? bigInteger.multiply(result.toBigInteger()) : bigInteger;
    }

    public static void purchase(ServerPlayer player, Item item, int quantity) {
        if (quantity <= 0) return;

        CraftorioShopMode mode = Craftorio.SERVER_CONFIG.SHOP_MODE.get();
        if (mode == CraftorioShopMode.DISABLED) {
            player.sendSystemMessage(Component.literal("The shop is disabled.").withStyle(ChatFormatting.RED));
            return;
        }

        BigInteger unitPrice = getUnitPrice(player, item,true);
        if (unitPrice.signum() <= 0) {
            player.sendSystemMessage(Component.literal("That item isn't sold in the shop.").withStyle(ChatFormatting.RED));
            return;
        }

        BigInteger totalPrice = unitPrice.multiply(BigInteger.valueOf(quantity));
        BigInteger points = CraftorioMisc.getPoints(player);

        if (points.compareTo(totalPrice) < 0) {
            player.sendSystemMessage(Component.literal("You don't have enough points for that.").withStyle(ChatFormatting.RED));
            return;
        }

        CraftorioMisc.setPoints(points.subtract(totalPrice), player);
        giveItems(player, item, quantity);

        player.sendSystemMessage(Component.literal(
                "Purchased " + quantity + "x " + new ItemStack(item).getHoverName().getString() + "."
        ).withStyle(ChatFormatting.GREEN));
    }

    private static void giveItems(ServerPlayer player, Item item, int quantity) {
        int remaining = quantity;
        while (remaining > 0) {
            ItemStack stack = new ItemStack(item);
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

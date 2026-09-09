package org.crimsoncrips.craftorio.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    public static BigInteger getUnitPrice(Player player, Item item, boolean applyCostIncrease) {
        return getUnitPrice(player, new ItemStack(item, 1), applyCostIncrease);
    }

    public static BigInteger getUnitPrice(Player player, ItemStack template, boolean applyCostIncrease) {
        BigInteger bigInteger = CraftorioMisc.checkValue(template, player, false);

        BigDecimal result = new BigDecimal(BigInteger.valueOf(Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt()));

        for (ShopMultiplierEffect shopEffect : CraftorioMisc.getShopEffects(player)) {
            result = result.add(BigDecimal.valueOf(shopEffect.getMultiplier()));
        }

        return applyCostIncrease ? bigInteger.multiply(result.toBigInteger()) : bigInteger;
    }

    public static void purchase(ServerPlayer player, ResourceLocation key, int quantity) {
        CraftorioShopCatalog.resolve(player.registryAccess(), key)
                .ifPresent(template -> purchase(player, template, quantity));
    }

    public static void purchase(ServerPlayer player, ItemStack template, int quantity) {
        if (quantity <= 0) return;

        CraftorioShopMode mode = Craftorio.SERVER_CONFIG.SHOP_MODE.get();
        if (mode == CraftorioShopMode.DISABLED) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.shop_disabled").withStyle(ChatFormatting.RED));
            return;
        }

        BigInteger unitPrice = getUnitPrice(player, template, true);
        if (unitPrice.signum() <= 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.item_not_sold").withStyle(ChatFormatting.RED));
            return;
        }

        BigInteger totalPrice = unitPrice.multiply(BigInteger.valueOf(quantity));
        BigInteger points = CraftorioMisc.getPoints(player);

        if (points.compareTo(totalPrice) < 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
            return;
        }

        CraftorioMisc.setPoints(points.subtract(totalPrice), player);
        CraftorioMisc.giveItemsSplitByStack(player, template, quantity);

        player.sendSystemMessage(Component.translatable(
                "misc.craftorio.purchased_items", quantity, template.getHoverName()
        ).withStyle(ChatFormatting.GREEN));
    }

}

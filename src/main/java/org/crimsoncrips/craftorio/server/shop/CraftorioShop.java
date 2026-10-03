package org.crimsoncrips.craftorio.server.shop;

import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
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
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;


public class CraftorioShop {

    public static boolean isEnabled() {
        return Craftorio.SERVER_CONFIG.SHOP_MODE.get() != CraftorioShopMode.DISABLED;
    }

    public static double shopCostMultiplier(Player player) {
        double additive = Craftorio.SERVER_CONFIG.SHOP_COST_MULTIPLIER.getAsInt();
        double factor = 1;
        for (ShopMultiplierEffect shopEffect : CraftorioMisc.getEffects(player, CraftorioDataAttachments.SHOP_MULTIPLIER_EFFECTS)) {
            additive += shopEffect.additiveContribution();
            factor *= shopEffect.factorContribution();
        }
        return additive * factor;
    }

    public static BigInteger getUnitPrice(Player player, Item item, boolean applyCostIncrease) {
        return getUnitPrice(player, new ItemStack(item, 1), applyCostIncrease);
    }

    public static BigInteger getUnitPrice(Player player, ItemStack template, boolean applyCostIncrease) {
        BigInteger bigInteger = CraftorioMisc.checkValue(template, player, false);

        if (!applyCostIncrease) return bigInteger;

        BigInteger multiplied = new BigDecimal(bigInteger).multiply(BigDecimal.valueOf(shopCostMultiplier(player)))
                .setScale(0, RoundingMode.HALF_UP).toBigInteger();
        return CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.SHOP_COST, multiplied);
    }

    public static void purchase(ServerPlayer player, ResourceLocation key, int quantity) {
        CraftorioShopCatalog.resolve(player.registryAccess(), key)
                .ifPresent(template -> purchase(player, template, quantity));
    }

    public static void purchase(ServerPlayer player, ItemStack template, int quantity) {
        if (quantity <= 0) return;

        if (!isEnabled()) return;

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

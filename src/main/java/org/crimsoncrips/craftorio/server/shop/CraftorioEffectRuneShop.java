package org.crimsoncrips.craftorio.server.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class CraftorioEffectRuneShop {

    public static final ResourceLocation UNLOCK_UPGRADE = Craftorio.prefix("effect_rune_shop_unlock");

    private CraftorioEffectRuneShop() {}

    public static boolean isUnlocked(Player player) {
        return CraftorioMisc.hasUnlockedUpgrade(player, UNLOCK_UPGRADE);
    }

    public static int maxEffectCount() {
        return Craftorio.SERVER_CONFIG.EFFECT_RUNE_MAX_EFFECTS.get();
    }

    public static BigInteger price(int effectCount, boolean mystery) {
        int count = Mth.clamp(effectCount, 1, maxEffectCount());
        BigInteger base = CraftorioMisc.scientificToInt(Craftorio.SERVER_CONFIG.EFFECT_RUNE_BASE_PRICE.get());
        BigInteger step = BigInteger.valueOf(Craftorio.SERVER_CONFIG.EFFECT_RUNE_PRICE_MULTIPLIER.get());
        BigInteger cost = base.multiply(step.pow(count - 1));
        if (mystery) {
            cost = cost.divide(BigInteger.TWO);
        }
        return cost.min(CraftorioMisc.pointThreshold());
    }

    public static int maxAffordableCount(Player player, boolean mystery) {
        BigInteger points = CraftorioMisc.getPoints(player);
        if (points.signum() <= 0 || price(1, mystery).compareTo(points) > 0) return 0;

        int max = maxEffectCount();
        int count = 1;
        while (count < max && price(count + 1, mystery).compareTo(points) <= 0) {
            count++;
        }
        return count;
    }

    public static void purchase(ServerPlayer player, int effectCount, boolean mystery) {
        if (!isUnlocked(player)) return;

        int count = Mth.clamp(effectCount, 1, maxEffectCount());
        BigInteger cost = price(count, mystery);
        BigInteger points = CraftorioMisc.getPoints(player);
        if (points.compareTo(cost) < 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
            return;
        }

        List<CraftorioEffects> effects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            effects.add(CraftorioMisc.getRandomEffect(player.level().registryAccess(), player.getRandom()));
        }

        CraftorioMisc.setPoints(points.subtract(cost), player);
        Item item = mystery ? CraftorioItems.MYSTERY_EFFECT_RUNE.get() : CraftorioItems.EFFECT_RUNE.get();
        CraftorioMisc.giveEffectRune(item, player, effects);

        player.sendSystemMessage(Component.translatable("misc.craftorio.effect_rune_purchased", item.getDefaultInstance().getHoverName())
                .withStyle(ChatFormatting.GREEN));
    }
}

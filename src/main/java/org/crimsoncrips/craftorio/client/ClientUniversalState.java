package org.crimsoncrips.craftorio.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ClientUniversalState {

    private static boolean available = false;
    private static BigInteger points = BigInteger.ZERO;
    private static BigInteger highestPoints = BigInteger.ZERO;
    private static BigInteger tempPoints = BigInteger.ZERO;
    private static long landAmount = 0L;
    private static Set<ResourceLocation> unlockedUpgrades = Set.of();
    private static List<GeneralMultiplierEffect> generalEffects = List.of();
    private static List<TagMultiplierEffect> tagEffects = List.of();
    private static List<ShopMultiplierEffect> shopEffects = List.of();
    private static double advancementMultiplierBonus = 0.0;
    private static List<CraftorioShipmentContract> contracts = List.of();
    private static List<CraftorioBorder> borders = List.of();

    private ClientUniversalState() {
    }

    public static void update(BigInteger points, BigInteger highestPoints, BigInteger tempPoints, long landAmount,
                               Set<ResourceLocation> unlockedUpgrades,
                               List<GeneralMultiplierEffect> generalEffects,
                               List<TagMultiplierEffect> tagEffects,
                               List<ShopMultiplierEffect> shopEffects,
                               double advancementMultiplierBonus,
                               List<CraftorioShipmentContract> contracts,
                               List<CraftorioBorder> borders) {
        available = true;
        ClientUniversalState.points = points;
        ClientUniversalState.highestPoints = highestPoints;
        ClientUniversalState.tempPoints = tempPoints;
        ClientUniversalState.landAmount = landAmount;
        ClientUniversalState.unlockedUpgrades = unlockedUpgrades;
        ClientUniversalState.generalEffects = generalEffects;
        ClientUniversalState.tagEffects = tagEffects;
        ClientUniversalState.shopEffects = shopEffects;
        ClientUniversalState.advancementMultiplierBonus = advancementMultiplierBonus;
        ClientUniversalState.contracts = contracts;
        ClientUniversalState.borders = borders;
    }

    public static void clear() {
        available = false;
    }

    public static boolean isAvailable() {
        return available;
    }

    public static BigInteger getPoints() {
        return points;
    }

    public static BigInteger getHighestPoints() {
        return highestPoints;
    }

    public static BigInteger getTempPoints() {
        return tempPoints;
    }

    public static long getLandAmount() {
        return landAmount;
    }

    public static Set<ResourceLocation> getUnlockedUpgrades() {
        return unlockedUpgrades;
    }

    public static List<GeneralMultiplierEffect> getGeneralEffects() {
        return generalEffects;
    }

    public static List<TagMultiplierEffect> getTagEffects() {
        return tagEffects;
    }

    public static List<ShopMultiplierEffect> getShopEffects() {
        return shopEffects;
    }

    public static double getAdvancementMultiplierBonus() {
        return advancementMultiplierBonus;
    }

    public static List<CraftorioShipmentContract> getContracts() {
        return contracts;
    }

    public static List<CraftorioBorder> getBorders() {
        return borders;
    }
}

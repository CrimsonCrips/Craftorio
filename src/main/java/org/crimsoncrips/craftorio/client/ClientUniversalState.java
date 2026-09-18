package org.crimsoncrips.craftorio.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientUniversalState {

    private static boolean available = false;
    private static BigInteger points = BigInteger.ZERO;
    private static BigInteger highestPoints = BigInteger.ZERO;
    private static BigInteger tempPoints = BigInteger.ZERO;
    private static long landAmount = 0L;
    private static Map<ResourceLocation, Integer> unlockedUpgrades = Map.of();
    private static List<GeneralMultiplierEffect> generalEffects = List.of();
    private static List<TagMultiplierEffect> tagEffects = List.of();
    private static List<ShopMultiplierEffect> shopEffects = List.of();
    private static double advancementMultiplierBonus = 0.0;
    private static List<CraftorioContract> contracts = List.of();
    private static List<CraftorioBorder> borders = List.of();
    private static int contractsCompleted = 0;
    private static float highestMultiplier = 0.0F;
    private static int life = 1;
    private static BigInteger lifePoints = BigInteger.ZERO;
    private static Map<ResourceLocation, Integer> rebirthUpgradesUnlocked = Map.of();
    private static BigInteger overallHighestPoints = BigInteger.ZERO;
    private static int overallContractsCompleted = 0;
    private static Map<ResourceLocation, Long> overallItemsSinked = Map.of();

    private ClientUniversalState() {
    }

    public static void update(BigInteger points, BigInteger highestPoints, BigInteger tempPoints, long landAmount,
                               Map<ResourceLocation, Integer> unlockedUpgrades,
                               List<GeneralMultiplierEffect> generalEffects,
                               List<TagMultiplierEffect> tagEffects,
                               List<ShopMultiplierEffect> shopEffects,
                               double advancementMultiplierBonus,
                               List<CraftorioContract> contracts,
                               List<CraftorioBorder> borders,
                               int contractsCompleted,
                               float highestMultiplier,
                               int life,
                               BigInteger lifePoints,
                               Map<ResourceLocation, Integer> rebirthUpgradesUnlocked,
                               BigInteger overallHighestPoints,
                               int overallContractsCompleted,
                               Map<ResourceLocation, Long> overallItemsSinked) {
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
        ClientUniversalState.contractsCompleted = contractsCompleted;
        ClientUniversalState.highestMultiplier = highestMultiplier;
        ClientUniversalState.life = life;
        ClientUniversalState.lifePoints = lifePoints;
        ClientUniversalState.rebirthUpgradesUnlocked = rebirthUpgradesUnlocked;
        ClientUniversalState.overallHighestPoints = overallHighestPoints;
        ClientUniversalState.overallContractsCompleted = overallContractsCompleted;
        ClientUniversalState.overallItemsSinked = overallItemsSinked;
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

    public static Map<ResourceLocation, Integer> getUpgradePurchaseCounts() {
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

    public static List<CraftorioContract> getContracts() {
        return contracts;
    }

    public static List<CraftorioBorder> getBorders() {
        return borders;
    }

    public static int getContractsCompleted() {
        return contractsCompleted;
    }

    public static float getHighestMultiplier() {
        return highestMultiplier;
    }

    public static int getLife() {
        return life;
    }

    public static BigInteger getLifePoints() {
        return lifePoints;
    }

    public static Map<ResourceLocation, Integer> getRebirthUpgradePurchaseCounts() {
        return rebirthUpgradesUnlocked;
    }

    public static BigInteger getOverallHighestPoints() {
        return overallHighestPoints;
    }

    public static int getOverallContractsCompleted() {
        return overallContractsCompleted;
    }

    public static Map<ResourceLocation, Long> getOverallItemsSinked() {
        return overallItemsSinked;
    }
}

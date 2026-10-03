package org.crimsoncrips.craftorio.server.skill_tree;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class SkillTreePurchasing {

    public enum Result { PURCHASED, MISSING, MAXED, LOCKED, TOO_EXPENSIVE }

    private static final int MAX_PURCHASES_PER_REQUEST = 100_000;

    private SkillTreePurchasing() {}

    public static Result purchase(ServerPlayer player, UpgradeTree tree, ResourceLocation id) {
        Registry<CraftorioUpgrade> registry = player.level().registryAccess().registryOrThrow(tree.registryKey());
        CraftorioUpgrade upgrade = registry.get(id);
        if (upgrade == null) return Result.MISSING;

        if (CraftorioMisc.getUpgradeCount(player, tree, id) >= upgrade.getMaxPurchases()) return Result.MAXED;
        if (upgrade.getParent().isPresent() && !CraftorioMisc.hasUnlockedUpgrade(player, tree, upgrade.getParent().get())) return Result.LOCKED;

        BigInteger cost = upgrade.getCost();
        BigInteger currency = CraftorioMisc.getCurrency(player, tree);
        if (currency.compareTo(cost) < 0) return Result.TOO_EXPENSIVE;

        int purchaseCount = CraftorioMisc.purchaseUpgrade(player, tree, id, upgrade.getMaxPurchases());
        if (purchaseCount < 0) return Result.MAXED;
        CraftorioMisc.setCurrency(player, tree, currency.subtract(cost));

        if (CraftorioMisc.universalBased(player.level())) {
            for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
                upgrade.onUnlock(other, id, purchaseCount);
            }
        } else {
            upgrade.onUnlock(player, id, purchaseCount);
        }
        return Result.PURCHASED;
    }

    public static int purchaseAll(ServerPlayer player, UpgradeTree tree) {
        Registry<CraftorioUpgrade> registry = player.level().registryAccess().registryOrThrow(tree.registryKey());
        RandomSource random = player.getRandom();
        int purchased = 0;

        while (purchased < MAX_PURCHASES_PER_REQUEST) {
            List<ResourceLocation> candidates = affordable(player, tree, registry);
            if (candidates.isEmpty()) break;

            ResourceLocation chosen = candidates.get(random.nextInt(candidates.size()));
            while (purchased < MAX_PURCHASES_PER_REQUEST && purchase(player, tree, chosen) == Result.PURCHASED) {
                purchased++;
            }
        }
        return purchased;
    }

    private static List<ResourceLocation> affordable(ServerPlayer player, UpgradeTree tree, Registry<CraftorioUpgrade> registry) {
        BigInteger currency = CraftorioMisc.getCurrency(player, tree);
        List<ResourceLocation> candidates = new ArrayList<>();
        for (Holder.Reference<CraftorioUpgrade> holder : registry.holders().toList()) {
            ResourceLocation id = holder.key().location();
            CraftorioUpgrade upgrade = holder.value();
            if (CraftorioMisc.getUpgradeCount(player, tree, id) >= upgrade.getMaxPurchases()) continue;
            if (upgrade.getParent().isPresent() && !CraftorioMisc.hasUnlockedUpgrade(player, tree, upgrade.getParent().get())) continue;
            if (currency.compareTo(upgrade.getCost()) < 0) continue;
            candidates.add(id);
        }
        return candidates;
    }

}

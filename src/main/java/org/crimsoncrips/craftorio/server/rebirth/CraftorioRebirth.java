package org.crimsoncrips.craftorio.server.rebirth;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.skill_tree.SkillTreeReveal;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.networking.contract.ContractOfferStatusPacket;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CraftorioRebirth {

    private static final double DEFAULT_BASE_COST = 2.5e19;
    private static final double TIER_BASE_FACTOR_RATIO = 0.4;

    private CraftorioRebirth() {}

    public static double baseCost() {
        try {
            double value = new BigDecimal(Craftorio.SERVER_CONFIG.REBIRTH_BASE_COST.get()).doubleValue();
            return value > 0 ? value : DEFAULT_BASE_COST;
        } catch (Exception e) {
            Craftorio.LOGGER.debug("INCORRECT INPUT FOR REBIRTH_BASE_COST IN Craftorio Server Config");
            return DEFAULT_BASE_COST;
        }
    }

    public static BigInteger rebirthCost(int life) {
        double x = life - 1;
        double price;
        double tier1Base = baseCost();
        double tierBaseFactor = tier1Base * TIER_BASE_FACTOR_RATIO;

        if (x < 40) {
            price = tier1Base * (x + 1);
        } else {
            double base = tierBaseFactor
                    * (5 * Math.floor(x / 5) + 2.5)
                    * (100 * Math.floor(x / 25) + 1)
                    * (1000 * Math.floor(x / 500) + 1);

            double exponent;
            if (x < 5000) {
                exponent = 0.00024 * x + 1;
            } else {
                double seventhRoot = Math.pow(12500.0 * Math.pow(x, 4), 1.0 / 7.0);
                exponent = 0.00024 * Math.floor(10 * seventhRoot) + 1;
            }

            price = Math.pow(base, exponent);
        }

        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) {
            return CraftorioMisc.pointThreshold();
        }

        BigInteger result = new BigDecimal(price).toBigInteger();
        return result.compareTo(CraftorioMisc.pointThreshold()) > 0 ? CraftorioMisc.pointThreshold() : result;
    }

    public static BigInteger skipCost(int life, int skipCount) {
        BigInteger total = BigInteger.ZERO;
        for (int i = 0; i <= skipCount; i++) {
            total = total.add(rebirthCost(life + i));
        }
        return total;
    }

    public static int maxAffordableSkip(int life, BigInteger availablePoints) {
        int maxSkip = Craftorio.SERVER_CONFIG.REBIRTH_MAX_SKIP.get();
        BigInteger total = rebirthCost(life);
        if (availablePoints.compareTo(total) < 0) return -1;

        int skip = 0;
        while (skip < maxSkip) {
            BigInteger next = total.add(rebirthCost(life + skip + 1));
            if (availablePoints.compareTo(next) < 0) break;
            total = next;
            skip++;
        }
        return skip;
    }

    public static BigInteger lifePointsEarned(int skipCount) {
        int baseline = Craftorio.SERVER_CONFIG.REBIRTH_BASE_LIFE_POINTS.get();
        double bonusPercent = Craftorio.SERVER_CONFIG.REBIRTH_SKIP_BONUS_PERCENT.get();

        double total = 0;
        for (int i = 0; i <= skipCount; i++) {
            total += baseline * (1 + i * bonusPercent);
        }
        return BigInteger.valueOf(Math.round(total));
    }

    public static boolean canRebirth(Player player) {
        return CraftorioMisc.getPoints(player).compareTo(rebirthCost(CraftorioMisc.getLife(player))) >= 0;
    }

    public static boolean performRebirth(ServerPlayer player, int requestedSkip) {
        int currentLife = CraftorioMisc.getLife(player);
        int maxSkip = Craftorio.SERVER_CONFIG.REBIRTH_MAX_SKIP.get();
        int skipCount = Mth.clamp(requestedSkip, 0, maxSkip);

        BigInteger cost = skipCost(currentLife, skipCount);
        BigInteger points = CraftorioMisc.getPoints(player);
        if (points.compareTo(cost) < 0) return false;

        applyRebirth(player, 1 + skipCount, lifePointsEarned(skipCount));
        return true;
    }

    public static void forceRebirth(ServerPlayer player) {
        applyRebirth(player, 1, BigInteger.ZERO);
    }

    public static final float SACRIFICE_REVEAL_FIRST_STAGE = 0.1f;

    public static boolean reachedSacrificeRevealStage(int life) {
        return life >= SACRIFICE_REVEAL_FIRST_STAGE * Craftorio.SERVER_CONFIG.SACRIFICE_REQUIRED_LIFE.getAsInt();
    }

    private static void applyRebirth(ServerPlayer player, int livesGained, BigInteger earned) {
        if (CraftorioMisc.universalBased(player.level()) && player.getServer() != null) {
            for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
                removeAttributeModifiers(online);
                renew(online);
            }
        } else {
            removeAttributeModifiers(player);
            renew(player);
        }

        CraftorioMisc.setPoints(CraftorioMisc.startingValue(), player);
        clearUpgrades(player);
        CraftorioMisc.removeEffectsIf(player, effect -> true);
        CraftorioMisc.setCraftorioContracts(player, List.of());
        resetContractOffer(player);
        clearItemsSinked(player);
        clearContractsCompleted(player);
        clearHighestPoints(player);

        int previousLife = CraftorioMisc.getLife(player);
        CraftorioMisc.setLife(previousLife + livesGained, player);
        CraftorioMisc.setLifePoints(CraftorioMisc.getLifePoints(player).add(earned), player);

        if (!reachedSacrificeRevealStage(previousLife) && reachedSacrificeRevealStage(CraftorioMisc.getLife(player))) {
            Component notice = Component.translatable("misc.craftorio.rebirth_notice").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
            List<ServerPlayer> recipients = CraftorioMisc.universalBased(player.level()) ? player.getServer().getPlayerList().getPlayers() : List.of(player);
            for (ServerPlayer recipient : recipients) {
                recipient.sendSystemMessage(notice);
            }
        }

        SkillTreeReveal.unlock(player, UpgradeTree.REBIRTH);
        ServerEvents.syncUniversalState(player);

    }

    private static void resetContractOffer(ServerPlayer player) {
        boolean universal = CraftorioMisc.universalBased(player.level()) && player.getServer() != null;
        int refreshTicks = Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS;

        CraftorioMisc.setContractOffer(player, new ArrayList<>());
        CraftorioMisc.setContractOfferClaimed(player, true);
        if (universal) {
            CraftorioMisc.setContractRefreshTime(player.getServer().overworld(), refreshTicks);
        } else {
            CraftorioMisc.setContractRefreshTime(player, refreshTicks);
        }

        List<ServerPlayer> affected = universal ? player.getServer().getPlayerList().getPlayers() : List.of(player);
        for (ServerPlayer online : affected) {
            PacketDistributor.sendToPlayer(online, new ContractOfferStatusPacket(false));
        }
    }

    private static void renew(ServerPlayer player) {
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
    }

    private static void removeAttributeModifiers(ServerPlayer player) {
        RegistryAccess registryAccess = player.level().registryAccess();
        Registry<CraftorioUpgrade> registry = registryAccess.registryOrThrow(CraftorioUpgrade.REGISTRY_KEY);

        for (ResourceLocation id : CraftorioMisc.getUpgradePurchaseCounts(player, UpgradeTree.BASIC).keySet()) {
            CraftorioUpgrade upgrade = registry.get(id);
            if (!(upgrade instanceof CraftorioAttributeUpgrade attributeUpgrade)) continue;

            Holder<Attribute> attribute = attributeUpgrade.getTarget().getAttribute();
            if (attribute == null) continue;

            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(id);
            }
        }
    }

    private static void clearUpgrades(Player player) {
        Level level = CraftorioMisc.universalLevel(player);
        if (CraftorioMisc.universalBased(level)) {
            level.setData(CraftorioDataAttachments.UNLOCKED_UPGRADES, new HashMap<>());
        } else {
            player.setData(CraftorioDataAttachments.UNLOCKED_UPGRADES, new HashMap<>());
        }
    }

    private static void clearItemsSinked(Player player) {
        Level level = CraftorioMisc.universalLevel(player);
        if (CraftorioMisc.universalBased(level)) {
            level.setData(CraftorioDataAttachments.ITEMS_SINKED, new HashMap<>());
        } else {
            player.setData(CraftorioDataAttachments.ITEMS_SINKED, new HashMap<>());
        }
    }

    private static void clearContractsCompleted(Player player) {
        Level level = CraftorioMisc.universalLevel(player);
        if (CraftorioMisc.universalBased(level)) {
            level.setData(CraftorioDataAttachments.CONTRACTS_COMPLETED, 0);
        } else {
            player.setData(CraftorioDataAttachments.CONTRACTS_COMPLETED, 0);
        }
    }

    private static void clearHighestPoints(Player player) {
        Level level = CraftorioMisc.universalLevel(player);
        if (CraftorioMisc.universalBased(level)) {
            level.setData(CraftorioDataAttachments.HIGHEST_REACHED_POINTS, CraftorioMisc.startingValue());
        } else {
            player.setData(CraftorioDataAttachments.HIGHEST_REACHED_POINTS, CraftorioMisc.startingValue());
        }
    }
}

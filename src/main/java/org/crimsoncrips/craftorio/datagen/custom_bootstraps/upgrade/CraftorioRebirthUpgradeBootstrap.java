package org.crimsoncrips.craftorio.datagen.custom_bootstraps.upgrade;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.target.AttributeTarget;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.DoubleOrNothingUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.EffectRuneShopUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.SinkValueScalingUpgrade;

public class CraftorioRebirthUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        Holder.Reference<CraftorioUpgrade> rebirthRootUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rebirth_root")
                .icon(DEFAULT_ICON)
                .description("misc.craftorio.upgrade_rebirth_root_description")
                .cost(5)
                .position(0.0, 0.0)
                .saveRebirth(context, Craftorio.prefix("rebirth_root"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> doubleOrNothingUnlockUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_double_or_nothing_unlock")
                .icon(DEFAULT_ICON)
                .parent(rebirthRootUpgrade)
                .description("misc.craftorio.upgrade_double_or_nothing_unlock_description")
                .cost(30)
                .position(64.0, 0.0)
                .saveRebirth(context, Craftorio.prefix("double_or_nothing_unlock"),
                        DoubleOrNothingUnlockUpgrade::of);

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_bet_bonus")
                .icon(DEFAULT_ICON)
                .parent(doubleOrNothingUnlockUpgrade)
                .description("misc.craftorio.upgrade_bet_bonus_description")
                .cost(50)
                .maxPurchases(4)
                .levelCosts(50, 100, 200, 400)
                .position(128.0, -64.0)
                .saveRebirth(context, Craftorio.prefix("bet_bonus"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BET_BONUS, UpgradeOperation.ADD, 0.25));

        Holder.Reference<CraftorioUpgrade> betOddsUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_bet_odds")
                .icon(DEFAULT_ICON)
                .parent(doubleOrNothingUnlockUpgrade)
                .description("misc.craftorio.upgrade_bet_odds_description")
                .cost(60)
                .maxPurchases(3)
                .levelCosts(60, 120, 240)
                .position(128.0, 0.0)
                .saveRebirth(context, Craftorio.prefix("bet_odds"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BET_ODDS, UpgradeOperation.ADD, 0.03));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_lost_bet_refund")
                .icon(DEFAULT_ICON)
                .parent(betOddsUpgrade)
                .description("misc.craftorio.upgrade_lost_bet_refund_description")
                .cost(80)
                .maxPurchases(3)
                .levelCosts(80, 160, 320)
                .position(192.0, 0.0)
                .saveRebirth(context, Craftorio.prefix("lost_bet_refund"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.LOST_BET_REFUND, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> effectRuneShopUnlockUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_rune_shop_unlock")
                .icon(DEFAULT_ICON)
                .parent(rebirthRootUpgrade)
                .description("misc.craftorio.upgrade_effect_rune_shop_unlock_description")
                .cost(120)
                .position(0.0, -64.0)
                .saveRebirth(context, Craftorio.prefix("effect_rune_shop_unlock"),
                        EffectRuneShopUnlockUpgrade::of);

        Holder.Reference<CraftorioUpgrade> luckyStarUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_lucky_star")
                .icon(DEFAULT_ICON)
                .parent(effectRuneShopUnlockUpgrade)
                .description("misc.craftorio.upgrade_lucky_star_description")
                .cost(250)
                .position(0.0, -128.0)
                .saveRebirth(context, Craftorio.prefix("lucky_star"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.MULTIPLY, 0.5));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rebirth_rarer_effect")
                .icon(DEFAULT_ICON)
                .parent(luckyStarUpgrade)
                .description("misc.craftorio.upgrade_rebirth_rarer_effect_description")
                .cost(200)
                .maxPurchases(2)
                .levelCosts(200, 800)
                .position(0.0, -192.0)
                .saveRebirth(context, Craftorio.prefix("rebirth_rarer_effect"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.25));

        Holder.Reference<CraftorioUpgrade> standingClienteleUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_standing_clientele")
                .icon(DEFAULT_ICON)
                .parent(rebirthRootUpgrade)
                .description("misc.craftorio.upgrade_standing_clientele_description")
                .cost(250)
                .position(-64.0, 0.0)
                .saveRebirth(context, Craftorio.prefix("standing_clientele"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 0.5));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rebirth_rarer_contract")
                .icon(DEFAULT_ICON)
                .parent(standingClienteleUpgrade)
                .description("misc.craftorio.upgrade_rebirth_rarer_contract_description")
                .cost(200)
                .maxPurchases(2)
                .levelCosts(200, 800)
                .position(-128.0, 0.0)
                .saveRebirth(context, Craftorio.prefix("rebirth_rarer_contract"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.25));

        Holder.Reference<CraftorioUpgrade> echoesUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_echoes")
                .icon(DEFAULT_ICON)
                .parent(rebirthRootUpgrade)
                .description("misc.craftorio.upgrade_echoes_description")
                .cost(100)
                .maxPurchases(4)
                .levelCosts(100, 300, 900, 2700)
                .position(0.0, 64.0)
                .saveRebirth(context, Craftorio.prefix("echoes"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY, 0.25));

        Holder.Reference<CraftorioUpgrade> inheritedAppraisalUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_inherited_appraisal")
                .icon(DEFAULT_ICON)
                .parent(echoesUpgrade)
                .description("misc.craftorio.upgrade_inherited_appraisal_description")
                .cost(150)
                .maxPurchases(3)
                .levelCosts(150, 600, 2400)
                .position(0.0, 128.0)
                .saveRebirth(context, Craftorio.prefix("inherited_appraisal"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 0.5));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_sink_value_scaling")
                .icon(DEFAULT_ICON)
                .parent(inheritedAppraisalUpgrade)
                .description("misc.craftorio.upgrade_sink_value_scaling_description")
                .cost(300)
                .position(0.0, 192.0)
                .saveRebirth(context, Craftorio.prefix("sink_value_scaling"),
                        SinkValueScalingUpgrade::of);

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_land_grant")
                .icon(DEFAULT_ICON)
                .parent(echoesUpgrade)
                .description("misc.craftorio.upgrade_land_grant_description")
                .cost(100)
                .maxPurchases(2)
                .levelCosts(100, 400)
                .position(-64.0, 128.0)
                .saveRebirth(context, Craftorio.prefix("land_grant"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EXPANSION_COST, UpgradeOperation.DIVIDE, 1.0));

        Holder.Reference<CraftorioUpgrade> vitalityUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_vitality")
                .icon(DEFAULT_ICON)
                .parent(rebirthRootUpgrade)
                .description("misc.craftorio.upgrade_vitality_description")
                .cost(20)
                .maxPurchases(5)
                .levelCosts(20, 40, 80, 160, 320)
                .position(64.0, 64.0)
                .saveRebirth(context, Craftorio.prefix("vitality"),
                        b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.HEALTH, UpgradeOperation.ADD, 2.0));

        Holder.Reference<CraftorioUpgrade> fortitudeUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_fortitude")
                .icon(DEFAULT_ICON)
                .parent(vitalityUpgrade)
                .description("misc.craftorio.upgrade_fortitude_description")
                .cost(60)
                .maxPurchases(3)
                .levelCosts(60, 120, 240)
                .position(64.0, 128.0)
                .saveRebirth(context, Craftorio.prefix("fortitude"),
                        b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.DEFENSE, UpgradeOperation.ADD, 2.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_might")
                .icon(DEFAULT_ICON)
                .parent(fortitudeUpgrade)
                .description("misc.craftorio.upgrade_might_description")
                .cost(60)
                .maxPurchases(3)
                .levelCosts(60, 120, 240)
                .position(64.0, 192.0)
                .saveRebirth(context, Craftorio.prefix("might"),
                        b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.DAMAGE, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> swiftnessUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_swiftness")
                .icon(DEFAULT_ICON)
                .parent(vitalityUpgrade)
                .description("misc.craftorio.upgrade_swiftness_description")
                .cost(50)
                .maxPurchases(3)
                .levelCosts(50, 100, 200)
                .position(128.0, 128.0)
                .saveRebirth(context, Craftorio.prefix("swiftness"),
                        b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.SPEED, UpgradeOperation.MULTIPLY, 0.05));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_long_reach")
                .icon(DEFAULT_ICON)
                .parent(swiftnessUpgrade)
                .description("misc.craftorio.upgrade_long_reach_description")
                .cost(40)
                .maxPurchases(4)
                .levelCosts(40, 80, 160, 320)
                .position(192.0, 128.0)
                .saveRebirth(context, Craftorio.prefix("long_reach"),
                        b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.BLOCK_REACH, UpgradeOperation.ADD, 0.5));
    }
}

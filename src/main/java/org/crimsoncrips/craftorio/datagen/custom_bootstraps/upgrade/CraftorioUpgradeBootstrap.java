package org.crimsoncrips.craftorio.datagen.custom_bootstraps.upgrade;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.target.PlayerActionTarget;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioActionEffectUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioDifficultyUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.EffectTimerDisplayUnlockUpgrade;

import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;

public class CraftorioUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        Holder.Reference<CraftorioUpgrade> rootUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_root")
                .icon(DEFAULT_ICON)
                .description("misc.craftorio.upgrade_root_description")
                .cost(scientificToInt("100"))
                .position(0.0, 0.0)
                .save(context, Craftorio.prefix("root"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> mult1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_mult_1_description")
                .cost(scientificToInt("500"))
                .position(64.0, 0.0)
                .save(context, Craftorio.prefix("mult_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.25));

        Holder.Reference<CraftorioUpgrade> mult2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_2")
                .icon(DEFAULT_ICON)
                .parent(mult1Upgrade)
                .description("misc.craftorio.upgrade_mult_2_description")
                .cost(scientificToInt("1e4"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e4"), scientificToInt("5e4"), scientificToInt("2.5e5"))
                .position(128.0, 0.0)
                .save(context, Craftorio.prefix("mult_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.5));

        Holder.Reference<CraftorioUpgrade> mult3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_3")
                .icon(DEFAULT_ICON)
                .parent(mult2Upgrade)
                .description("misc.craftorio.upgrade_mult_3_description")
                .cost(scientificToInt("1e7"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e7"), scientificToInt("1e8"), scientificToInt("1e9"))
                .position(192.0, 0.0)
                .save(context, Craftorio.prefix("mult_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> mult4Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_4")
                .icon(DEFAULT_ICON)
                .parent(mult3Upgrade)
                .description("misc.craftorio.upgrade_mult_4_description")
                .cost(scientificToInt("1e12"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e12"), scientificToInt("1e14"))
                .position(256.0, 0.0)
                .save(context, Craftorio.prefix("mult_4"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 2.5));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_difficulty_multiplier")
                .icon(DEFAULT_ICON)
                .parent(mult3Upgrade)
                .description("misc.craftorio.upgrade_difficulty_multiplier_description")
                .cost(scientificToInt("1e11"))
                .position(192.0, 64.0)
                .save(context, Craftorio.prefix("difficulty_multiplier"),
                        b -> CraftorioDifficultyUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.0, 0.5, 1.0, 2.5));

        Holder.Reference<CraftorioUpgrade> multAmplifier1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_amplifier_1")
                .icon(DEFAULT_ICON)
                .parent(mult4Upgrade)
                .description("misc.craftorio.upgrade_mult_amplifier_1_description")
                .cost(scientificToInt("1e18"))
                .position(320.0, 0.0)
                .save(context, Craftorio.prefix("mult_amplifier_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY, 0.5));

        Holder.Reference<CraftorioUpgrade> multAmplifier2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_amplifier_2")
                .icon(DEFAULT_ICON)
                .parent(multAmplifier1Upgrade)
                .description("misc.craftorio.upgrade_mult_amplifier_2_description")
                .cost(scientificToInt("1e26"))
                .position(384.0, 0.0)
                .save(context, Craftorio.prefix("mult_amplifier_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY, 1.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_overdrive")
                .icon(DEFAULT_ICON)
                .parent(multAmplifier2Upgrade)
                .description("misc.craftorio.upgrade_overdrive_description")
                .cost(scientificToInt("1e33"))
                .position(448.0, 0.0)
                .save(context, Craftorio.prefix("overdrive"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY, 2.0));

        Holder.Reference<CraftorioUpgrade> baseValue1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_base_value_1_description")
                .cost(scientificToInt("2e3"))
                .maxPurchases(5)
                .levelCosts(scientificToInt("2e3"), scientificToInt("1e4"), scientificToInt("5e4"), scientificToInt("2.5e5"), scientificToInt("1e6"))
                .position(64.0, 64.0)
                .save(context, Craftorio.prefix("base_value_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 10.0));

        Holder.Reference<CraftorioUpgrade> baseValue2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_2")
                .icon(DEFAULT_ICON)
                .parent(baseValue1Upgrade)
                .description("misc.craftorio.upgrade_base_value_2_description")
                .cost(scientificToInt("1e8"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e8"), scientificToInt("1e9"), scientificToInt("1e10"))
                .position(128.0, 128.0)
                .save(context, Craftorio.prefix("base_value_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 100.0));

        Holder.Reference<CraftorioUpgrade> baseValue3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_3")
                .icon(DEFAULT_ICON)
                .parent(baseValue2Upgrade)
                .description("misc.craftorio.upgrade_base_value_3_description")
                .cost(scientificToInt("1e15"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e15"), scientificToInt("1e17"))
                .position(192.0, 192.0)
                .save(context, Craftorio.prefix("base_value_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 1000.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_value_surge_1")
                .icon(DEFAULT_ICON)
                .parent(baseValue2Upgrade)
                .description("misc.craftorio.upgrade_value_surge_1_description")
                .cost(scientificToInt("1e13"))
                .position(192.0, 128.0)
                .save(context, Craftorio.prefix("value_surge_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 0.5));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_value_surge_2")
                .icon(DEFAULT_ICON)
                .parent(baseValue3Upgrade)
                .description("misc.craftorio.upgrade_value_surge_2_description")
                .cost(scientificToInt("1e22"))
                .position(256.0, 192.0)
                .save(context, Craftorio.prefix("value_surge_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 1.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_manual_sink_value")
                .icon(DEFAULT_ICON)
                .parent(baseValue2Upgrade)
                .description("misc.craftorio.upgrade_manual_sink_value_description")
                .cost(scientificToInt("1e9"))
                .maxPurchases(4)
                .levelCosts(scientificToInt("1e9"), scientificToInt("1e12"), scientificToInt("1e15"), scientificToInt("1e18"))
                .position(128.0, 192.0)
                .save(context, Craftorio.prefix("manual_sink_value"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MANUAL_SINK_VALUE, UpgradeOperation.MULTIPLY, 0.25));

        Holder.Reference<CraftorioUpgrade> condenserCapUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_condenser_cap")
                .icon(DEFAULT_ICON)
                .parent(baseValue1Upgrade)
                .description("misc.craftorio.upgrade_condenser_cap_description")
                .cost(scientificToInt("1e10"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e10"), scientificToInt("1e13"), scientificToInt("1e16"))
                .position(64.0, 128.0)
                .save(context, Craftorio.prefix("condenser_cap"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.VALUE_CONDENSER_CAP, UpgradeOperation.MULTIPLY, 1.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_condenser_vault")
                .icon(DEFAULT_ICON)
                .parent(condenserCapUpgrade)
                .description("misc.craftorio.upgrade_condenser_vault_description")
                .cost(scientificToInt("1e20"))
                .position(64.0, 192.0)
                .save(context, Craftorio.prefix("condenser_vault"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.VALUE_CONDENSER_CAP, UpgradeOperation.ADD, 1000000000000000.0));

        Holder.Reference<CraftorioUpgrade> contractRefresh1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_refresh_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_contract_refresh_1_description")
                .cost(scientificToInt("1e6"))
                .maxPurchases(5)
                .levelCosts(scientificToInt("1e6"), scientificToInt("5e6"), scientificToInt("2.5e7"), scientificToInt("1e8"), scientificToInt("5e8"))
                .position(0.0, -64.0)
                .save(context, Craftorio.prefix("contract_refresh_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.ADD, 60.0));

        Holder.Reference<CraftorioUpgrade> contractRefresh2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_refresh_2")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_contract_refresh_2_description")
                .cost(scientificToInt("1e14"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e14"), scientificToInt("1e18"))
                .position(0.0, -128.0)
                .save(context, Craftorio.prefix("contract_refresh_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 0.25));

        Holder.Reference<CraftorioUpgrade> rarerContract1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_1")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_1_description")
                .cost(scientificToInt("1e7"))
                .maxPurchases(4)
                .levelCosts(scientificToInt("1e7"), scientificToInt("1e9"), scientificToInt("1e11"), scientificToInt("1e13"))
                .position(64.0, -128.0)
                .save(context, Craftorio.prefix("rarer_contract_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.05));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_2")
                .icon(DEFAULT_ICON)
                .parent(rarerContract1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_2_description")
                .cost(scientificToInt("1e19"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e19"), scientificToInt("1e24"))
                .position(64.0, -192.0)
                .save(context, Craftorio.prefix("rarer_contract_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.5));

        Holder.Reference<CraftorioUpgrade> refreshCostUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_refresh_cost")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_refresh_cost_description")
                .cost(scientificToInt("1e12"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e12"), scientificToInt("1e16"), scientificToInt("1e20"))
                .position(-64.0, -128.0)
                .save(context, Craftorio.prefix("refresh_cost"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_COST, UpgradeOperation.DIVIDE, 0.25));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_punishment_duration")
                .icon(DEFAULT_ICON)
                .parent(refreshCostUpgrade)
                .description("misc.craftorio.upgrade_punishment_duration_description")
                .cost(scientificToInt("1e15"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e15"), scientificToInt("1e21"))
                .position(-64.0, -192.0)
                .save(context, Craftorio.prefix("punishment_duration"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.PUNISHMENT_DURATION, UpgradeOperation.DIVIDE, 0.5));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_momentum")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh2Upgrade)
                .description("misc.craftorio.upgrade_contract_momentum_description")
                .cost(scientificToInt("1e10"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e10"), scientificToInt("1e13"), scientificToInt("1e16"))
                .position(0.0, -192.0)
                .save(context, Craftorio.prefix("contract_momentum"),
                        b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.CONTRACT_COMPLETE, Craftorio.prefix("contract_momentum"), 0.2));

        Holder.Reference<CraftorioUpgrade> effectTimer1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_effect_timer_1_description")
                .cost(scientificToInt("1e9"))
                .maxPurchases(5)
                .levelCosts(scientificToInt("1e9"), scientificToInt("5e9"), scientificToInt("2.5e10"), scientificToInt("1e11"), scientificToInt("5e11"))
                .position(-64.0, 0.0)
                .save(context, Craftorio.prefix("effect_timer_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.ADD, 60.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_display_unlock")
                .icon(DEFAULT_ICON)
                .parent(effectTimer1Upgrade)
                .description("misc.craftorio.upgrade_effect_timer_display_unlock_description")
                .cost(scientificToInt("1e9"))
                .position(-64.0, 64.0)
                .save(context, Craftorio.prefix("effect_timer_display_unlock"),
                        EffectTimerDisplayUnlockUpgrade::of);

        Holder.Reference<CraftorioUpgrade> effectTimer2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_2")
                .icon(DEFAULT_ICON)
                .parent(effectTimer1Upgrade)
                .description("misc.craftorio.upgrade_effect_timer_2_description")
                .cost(scientificToInt("1e16"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e16"), scientificToInt("1e21"))
                .position(-128.0, 0.0)
                .save(context, Craftorio.prefix("effect_timer_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.MULTIPLY, 0.25));

        Holder.Reference<CraftorioUpgrade> rarerEffect1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_1")
                .icon(DEFAULT_ICON)
                .parent(effectTimer1Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_1_description")
                .cost(scientificToInt("1e11"))
                .maxPurchases(4)
                .levelCosts(scientificToInt("1e11"), scientificToInt("1e13"), scientificToInt("1e15"), scientificToInt("1e17"))
                .position(-128.0, -64.0)
                .save(context, Craftorio.prefix("rarer_effect_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.05));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_2")
                .icon(DEFAULT_ICON)
                .parent(rarerEffect1Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_2_description")
                .cost(scientificToInt("1e20"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e20"), scientificToInt("1e25"))
                .position(-192.0, -64.0)
                .save(context, Craftorio.prefix("rarer_effect_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.5));

        Holder.Reference<CraftorioUpgrade> effectDurationUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_duration")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_effect_duration_description")
                .cost(scientificToInt("1e13"))
                .maxPurchases(4)
                .levelCosts(scientificToInt("1e13"), scientificToInt("1e16"), scientificToInt("1e19"), scientificToInt("1e22"))
                .position(-192.0, 0.0)
                .save(context, Craftorio.prefix("effect_duration"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_DURATION, UpgradeOperation.MULTIPLY, 0.25));

        Holder.Reference<CraftorioUpgrade> wakeUpProductiveUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_wake_up_productive")
                .icon(DEFAULT_ICON)
                .parent(effectDurationUpgrade)
                .description("misc.craftorio.upgrade_wake_up_productive_description")
                .cost(scientificToInt("1e12"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e12"), scientificToInt("1e14"), scientificToInt("1e16"))
                .position(-192.0, 64.0)
                .save(context, Craftorio.prefix("wake_up_productive"),
                        b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.WAKE_UP, Craftorio.prefix("productive"), 0.2));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_trade_economic_boom")
                .icon(DEFAULT_ICON)
                .parent(wakeUpProductiveUpgrade)
                .description("misc.craftorio.upgrade_trade_economic_boom_description")
                .cost(scientificToInt("1e12"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e12"), scientificToInt("1e14"), scientificToInt("1e16"))
                .position(-256.0, 64.0)
                .save(context, Craftorio.prefix("trade_economic_boom"),
                        b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.TRADE, Craftorio.prefix("economic_boom"), 0.2));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_spoils_of_war")
                .icon(DEFAULT_ICON)
                .parent(effectDurationUpgrade)
                .description("misc.craftorio.upgrade_spoils_of_war_description")
                .cost(scientificToInt("1e18"))
                .maxPurchases(2)
                .levelCosts(scientificToInt("1e18"), scientificToInt("1e22"))
                .position(-256.0, 0.0)
                .save(context, Craftorio.prefix("spoils_of_war"),
                        b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.RAID_WIN, Craftorio.prefix("spoils_of_war"), 0.25));

        Holder.Reference<CraftorioUpgrade> expansionCostUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_expansion_cost")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_expansion_cost_description")
                .cost(scientificToInt("1e5"))
                .maxPurchases(4)
                .levelCosts(scientificToInt("1e5"), scientificToInt("1e8"), scientificToInt("1e12"), scientificToInt("1e16"))
                .position(0.0, 64.0)
                .save(context, Craftorio.prefix("expansion_cost"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EXPANSION_COST, UpgradeOperation.DIVIDE, 0.25));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_shop_cost")
                .icon(DEFAULT_ICON)
                .parent(expansionCostUpgrade)
                .description("misc.craftorio.upgrade_shop_cost_description")
                .cost(scientificToInt("1e6"))
                .maxPurchases(3)
                .levelCosts(scientificToInt("1e6"), scientificToInt("1e10"), scientificToInt("1e14"))
                .position(0.0, 128.0)
                .save(context, Craftorio.prefix("shop_cost"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.SHOP_COST, UpgradeOperation.DIVIDE, 0.25));
    }
}

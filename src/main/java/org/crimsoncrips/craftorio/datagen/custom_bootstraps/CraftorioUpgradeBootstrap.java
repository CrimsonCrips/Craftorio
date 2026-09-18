package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.*;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioActionEffectUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.AdvancementMultiplierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.ContractCompletionScalingUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.DoubleOrNothingUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.SinkValueScalingUpgrade;

import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;

public class CraftorioUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        Holder.Reference<CraftorioUpgrade> betBonus1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_bet_bonus_1")
                .icon(DEFAULT_ICON)
                .parent(ResourceLocation.parse("craftorio:double_or_nothing_unlock"))
                .description("misc.craftorio.upgrade_bet_bonus_1_description")
                .cost(scientificToInt("620000000000"))
                .position(532.5230354446335, -43.61441019924491)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "bet_bonus_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BET_BONUS, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> betBonus2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_bet_bonus_2")
                .icon(DEFAULT_ICON)
                .parent(ResourceLocation.parse("craftorio:double_or_nothing_unlock"))
                .description("misc.craftorio.upgrade_bet_bonus_2_description")
                .cost(scientificToInt("320000000000000000000000000"))
                .position(571.5683916925362, 25.54235920302036)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "bet_bonus_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BET_BONUS, UpgradeOperation.ADD, 0.3));

        Holder.Reference<CraftorioUpgrade> betBonus3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_bet_bonus_3")
                .icon(DEFAULT_ICON)
                .parent(ResourceLocation.parse("craftorio:double_or_nothing_unlock"))
                .description("misc.craftorio.upgrade_bet_bonus_3_description")
                .cost(scientificToInt("3200000000000000000000000000000000000000000"))
                .position(518.7290249244992, 100.89613617030244)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "bet_bonus_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BET_BONUS, UpgradeOperation.ADD, 0.9));

        Holder.Reference<CraftorioUpgrade> lostBetRefund1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_lost_bet_refund_1")
                .icon(DEFAULT_ICON)
                .parent(ResourceLocation.parse("craftorio:double_or_nothing_unlock"))
                .description("misc.craftorio.upgrade_lost_bet_refund_1_description")
                .cost(scientificToInt("9200000000000000"))
                .position(445.6573657533515, -104.01292544229264)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "lost_bet_refund_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.LOST_BET_REFUND, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> lostBetRefund2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_lost_bet_refund_2")
                .icon(DEFAULT_ICON)
                .parent(lostBetRefund1Upgrade)
                .description("misc.craftorio.upgrade_lost_bet_refund_2_description")
                .cost(scientificToInt("86000000000000000000000000000000000000"))
                .position(457.2603581883347, -200.63977049472592)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "lost_bet_refund_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.LOST_BET_REFUND, UpgradeOperation.ADD, 0.3));

        Holder.Reference<CraftorioUpgrade> rootUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_root")
                .icon(DEFAULT_ICON)
                .description("misc.craftorio.upgrade_root_description")
                .cost(scientificToInt("100"))
                .position(0.0, 0.0)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "root"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> contractRefresh1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_refresh_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_contract_refresh_1_description")
                .cost(scientificToInt("1000000"))
                .position(16.364435808510795, 101.89011241455968)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "contract_refresh_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 1.1));

        Holder.Reference<CraftorioUpgrade> contractRefresh2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_refresh_2")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_contract_refresh_2_description")
                .cost(scientificToInt("10000000000000"))
                .position(285.30551855916224, 145.3533044962423)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "contract_refresh_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 1.3));

        Holder.Reference<CraftorioUpgrade> contractRefresh3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_refresh_3")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh2Upgrade)
                .description("misc.craftorio.upgrade_contract_refresh_3_description")
                .cost(scientificToInt("1000000000000000000000"))
                .position(415.8275362571488, 222.17813604314523)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "contract_refresh_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 1.5));

// Manual upgrade - replace factory below with your manual ActionUpgrade, e.g. YourManualUpgrade::of
        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_double_or_nothing_unlock")
                .icon(DEFAULT_ICON)
                .parent(ResourceLocation.parse("craftorio:root"))
                .description("misc.craftorio.upgrade_double_or_nothing_unlock_description")
                .cost(scientificToInt("31000000"))
                .position(347.4890348148663, 7.427466788962272)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "double_or_nothing_unlock"),
                        DoubleOrNothingUnlockUpgrade::of);

        Holder.Reference<CraftorioUpgrade> effectTimer1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_effect_timer_1_description")
                .cost(scientificToInt("1000000000"))
                .position(-143.6797028147919, -13.54766696385628)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "effect_timer_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> effectTimer2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_2")
                .icon(DEFAULT_ICON)
                .parent(effectTimer1Upgrade)
                .description("misc.craftorio.upgrade_effect_timer_2_description")
                .cost(scientificToInt("100000000000"))
                .position(-275.57756633203513, -66.07740217816789)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "effect_timer_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.ADD, 1.4000000000000001));

        Holder.Reference<CraftorioUpgrade> effectTimer3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_3")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_effect_timer_3_description")
                .cost(scientificToInt("100000000000"))
                .position(-355.4057883014768, 23.82634333821403)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "effect_timer_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.ADD, 2.4));

        Holder.Reference<CraftorioUpgrade> effectTimer4Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_timer_4")
                .icon(DEFAULT_ICON)
                .parent(effectTimer3Upgrade)
                .description("misc.craftorio.upgrade_effect_timer_4_description")
                .cost(scientificToInt("100000000000"))
                .position(-491.8642242450119, 108.6548358386134)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "effect_timer_4"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.ADD, 8.0));

        Holder.Reference<CraftorioUpgrade> expansionCost1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_expansion_cost_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_expansion_cost_1_description")
                .cost(scientificToInt("7000000000000"))
                .position(267.3897041911375, -140.39923781967815)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "expansion_cost_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EXPANSION_COST, UpgradeOperation.MULTIPLY, -0.1));

        Holder.Reference<CraftorioUpgrade> expansionCost2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_expansion_cost_2")
                .icon(DEFAULT_ICON)
                .parent(expansionCost1Upgrade)
                .description("misc.craftorio.upgrade_expansion_cost_2_description")
                .cost(scientificToInt("700000000000000000000000000000000000000000"))
                .position(312.86387622763084, -261.9502549086048)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "expansion_cost_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EXPANSION_COST, UpgradeOperation.MULTIPLY, -0.3));

        Holder.Reference<CraftorioUpgrade> mult1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_1")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_mult_1_description")
                .cost(scientificToInt("500"))
                .position(0.5355809456095741, -105.12537387183875)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "mult_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.2));

        Holder.Reference<CraftorioUpgrade> mult2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_2")
                .icon(DEFAULT_ICON)
                .parent(mult1Upgrade)
                .description("misc.craftorio.upgrade_mult_2_description")
                .cost(scientificToInt("10000"))
                .position(1.7159830427900415, -188.12249064686955)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "mult_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.5));

        Holder.Reference<CraftorioUpgrade> mult3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_3")
                .icon(DEFAULT_ICON)
                .parent(mult2Upgrade)
                .description("misc.craftorio.upgrade_mult_3_description")
                .cost(scientificToInt("100000"))
                .position(5.9836230300774895, -268.5991181672547)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "mult_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.8));

        Holder.Reference<CraftorioUpgrade> mult4Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_4")
                .icon(DEFAULT_ICON)
                .parent(mult3Upgrade)
                .description("misc.craftorio.upgrade_mult_4_description")
                .cost(scientificToInt("10000000"))
                .position(96.02459727859531, -351.3207610983721)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "mult_4"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> mult5Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_5")
                .icon(DEFAULT_ICON)
                .parent(mult4Upgrade)
                .description("misc.craftorio.upgrade_mult_5_description")
                .cost(scientificToInt("10000000000"))
                .position(121.82926223169542, -461.4994594279672)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "mult_5"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 2.0));

        Holder.Reference<CraftorioUpgrade> mult6Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_mult_6")
                .icon(DEFAULT_ICON)
                .parent(mult5Upgrade)
                .description("misc.craftorio.upgrade_mult_6_description")
                .cost(scientificToInt("1000000000000000"))
                .position(236.21205981385617, -553.9050753357576)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "mult_6"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY, 2.0));

        Holder.Reference<CraftorioUpgrade> punishmentDuration1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_punishment_duration_1")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh3Upgrade)
                .description("misc.craftorio.upgrade_punishment_duration_1_description")
                .cost(scientificToInt("5500000000000000000"))
                .position(593.1775187094652, 277.03869049792115)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "punishment_duration_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.PUNISHMENT_DURATION, UpgradeOperation.MULTIPLY, -0.25));

        Holder.Reference<CraftorioUpgrade> rarerContract1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_1")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_1_description")
                .cost(scientificToInt("90000000"))
                .position(-100.72445086904531, 168.58627056655507)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_contract_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.02));

        Holder.Reference<CraftorioUpgrade> rarerContract2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_2")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_2_description")
                .cost(scientificToInt("3000000000"))
                .position(-78.23974604370696, 206.34503263096445)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_contract_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.05));

        Holder.Reference<CraftorioUpgrade> rarerContract3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_3")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_3_description")
                .cost(scientificToInt("8000000000000"))
                .position(-30.28440273712357, 252.85723492752504)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_contract_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> rarerContract4Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_4")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_4_description")
                .cost(scientificToInt("40000000000000000000"))
                .position(64.08521339250535, 255.78462925523317)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_contract_4"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.5));

        Holder.Reference<CraftorioUpgrade> rarerContract5Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_5")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh1Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_5_description")
                .cost(scientificToInt("700000000000000000000000"))
                .position(114.51255150526089, 218.06167737685564)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_contract_5"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> rarerContract6Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_contract_6")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh3Upgrade)
                .description("misc.craftorio.upgrade_rarer_contract_6_description")
                .cost(scientificToInt("7000000000000000000000000000000"))
                .position(395.67076953967455, 326.75490655414575)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_contract_6"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.MULTIPLY, 3.0));

        Holder.Reference<CraftorioUpgrade> rarerEffect1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_1")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_1_description")
                .cost(scientificToInt("5000000000000"))
                .position(-305.4553820638518, -260.60668738414495)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_effect_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.02));

        Holder.Reference<CraftorioUpgrade> rarerEffect2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_2")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_2_description")
                .cost(scientificToInt("70000000000000"))
                .position(-308.8971871514172, -223.0240184617317)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_effect_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.05));

        Holder.Reference<CraftorioUpgrade> rarerEffect3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_3")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_3_description")
                .cost(scientificToInt("7000000000000000"))
                .position(-311.9408164043311, -187.2524387413839)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_effect_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.1));

        Holder.Reference<CraftorioUpgrade> rarerEffect4Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_4")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_4_description")
                .cost(scientificToInt("60000000000000000000000"))
                .position(-307.98275301375514, -151.3967829258773)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_effect_4"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.5));

        Holder.Reference<CraftorioUpgrade> rarerEffect5Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_rarer_effect_5")
                .icon(DEFAULT_ICON)
                .parent(effectTimer2Upgrade)
                .description("misc.craftorio.upgrade_rarer_effect_5_description")
                .cost(scientificToInt("100000000000000000000000000000000000"))
                .position(-308.4445446954528, -115.89296690055468)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "rarer_effect_5"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 1.0));

        Holder.Reference<CraftorioUpgrade> refreshCost1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_refresh_cost_1")
                .icon(DEFAULT_ICON)
                .parent(contractRefresh2Upgrade)
                .description("misc.craftorio.upgrade_refresh_cost_1_description")
                .cost(scientificToInt("870000000000000"))
                .position(237.33137676235734, 264.57362386112555)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "refresh_cost_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_COST, UpgradeOperation.MULTIPLY, -0.12));

        Holder.Reference<CraftorioUpgrade> refreshCost2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_refresh_cost_2")
                .icon(DEFAULT_ICON)
                .parent(refreshCost1Upgrade)
                .description("misc.craftorio.upgrade_refresh_cost_2_description")
                .cost(scientificToInt("24000000000000000000000000"))
                .position(246.8313767623573, 349.07362386112555)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "refresh_cost_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_COST, UpgradeOperation.MULTIPLY, -0.28));

        Holder.Reference<CraftorioUpgrade> baseValue1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_1")
                .icon(DEFAULT_ICON)
                .parent(mult3Upgrade)
                .description("misc.craftorio.upgrade_base_value_1_description")
                .cost(scientificToInt("120000"))
                .position(-66.91853619694659, -367.808166021831)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 10.0));

        Holder.Reference<CraftorioUpgrade> baseValue2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_2")
                .icon(DEFAULT_ICON)
                .parent(baseValue1Upgrade)
                .description("misc.craftorio.upgrade_base_value_2_description")
                .cost(scientificToInt("180000"))
                .position(-180.5076838621648, -295.2842295883677)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 20.0));

        Holder.Reference<CraftorioUpgrade> baseValue3Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_3")
                .icon(DEFAULT_ICON)
                .parent(baseValue1Upgrade)
                .description("misc.craftorio.upgrade_base_value_3_description")
                .cost(scientificToInt("200000"))
                .position(-211.09702391257912, -386.3144518555188)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_3"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 25.0));

        Holder.Reference<CraftorioUpgrade> baseValue4Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_4")
                .icon(DEFAULT_ICON)
                .parent(baseValue1Upgrade)
                .description("misc.craftorio.upgrade_base_value_4_description")
                .cost(scientificToInt("4000000"))
                .position(-66.54026288857465, -485.1527333337292)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_4"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 50.0));

        Holder.Reference<CraftorioUpgrade> baseValue5Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_5")
                .icon(DEFAULT_ICON)
                .parent(baseValue4Upgrade)
                .description("misc.craftorio.upgrade_base_value_5_description")
                .cost(scientificToInt("700000000"))
                .position(-162.10517879309842, -507.9351164337295)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_5"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 120.0));

        Holder.Reference<CraftorioUpgrade> baseValue6Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_6")
                .icon(DEFAULT_ICON)
                .parent(baseValue5Upgrade)
                .description("misc.craftorio.upgrade_base_value_6_description")
                .cost(scientificToInt("1000000000"))
                .position(-309.1354428713269, -495.9276197320903)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_6"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 130.0));

        Holder.Reference<CraftorioUpgrade> baseValue7Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_7")
                .icon(DEFAULT_ICON)
                .parent(baseValue5Upgrade)
                .description("misc.craftorio.upgrade_base_value_7_description")
                .cost(scientificToInt("2000000000"))
                .position(-204.07224829451553, -560.1169491897537)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_7"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.ADD, 135.0));

        Holder.Reference<CraftorioUpgrade> baseValue8Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_base_value_8")
                .icon(DEFAULT_ICON)
                .parent(baseValue4Upgrade)
                .description("misc.craftorio.upgrade_base_value_8_description")
                .cost(scientificToInt("9000000000000000"))
                .position(-50.425302613570636, -718.5935889573514)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "base_value_8"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 2.0));

        Holder.Reference<CraftorioUpgrade> manualSinkValue1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_manual_sink_value_1")
                .icon(DEFAULT_ICON)
                .parent(baseValue8Upgrade)
                .description("misc.craftorio.upgrade_manual_sink_value_1_description")
                .cost(scientificToInt("20000000000000000"))
                .position(-118.36978072713401, -798.6169229773282)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "manual_sink_value_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MANUAL_SINK_VALUE, UpgradeOperation.MULTIPLY, 0.25));

        Holder.Reference<CraftorioUpgrade> wakeUpProductiveUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_wake_up_productive")
                .icon(DEFAULT_ICON)
                .parent(manualSinkValue1Upgrade)
                .description("misc.craftorio.upgrade_wake_up_productive_description")
                .cost(scientificToInt("30000000000000000"))
                .position(-160.19353890940016, -860.0454193636273)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "wake_up_productive"),
                        b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.WAKE_UP, Craftorio.prefix("productive")));

        Holder.Reference<CraftorioUpgrade> tradeEconomicBoomUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_trade_economic_boom")
                .icon(DEFAULT_ICON)
                .parent(manualSinkValue1Upgrade)
                .description("misc.craftorio.upgrade_trade_economic_boom_description")
                .cost(scientificToInt("30000000000000000"))
                .position(-76.51602254486786, -860.0454193636273)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "trade_economic_boom"),
                        b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.TRADE, Craftorio.prefix("economic_boom")));

        Holder.Reference<CraftorioUpgrade> effectDuration1Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_duration_1")
                .icon(DEFAULT_ICON)
                .parent(effectTimer1Upgrade)
                .description("misc.craftorio.upgrade_effect_duration_1_description")
                .cost(scientificToInt("2000000000000000"))
                .position(-249.77878391924347, 79.28896480422381)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "effect_duration_1"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_DURATION, UpgradeOperation.MULTIPLY, 0.5));

        Holder.Reference<CraftorioUpgrade> effectDuration2Upgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_duration_2")
                .icon(DEFAULT_ICON)
                .parent(effectDuration1Upgrade)
                .description("misc.craftorio.upgrade_effect_duration_2_description")
                .cost(scientificToInt("6000000000000000000000000000000"))
                .position(-298.12166561049617, 144.39721033198776)
                .save(context, ResourceLocation.fromNamespaceAndPath("craftorio", "effect_duration_2"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_DURATION, UpgradeOperation.MULTIPLY, 1.0));
    }

    /* for later
            Holder.Reference<CraftorioUpgrade> advancement_multiplier = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_advancement_multiplier")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_advancement_multiplier_description")
                .cost(10000)
                .save(context, id("advancement_multiplier"), AdvancementMultiplierUpgrade::of);

        Holder.Reference<CraftorioUpgrade> sink_value_scaling = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_sink_value_scaling")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_sink_value_scaling_description")
                .cost(10000)
                .save(context, id("sink_value_scaling"), SinkValueScalingUpgrade::of);

        Holder.Reference<CraftorioUpgrade> contractCompletionScaling = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_completion_scaling")
                .icon(DEFAULT_ICON)
                .parent(rootUpgrade)
                .description("misc.craftorio.upgrade_contract_completion_scaling_description")
                .cost(10000)
                .save(context, id("contract_completion_scaling"), ContractCompletionScalingUpgrade::of);
     */

    private static ResourceLocation id(String path) {
        return Craftorio.prefix(path);
    }
}

package org.crimsoncrips.craftorio.datagen.custom_bootstraps.upgrade;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.AdvancementMultiplierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.BuildBlitzUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.ContractCompletionScalingUpgrade;

public class CraftorioSacrificeUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        Holder.Reference<CraftorioUpgrade> buildBlitzUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_build_blitz")
                .icon(DEFAULT_ICON)
                .description("misc.craftorio.upgrade_build_blitz_description")
                .cost(1)
                .position(0.0, 0.0)
                .saveSacrifice(context, Craftorio.prefix("build_blitz"),
                        BuildBlitzUnlockUpgrade::of);

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_build_blitz_reload")
                .icon(DEFAULT_ICON)
                .parent(buildBlitzUpgrade)
                .description("misc.craftorio.upgrade_build_blitz_reload_description")
                .cost(1)
                .maxPurchases(2)
                .levelCosts(1, 2)
                .position(64.0, 0.0)
                .saveSacrifice(context, Craftorio.prefix("build_blitz_reload"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BUILD_BLITZ_COOLDOWN, UpgradeOperation.SUBTRACT, 1800.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_build_blitz_coverage")
                .icon(DEFAULT_ICON)
                .parent(buildBlitzUpgrade)
                .description("misc.craftorio.upgrade_build_blitz_coverage_description")
                .cost(1)
                .maxPurchases(2)
                .levelCosts(1, 2)
                .position(64.0, 64.0)
                .saveSacrifice(context, Craftorio.prefix("build_blitz_coverage"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BUILD_BLITZ_COVERAGE, UpgradeOperation.ADD, 15.0));

        Holder.Reference<CraftorioUpgrade> advancementMultiplierUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_advancement_multiplier")
                .icon(DEFAULT_ICON)
                .parent(buildBlitzUpgrade)
                .description("misc.craftorio.upgrade_advancement_multiplier_description")
                .cost(2)
                .position(-64.0, 0.0)
                .saveSacrifice(context, Craftorio.prefix("advancement_multiplier"),
                        AdvancementMultiplierUpgrade::of);

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_eternal_flame")
                .icon(DEFAULT_ICON)
                .parent(advancementMultiplierUpgrade)
                .description("misc.craftorio.upgrade_eternal_flame_description")
                .cost(3)
                .position(-128.0, 0.0)
                .saveSacrifice(context, Craftorio.prefix("eternal_flame"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY, 2.0));

        Holder.Reference<CraftorioUpgrade> contractCompletionScalingUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_completion_scaling")
                .icon(DEFAULT_ICON)
                .parent(buildBlitzUpgrade)
                .description("misc.craftorio.upgrade_contract_completion_scaling_description")
                .cost(2)
                .position(0.0, -64.0)
                .saveSacrifice(context, Craftorio.prefix("contract_completion_scaling"),
                        ContractCompletionScalingUpgrade::of);

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_renown_boost")
                .icon(DEFAULT_ICON)
                .parent(contractCompletionScalingUpgrade)
                .description("misc.craftorio.upgrade_renown_boost_description")
                .cost(2)
                .position(0.0, -128.0)
                .saveSacrifice(context, Craftorio.prefix("renown_boost"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULT_PER_CONTRACT_DONE, UpgradeOperation.ADD, 0.01));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_sacred_appraisal")
                .icon(DEFAULT_ICON)
                .parent(buildBlitzUpgrade)
                .description("misc.craftorio.upgrade_sacred_appraisal_description")
                .cost(3)
                .position(0.0, 64.0)
                .saveSacrifice(context, Craftorio.prefix("sacred_appraisal"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 2.0));
    }
}

package org.crimsoncrips.craftorio.datagen.custom_bootstraps.upgrade;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.BuildBlitzUnlockUpgrade;

public class CraftorioSacrificeUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        Holder.Reference<CraftorioUpgrade> buildBlitz = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_build_blitz")
                .icon(DEFAULT_ICON)
                .description("misc.craftorio.upgrade_build_blitz_description")
                .cost(1)
                .position(0.0, 0.0)
                .saveSacrifice(context, Craftorio.prefix("build_blitz"), BuildBlitzUnlockUpgrade::of);

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_build_blitz_reload")
                .icon(DEFAULT_ICON)
                .parent(buildBlitz)
                .description("misc.craftorio.upgrade_build_blitz_reload_description")
                .cost(1)
                .maxPurchases(4)
                .position(80.0, 0.0)
                .saveSacrifice(context, Craftorio.prefix("build_blitz_reload"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BUILD_BLITZ_COOLDOWN, UpgradeOperation.SUBTRACT, 5 * 60));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_build_blitz_coverage")
                .icon(DEFAULT_ICON)
                .parent(buildBlitz)
                .description("misc.craftorio.upgrade_build_blitz_coverage_description")
                .cost(1)
                .maxPurchases(4)
                .position(80.0, 60.0)
                .saveSacrifice(context, Craftorio.prefix("build_blitz_coverage"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.BUILD_BLITZ_COVERAGE, UpgradeOperation.ADD, 10.0));
    }
}

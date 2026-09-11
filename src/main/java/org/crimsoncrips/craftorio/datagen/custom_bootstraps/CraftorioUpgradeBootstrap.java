package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.*;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.DamageUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.HealthUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.SpeedUpgrade;

public class CraftorioUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        Holder.Reference<CraftorioUpgrade> root = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_root")
                .icon(DEFAULT_ICON)
                .description("misc.craftorio.upgrade_root_description")
                .cost(0)
                .save(context, id("root"), b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0));

        Holder.Reference<CraftorioUpgrade> health1 = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_health_1")
                .icon(DEFAULT_ICON)
                .parent(root)
                .description("misc.craftorio.upgrade_health_1_description")
                .cost(1000)
                .save(context, id("health_1"), b -> HealthUpgrade.of(b, UpgradeOperation.ADD, 2.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_health_2")
                .icon(DEFAULT_ICON)
                .parent(health1)
                .description("misc.craftorio.upgrade_health_2_description")
                .cost(10000)
                .save(context, id("health_2"), b -> HealthUpgrade.of(b, UpgradeOperation.ADD, 3.0));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_speed_1")
                .icon(DEFAULT_ICON)
                .parent(root)
                .description("misc.craftorio.upgrade_speed_1_description")
                .cost(1000)
                .save(context, id("speed_1"), b -> SpeedUpgrade.of(b, UpgradeOperation.ADD, 0.02));

        Holder.Reference<CraftorioUpgrade> multiplier1 = CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_multiplier_1")
                .icon(DEFAULT_ICON)
                .parent(root)
                .description("misc.craftorio.upgrade_multiplier_1_description")
                .cost(5000)
                .save(context, id("multiplier_1"), b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.1));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_item_value_1")
                .icon(DEFAULT_ICON)
                .parent(multiplier1)
                .description("misc.craftorio.upgrade_item_value_1_description")
                .cost(5000)
                .save(context, id("item_value_1"), b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 0.05));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_contract_speed_1")
                .icon(DEFAULT_ICON)
                .parent(root)
                .description("misc.craftorio.upgrade_contract_speed_1_description")
                .cost(8000)
                .save(context, id("contract_speed_1"), b -> CraftorioModifierUpgrade.of(b, ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 0.1));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_effect_speed_1")
                .icon(DEFAULT_ICON)
                .parent(root)
                .description("misc.craftorio.upgrade_effect_speed_1_description")
                .cost(8000)
                .save(context, id("effect_speed_1"), b -> CraftorioModifierUpgrade.of(b, ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.MULTIPLY, 0.1));

        CraftorioUpgrade.builder()
                .name("misc.craftorio.upgrade_damage_1")
                .icon(DEFAULT_ICON)
                .parent(root)
                .description("misc.craftorio.upgrade_damage_1_description")
                .cost(3000)
                .save(context, id("damage_1"), b -> DamageUpgrade.of(b, UpgradeOperation.ADD, 1.0));
    }

    private static ResourceLocation id(String path) {
        return Craftorio.prefix(path);
    }
}

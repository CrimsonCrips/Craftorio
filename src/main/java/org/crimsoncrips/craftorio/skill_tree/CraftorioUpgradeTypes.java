package org.crimsoncrips.craftorio.skill_tree;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.*;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.AdvancementMultiplierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.ContractCompletionScalingUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.DoubleOrNothingUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.EffectRuneShopUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.EffectTimerDisplayUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.BuildBlitzUnlockUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.SinkValueScalingUpgrade;

import java.util.function.Supplier;

public class CraftorioUpgradeTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioUpgrade>> TYPES =
            DeferredRegister.create(CraftorioUpgrade.TYPE_REGISTRY_KEY, Craftorio.MODID);

    public static final Supplier<MapCodec<CraftorioModifierUpgrade>> MODIFIER =
            TYPES.register("modifier", () -> CraftorioModifierUpgrade.CODEC);

    public static final Supplier<MapCodec<CraftorioAttributeUpgrade>> ATTRIBUTE =
            TYPES.register("attribute", () -> CraftorioAttributeUpgrade.CODEC);

    public static final Supplier<MapCodec<CraftorioActionEffectUpgrade>> ACTION_EFFECT =
            TYPES.register("action_effect", () -> CraftorioActionEffectUpgrade.CODEC);

    public static final Supplier<MapCodec<CraftorioDifficultyUpgrade>> DIFFICULTY =
            TYPES.register("difficulty", () -> CraftorioDifficultyUpgrade.CODEC);


    public static final Supplier<MapCodec<AdvancementMultiplierUpgrade>> ADVANCEMENT_MULTIPLIER =
            TYPES.register("advancement_multiplier", () -> AdvancementMultiplierUpgrade.CODEC);

    public static final Supplier<MapCodec<SinkValueScalingUpgrade>> SINK_VALUE_SCALING =
            TYPES.register("sink_value_scaling", () -> SinkValueScalingUpgrade.CODEC);

    public static final Supplier<MapCodec<ContractCompletionScalingUpgrade>> CONTRACT_COMPLETION_SCALING =
            TYPES.register("contract_completion_scaling", () -> ContractCompletionScalingUpgrade.CODEC);

    public static final Supplier<MapCodec<DoubleOrNothingUnlockUpgrade>> DOUBLE_OR_NOTHING_UNLOCK =
            TYPES.register("double_or_nothing_unlock", () -> DoubleOrNothingUnlockUpgrade.CODEC);

    public static final Supplier<MapCodec<EffectRuneShopUnlockUpgrade>> EFFECT_RUNE_SHOP_UNLOCK =
            TYPES.register("effect_rune_shop_unlock", () -> EffectRuneShopUnlockUpgrade.CODEC);
    public static final Supplier<MapCodec<BuildBlitzUnlockUpgrade>> BUILD_BLITZ_UNLOCK =
            TYPES.register("build_blitz_unlock", () -> BuildBlitzUnlockUpgrade.CODEC);
    public static final Supplier<MapCodec<EffectTimerDisplayUnlockUpgrade>> EFFECT_TIMER_DISPLAY_UNLOCK =
            TYPES.register("effect_timer_display_unlock", () -> EffectTimerDisplayUnlockUpgrade.CODEC);
}

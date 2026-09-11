package org.crimsoncrips.craftorio.skill_tree;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.*;

import java.util.function.Supplier;

public class CraftorioUpgradeTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioUpgrade>> TYPES =
            DeferredRegister.create(CraftorioUpgrade.TYPE_REGISTRY_KEY, Craftorio.MODID);

    public static final Supplier<MapCodec<CraftorioModifierUpgrade>> MODIFIER =
            TYPES.register("modifier", () -> CraftorioModifierUpgrade.CODEC);

    public static final Supplier<MapCodec<HealthUpgrade>> HEALTH =
            TYPES.register("health", () -> HealthUpgrade.CODEC);

    public static final Supplier<MapCodec<SpeedUpgrade>> SPEED =
            TYPES.register("speed", () -> SpeedUpgrade.CODEC);

    public static final Supplier<MapCodec<DefenseUpgrade>> DEFENSE =
            TYPES.register("defense", () -> DefenseUpgrade.CODEC);

    public static final Supplier<MapCodec<DamageUpgrade>> DAMAGE =
            TYPES.register("damage", () -> DamageUpgrade.CODEC);

    public static final Supplier<MapCodec<BlockReachUpgrade>> BLOCK_REACH =
            TYPES.register("block_reach", () -> BlockReachUpgrade.CODEC);

    public static final Supplier<MapCodec<JumpHeightUpgrade>> JUMP_HEIGHT =
            TYPES.register("jump_height", () -> JumpHeightUpgrade.CODEC);

    public static final Supplier<MapCodec<XpGainUpgrade>> XP_GAIN =
            TYPES.register("xp_gain", () -> XpGainUpgrade.CODEC);

    public static final Supplier<MapCodec<ResistanceUpgrade>> RESISTANCE =
            TYPES.register("resistance", () -> ResistanceUpgrade.CODEC);
}

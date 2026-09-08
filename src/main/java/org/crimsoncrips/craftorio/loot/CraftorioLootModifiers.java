package org.crimsoncrips.craftorio.loot;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.function.Supplier;

public class CraftorioLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Craftorio.MODID);

    public static final Supplier<MapCodec<CraftorioEffectLootModifier>> EFFECT_ITEM_LOOT =
            MODIFIERS.register("effect_item_loot", () -> CraftorioEffectLootModifier.MAP_CODEC);

}

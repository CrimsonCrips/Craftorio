package org.crimsoncrips.craftorio.datagen.maps;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.effects.CraftorioEffects;
import org.crimsoncrips.craftorio.effects.points.CraftorioPointEffect;
import org.crimsoncrips.craftorio.effects.points.GeneralMultiplierEffect;

import java.util.function.Function;

public class CraftorioDataMaps {


    private static Codec<Float> floatRangeMinExclusiveWithMessage(float min, float max, Function<Float, String> errorMessage) {
        return Codec.FLOAT.validate((p_274865_) -> p_274865_.compareTo(min) > 0 && p_274865_.compareTo(max) <= 0 ? DataResult.success(p_274865_) : DataResult.error(() -> (String)errorMessage.apply(p_274865_)));
    }

    //From Androsa (Gaia Dimension)
    public static final DataMapType<Item, String> POINT_VALUE = DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "point_value"),
                    Registries.ITEM,
                    ExtraCodecs.NON_EMPTY_STRING)
            .synced(ExtraCodecs.NON_EMPTY_STRING, false)
            .build();

    public static final DataMapType<MobEffect, String> EFFECT_POINT_VALUE = DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "effect_point_value"),
                    Registries.MOB_EFFECT,
                    ExtraCodecs.NON_EMPTY_STRING)
            .synced(ExtraCodecs.NON_EMPTY_STRING, false)
            .build();

    public static final DataMapType<Enchantment, String> ENCHANTMENT_POINT_VALUE = DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "enchantment_point_value"),
                    Registries.ENCHANTMENT,
                    ExtraCodecs.NON_EMPTY_STRING)
            .synced(ExtraCodecs.NON_EMPTY_STRING, false)
            .build();

    public static final DataMapType<Item, CraftorioEffects> ITEM_EFFECTS = DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "item_effects"),
                    Registries.ITEM,
                    CraftorioEffects.dispatchCodec())
            .synced(CraftorioEffects.dispatchCodec(), false)
            .build();

    public static void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(POINT_VALUE);
        event.register(EFFECT_POINT_VALUE);
        event.register(ENCHANTMENT_POINT_VALUE);
    }
}
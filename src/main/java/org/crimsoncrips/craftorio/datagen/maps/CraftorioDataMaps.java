package org.crimsoncrips.craftorio.datagen.maps;


import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.crimsoncrips.craftorio.Craftorio;

public class CraftorioDataMaps {

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


    public static void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(POINT_VALUE);
        event.register(EFFECT_POINT_VALUE);
        event.register(ENCHANTMENT_POINT_VALUE);
    }
}
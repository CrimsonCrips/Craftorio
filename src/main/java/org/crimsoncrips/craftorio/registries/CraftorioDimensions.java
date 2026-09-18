package org.crimsoncrips.craftorio.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.crimsoncrips.craftorio.Craftorio;

public class CraftorioDimensions {

    public static final ResourceLocation HAVEN_ID = ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "haven");

    public static final ResourceKey<Biome> HAVEN_BIOME_KEY = ResourceKey.create(Registries.BIOME, HAVEN_ID);
    public static final ResourceKey<DimensionType> HAVEN_DIMENSION_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, HAVEN_ID);
    public static final ResourceKey<LevelStem> HAVEN_LEVEL_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM, HAVEN_ID);
    public static final ResourceKey<Level> HAVEN_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, HAVEN_ID);
}

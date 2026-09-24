package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class CraftorioHavenDimensionBootstrap {

    public static void bootstrapBiome(BootstrapContext<Biome> context) {
        context.register(CraftorioDimensions.HAVEN_BIOME_KEY, new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(0xFFFFFF)
                        .waterColor(0xFFFFFF)
                        .waterFogColor(0xFFFFFF)
                        .skyColor(0xFFFFFF)
                        .build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build());
    }

    public static void bootstrapDimensionType(BootstrapContext<DimensionType> context) {
        context.register(CraftorioDimensions.HAVEN_DIMENSION_TYPE_KEY, new DimensionType(
                OptionalLong.of(6000L),
                false,
                false,
                false,
                false,
                1.0,
                false,
                false,
                0,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                CraftorioDimensions.HAVEN_ID,
                1.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        ));
    }

    public static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        Holder<Biome> biome = biomes.getOrThrow(CraftorioDimensions.HAVEN_BIOME_KEY);
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of());
        FlatLevelSource generator = new FlatLevelSource(settings);

        context.register(CraftorioDimensions.HAVEN_LEVEL_STEM_KEY, new LevelStem(
                dimensionTypes.getOrThrow(CraftorioDimensions.HAVEN_DIMENSION_TYPE_KEY),
                generator
        ));
    }
}

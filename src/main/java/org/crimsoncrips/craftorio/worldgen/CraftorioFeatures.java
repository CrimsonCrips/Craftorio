package org.crimsoncrips.craftorio.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;

public class CraftorioFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Craftorio.MODID);

    public static final DeferredHolder<Feature<?>, CraftorioHavenPlatformFeature> HAVEN_PLATFORM =
            FEATURES.register("haven_platform", () -> new CraftorioHavenPlatformFeature(NoneFeatureConfiguration.CODEC));
}

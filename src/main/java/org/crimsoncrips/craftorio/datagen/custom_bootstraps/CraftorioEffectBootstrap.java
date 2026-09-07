package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

public class CraftorioEffectBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioEffects> context) {

        //Buffs
        context.register(
                key("tag/copper_block_buff"),
                new TagMultiplierEffect(100F, "registry.copper_block_buff", CraftorioItemTagGen.COPPER, 500)
        );

        context.register(
                key("tag/general_1"),
                new GeneralMultiplierEffect(100F, "registry.general_1", 500)
        );

        context.register(
                key("tag/general_2"),
                new GeneralMultiplierEffect(100F, "registry.general_2", 500)
        );

        context.register(
                key("tag/general_3"),
                new GeneralMultiplierEffect(100F, "registry.general_3", 500)
        );

        context.register(
                key("tag/general_4"),
                new GeneralMultiplierEffect(100F, "registry.general_4", 500)
        );

        context.register(
                key("tag/general_5"),
                new GeneralMultiplierEffect(100F, "registry.general_5", 500)
        );


        //Debuffs
        context.register(
                key("tag/copper_block_debuff"),
                new TagMultiplierEffect(-10F, "registry.copper_block_debuff", CraftorioItemTagGen.COPPER, 500)
        );
    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}
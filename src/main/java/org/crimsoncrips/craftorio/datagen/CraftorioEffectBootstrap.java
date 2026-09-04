package org.crimsoncrips.craftorio.datagen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.effects.CraftorioEffects;
import org.crimsoncrips.craftorio.effects.points.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.effects.points.TagMultiplierEffect;

public class CraftorioEffectBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioEffects> context) {
        context.register(
                key("general/50_percent_addition"),
                new GeneralMultiplierEffect(1.5F, "50% addition", 200)
        );

        context.register(
                key("tag/copper_block_buff"),
                new TagMultiplierEffect(100F, "Copper Block Buff", CraftorioItemTagGen.COPPER, 500)
        );
    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}
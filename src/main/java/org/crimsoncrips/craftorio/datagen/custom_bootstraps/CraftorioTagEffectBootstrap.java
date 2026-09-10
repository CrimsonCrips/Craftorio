package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

public class CraftorioTagEffectBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("locked.png");

    public static void buffBootstrap(BootstrapContext<CraftorioEffects> context) {

        context.register(
                key("tag/copper_block_buff"),
                new TagMultiplierEffect(100F, "registry.copper_block_buff", CraftorioItemTagGen.COPPER, 25, DEFAULT_ICON)
        );

    }

    public static void debuffBootstrap(BootstrapContext<CraftorioEffects> context) {

        context.register(
                key("tag/copper_block_debuff"),
                new TagMultiplierEffect(-10F, "registry.copper_block_debuff", CraftorioItemTagGen.COPPER, 25, DEFAULT_ICON)
        );
    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}

package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

public class CraftorioEffectBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("locked.png");

    public static void bootstrap(BootstrapContext<CraftorioEffects> context) {

        context.register(
                key("tag/copper_block_buff"),
                new TagMultiplierEffect(100F, "registry.copper_block_buff", CraftorioItemTagGen.COPPER, 500, DEFAULT_ICON)
        );

        context.register(
                key("tag/general_1"),
                new GeneralMultiplierEffect(100F, "registry.general_1", 500, DEFAULT_ICON)
        );

        context.register(
                key("tag/general_2"),
                new GeneralMultiplierEffect(100F, "registry.general_2", 500, DEFAULT_ICON)
        );

        context.register(
                key("tag/general_3"),
                new GeneralMultiplierEffect(100F, "registry.general_3", 500, DEFAULT_ICON)
        );

        context.register(
                key("tag/general_4"),
                new GeneralMultiplierEffect(100F, "registry.general_4", 500, DEFAULT_ICON)
        );

        context.register(
                key("general/general_5"),
                new GeneralMultiplierEffect(100F, "registry.general_5", 500, DEFAULT_ICON)
        );

        context.register(
                key("shop/0_25_increase"),
                new ShopMultiplierEffect(1.25F, "registry.0_25_increase", 500, DEFAULT_ICON)
        );

        context.register(
                key("tag/copper_block_debuff"),
                new TagMultiplierEffect(-10F, "registry.copper_block_debuff", CraftorioItemTagGen.COPPER, 500, DEFAULT_ICON)
        );
    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}

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

public class CraftorioShopBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void buffBootstrap(BootstrapContext<CraftorioEffects> context) {

        context.register(
                key("shop/0_25_increase"),
                new ShopMultiplierEffect(1.25F, "registry.0_25_increase", 25, DEFAULT_ICON)
        );

    }

    public static void debuffBootstrap(BootstrapContext<CraftorioEffects> context) {

        context.register(
                key("shop/0_25_decrease"),
                new ShopMultiplierEffect(-1.25F, "registry.0_25_decrease", 25, DEFAULT_ICON)
        );

    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}

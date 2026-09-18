package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

public class CraftorioEffectBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static void buffBootstrap(BootstrapContext<CraftorioEffects> context) {
        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "economic_boom")),
                new ShopMultiplierEffect(15.0F, "registry.economic_boom", 120, DEFAULT_ICON, 7, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "lucky")),
                new GeneralMultiplierEffect(2.0F, "registry.lucky", 2100, DEFAULT_ICON, 10, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "productive")),
                new GeneralMultiplierEffect(3.5F, "registry.productive", 1620, DEFAULT_ICON, 8, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "redstone_mania")),
                new TagMultiplierEffect(4.0F, "registry.redstone_mania", TagKey.create(Registries.ITEM, ResourceLocation.parse("craftorio:redstone_related")), 720, DEFAULT_ICON, 7, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "music_fest")),
                new TagMultiplierEffect(7.0F, "registry.music_fest", TagKey.create(Registries.ITEM, ResourceLocation.parse("c:music_discs")), 1000, DEFAULT_ICON, 3, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "archery_season")),
                new TagMultiplierEffect(4.0F, "registry.archery_season", TagKey.create(Registries.ITEM, ResourceLocation.parse("craftorio:archery_season")), 4800, DEFAULT_ICON, 5, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "universal_demand")),
                new GeneralMultiplierEffect(100.0F, "registry.universal_demand", 60, DEFAULT_ICON, 1, false)
        );

    }

    public static void debuffBootstrap(BootstrapContext<CraftorioEffects> context) {

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "inflated_valuables")),
                new TagMultiplierEffect(-10.0F, "registry.inflated_valuables", TagKey.create(Registries.ITEM, ResourceLocation.parse("craftorio:valuables")), 2400, DEFAULT_ICON, 6, false)
        );


        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "black_holdover")),
                new GeneralMultiplierEffect(-80.0F, "registry.black_holdover", 600, DEFAULT_ICON, 1, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "monopolized")),
                new ShopMultiplierEffect(-20.0F, "registry.monopolized", 5400, DEFAULT_ICON, 3, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "oversupplied")),
                new ShopMultiplierEffect(-5.0F, "registry.oversupplied", 7200, DEFAULT_ICON, 6, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "dense_traffic")),
                new ShopMultiplierEffect(-3.0F, "registry.dense_traffic", 1400, DEFAULT_ICON, 9, false)
        );


        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "rugpulled")),
                new ShopMultiplierEffect(-10.0F, "registry.rugpulled", 1200, DEFAULT_ICON, 6, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "strait_to_deficits")),
                new GeneralMultiplierEffect(-7.0F, "registry.strait_to_deficits", 700, DEFAULT_ICON, 3, false)
        );

        context.register(
                key("shop/kingdom_tariff"),
                new ShopMultiplierEffect(-10.25F, "registry.kingdom_tariff", 1900, DEFAULT_ICON, 10, true)
        );

        context.register(
                key("tag/copper_deficiency"),
                new TagMultiplierEffect(-80F, "registry.copper_deficiency", CraftorioItemTagGen.COPPER, 5400, DEFAULT_ICON, 10, true)
        );

        context.register(
                key("general/inquisitors_wrath"),
                new GeneralMultiplierEffect(-10F, "registry.inquisitors_wrath", 3600, DEFAULT_ICON, 10, true)
        );

        context.register(
                key("general/trazyns_curse"),
                new GeneralMultiplierEffect(-10000F, "registry.trazyns_curse", 18000, DEFAULT_ICON, 10, true)
        );

        context.register(
                key("general/commeupance_of_the_gods"),
                new GeneralMultiplierEffect(-5000F, "registry.commeupance_of_the_gods", 14400, DEFAULT_ICON, 10, true)
        );
    }

    private static String pathSuffix(float multiplier) {
        String text = multiplier == Math.floor(multiplier)
                ? String.valueOf((int) multiplier)
                : String.valueOf(multiplier);
        return text.replace('.', '_');
    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}

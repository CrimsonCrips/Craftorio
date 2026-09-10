package org.crimsoncrips.craftorio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.advancement.CraftorioAdvancementProvider;
import org.crimsoncrips.craftorio.datagen.custom_bootstraps.CraftorioGeneralEffectBootstrap;
import org.crimsoncrips.craftorio.datagen.custom_bootstraps.CraftorioShipmentBootstrap;
import org.crimsoncrips.craftorio.datagen.custom_bootstraps.CraftorioShopBootstrap;
import org.crimsoncrips.craftorio.datagen.custom_bootstraps.CraftorioTagEffectBootstrap;
import org.crimsoncrips.craftorio.datagen.language.CraftLangGen;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioPointsDeterminer;
import org.crimsoncrips.craftorio.datagen.recipe.CraftorioRecipeGenerator;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioBlockTagGen;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


@Mod(Craftorio.MODID)
public class CraftorioDatagen {
    public static void generateData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new CraftLangGen(output));
        CraftorioBlockTagGen blocktags = new CraftorioBlockTagGen(output, provider, helper);
        generator.addProvider(event.includeServer(), blocktags);
        generator.addProvider(event.includeServer(), new CraftorioItemTagGen(output, provider, blocktags.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new CraftorioPointsDeterminer(output, provider));
        generator.addProvider(event.includeServer(), new CraftorioLootModifierProvider(output, provider));
        generator.addProvider(event.includeServer(), new CraftorioRecipeGenerator(output, provider));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, provider, helper, List.of(new CraftorioAdvancementProvider())));

        RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
                .add(CraftorioEffects.REGISTRY_KEY, context -> {
                    CraftorioTagEffectBootstrap.buffBootstrap(context);
                    CraftorioTagEffectBootstrap.debuffBootstrap(context);
                    CraftorioGeneralEffectBootstrap.buffBootstrap(context);
                    CraftorioGeneralEffectBootstrap.debuffBootstrap(context);
                    CraftorioShopBootstrap.buffBootstrap(context);
                    CraftorioShopBootstrap.debuffBootstrap(context);
                })
                .add(CraftorioShipmentContract.REGISTRY_KEY, CraftorioShipmentBootstrap::bootstrap);

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, provider, registryBuilder, Set.of(Craftorio.MODID)));

    }
}
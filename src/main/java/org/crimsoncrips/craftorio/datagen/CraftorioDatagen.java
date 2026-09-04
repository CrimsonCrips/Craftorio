package org.crimsoncrips.craftorio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.language.CraftLangGen;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioPointsDeterminer;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioBlockTagGen;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;

import java.util.concurrent.CompletableFuture;


@Mod(Craftorio.MODID)
public class CraftorioDatagen {
    //Giga Props to Drull and TF for assistance (and code yoinking)//
    public static void generateData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(event.includeClient(), new CraftLangGen(output));
        CraftorioBlockTagGen blocktags = new CraftorioBlockTagGen(output, provider, helper);
        generator.addProvider(event.includeServer(), blocktags);
        generator.addProvider(event.includeServer(), new CraftorioItemTagGen(output, provider, blocktags.contentsGetter(), helper));

//        generator.addProvider(event.includeServer(), new CraftorioRecipeGenerator(output, provider));
        generator.addProvider(event.includeServer(), new CraftorioPointsDeterminer(output, provider));
    }

}

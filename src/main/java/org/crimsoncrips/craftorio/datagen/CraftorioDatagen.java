package org.crimsoncrips.craftorio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.language.Craft_LangGen;
import org.crimsoncrips.craftorio.datagen.maps.Craft_PointMap;
import org.crimsoncrips.craftorio.datagen.tags.Craft_BlockTagGenerator;
import org.crimsoncrips.craftorio.datagen.tags.Craft_ItemTagGenerator;

import java.util.concurrent.CompletableFuture;


@Mod(Craftorio.MODID)
public class CraftorioDatagen {
    //Giga Props to Drull and TF for assistance (and code yoinking)//
    public static void generateData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(event.includeClient(), new Craft_LangGen(output));
        Craft_BlockTagGenerator blocktags = new Craft_BlockTagGenerator(output, provider, helper);
        generator.addProvider(event.includeServer(), blocktags);
        generator.addProvider(event.includeServer(), new Craft_ItemTagGenerator(output, provider, blocktags.contentsGetter(), helper));

        generator.addProvider(event.includeServer(), new Craft_PointMap(output, provider));
    }

}

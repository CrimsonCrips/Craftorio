package org.crimsoncrips.craftorio.item;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.schematic.ContractSchematicItem;
import org.crimsoncrips.craftorio.item.rune.EffectRune;
import org.crimsoncrips.craftorio.item.rune.MysteryEffectRune;
import org.crimsoncrips.craftorio.item.structure.StructureWandItem;


public class CraftorioItems {


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craftorio.MODID);

    public static final DeferredItem<Item> EFFECT_RUNE = ITEMS.register("effect_rune", () -> new EffectRune(new Item.Properties()));
    public static final DeferredItem<Item> MYSTERY_EFFECT_RUNE = ITEMS.register("mystery_effect_rune", () -> new MysteryEffectRune(new Item.Properties()));
    public static final DeferredItem<Item> CLAIM_ITEM = ITEMS.register("claim_item", () -> new ClaimChunkItem(new Item.Properties()));
    public static final DeferredItem<Item> SCANNER_STICK = ITEMS.register("scanner_stick", () -> new ScannerStickItem(new Item.Properties()));
    public static final DeferredItem<Item> CHRONOSPHERE_STICK = ITEMS.register("chronosphere_stick", () -> new ChronosphereStickItem(new Item.Properties()));
    public static final DeferredItem<Item> STRUCTURE_WAND = ITEMS.register("structure_wand", () -> new StructureWandItem(new Item.Properties()));
    public static final DeferredItem<Item> SCHEMATIC = ITEMS.register("schematic", () -> new ContractSchematicItem(new Item.Properties()));

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (DeferredHolder<Item, ? extends Item> item : ITEMS.getEntries()) {
                if (item == SCHEMATIC || item == SCANNER_STICK || item == CHRONOSPHERE_STICK) continue;
                event.accept(item.get());
            }
        }
    }

}
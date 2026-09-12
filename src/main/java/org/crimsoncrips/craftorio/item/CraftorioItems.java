package org.crimsoncrips.craftorio.item;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CraftorioItems {


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craftorio.MODID);

    public static final DeferredItem<Item> EFFECT_RUNE = ITEMS.register("effect_rune", () -> new EffectRune(new Item.Properties()));
    public static final DeferredItem<Item> MYSTERY_EFFECT_RUNE = ITEMS.register("mystery_effect_rune", () -> new MysteryEffectRune(new Item.Properties()));
    public static final DeferredItem<Item> CLAIM_ITEM = ITEMS.register("claim_item", () -> new ClaimChunkItem(new Item.Properties()));

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (DeferredHolder<Item, ? extends Item> item : ITEMS.getEntries()) {
                event.accept(item.get());
            }
        }
    }

}
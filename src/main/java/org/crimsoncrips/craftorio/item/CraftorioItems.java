package org.crimsoncrips.craftorio.item;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static final DeferredItem<Item> EFFECT_ITEM = ITEMS.register("effect_item", () -> new EffectItem(new Item.Properties()));
    public static final DeferredItem<Item> MYSTERY_EFFECT_ITEM = ITEMS.register("mystery_effect_item", () -> new MysteryEffectItem(new Item.Properties()));


}
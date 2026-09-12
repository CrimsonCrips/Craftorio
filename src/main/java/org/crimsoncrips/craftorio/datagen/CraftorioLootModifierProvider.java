package org.crimsoncrips.craftorio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.loot.CraftorioEffectLootModifier;

import java.util.concurrent.CompletableFuture;

public class CraftorioLootModifierProvider extends GlobalLootModifierProvider {

    public CraftorioLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Craftorio.MODID);
    }

    @Override
    protected void start() {
        add("effect_rune_loot", new CraftorioEffectLootModifier(new LootItemCondition[0]));
    }
}

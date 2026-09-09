package org.crimsoncrips.craftorio.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.item.CraftorioItems;

import java.util.function.Consumer;

public class CraftorioAdvancementProvider implements AdvancementProvider.AdvancementGenerator {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        CraftorioBlocks.SINKER.get(),
                        Component.translatable("advancements.craftorio.root.title"),
                        Component.translatable("advancements.craftorio.root.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("has_sinker", InventoryChangeTrigger.TriggerInstance.hasItems(CraftorioBlocks.SINKER.get()))
                .save(saver, Craftorio.prefix("root"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        CraftorioItems.CLAIM_ITEM.get(),
                        Component.translatable("advancements.craftorio.claim_land.title"),
                        Component.translatable("advancements.craftorio.claim_land.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("has_claim_item", InventoryChangeTrigger.TriggerInstance.hasItems(CraftorioItems.CLAIM_ITEM.get()))
                .save(saver, Craftorio.prefix("claim_land"), existingFileHelper);
    }
}

package org.crimsoncrips.craftorio.datagen.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
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
                .display(CraftorioBlocks.SINKER.get(),
                        Component.translatable(getTitle("root")),
                        Component.translatable(getDescription("root")),
                        Craftorio.prefix("gui/adv_bg"), AdvancementType.TASK, true, true, false
                )
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                .save(saver, Craftorio.prefix("root"), existingFileHelper);

        AdvancementHolder millionaire = pointsMilestone(saver, existingFileHelper, root, "millionaire", Items.GOLD_INGOT);
        AdvancementHolder humanBody = pointsMilestone(saver, existingFileHelper, millionaire, "the_human_body", Items.EMERALD);
        AdvancementHolder russiasLawsuit = pointsMilestone(saver, existingFileHelper, humanBody, "russias_lawsuit", Items.DIAMOND);
        AdvancementHolder universalNumber = pointsMilestone(saver, existingFileHelper, russiasLawsuit, "universal_number", Items.NETHERITE_INGOT);
        AdvancementHolder captureOfTheTrueOverlord = pointsMilestone(saver, existingFileHelper, universalNumber, "capture_of_the_true_overlord", Items.NETHER_STAR);
        AdvancementHolder minersNumber = pointsMilestone(saver, existingFileHelper, captureOfTheTrueOverlord, "the_miners_number", Items.BEACON);
        AdvancementHolder existentialInfinity = pointsMilestone(saver, existingFileHelper, minersNumber, "existential_infinity", Items.DRAGON_EGG);
        pointsMilestone(saver, existingFileHelper, existentialInfinity, "existential_debt", Items.WITHER_SKELETON_SKULL);
    }

    private AdvancementHolder pointsMilestone(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder parent, String path, net.minecraft.world.item.Item icon) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon,
                        Component.translatable(getTitle(path)),
                        Component.translatable(getDescription(path)),
                        null, AdvancementType.CHALLENGE, true, true, false
                )
                .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .save(saver, Craftorio.prefix(path), existingFileHelper);
    }

    public String getTitle(String value){
        return "advancements.craftorio." + value + ".title";
    }

    public String getDescription(String value){
        return "advancements.craftorio." + value + ".description";
    }
}

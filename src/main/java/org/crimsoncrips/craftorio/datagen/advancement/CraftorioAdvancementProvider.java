package org.crimsoncrips.craftorio.datagen.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.server.CraftorioPointsAdvancements;
import org.crimsoncrips.craftorio.server.CraftorioPointsTrigger;

import java.util.Map;
import java.util.function.Consumer;

public class CraftorioAdvancementProvider implements AdvancementProvider.AdvancementGenerator {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(CraftorioBlocks.SINKER.get(),
                        Component.translatable(getTitle("root")),
                        Component.translatable(getDescription("root")),
                        Craftorio.getGuiTexture("adv_bg.png"), AdvancementType.TASK, true, true, false
                )
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                .save(saver, Craftorio.prefix("root"), existingFileHelper);

        Map<String, net.minecraft.world.item.Item> icons = Map.of(
                "millionaire", Items.GOLD_INGOT,
                "the_human_body", Items.EMERALD,
                "russias_lawsuit", Items.DIAMOND,
                "universal_number", Items.NETHERITE_INGOT,
                "capture_of_the_true_overlord", Items.NETHER_STAR,
                "the_miners_number", Items.BEACON,
                "existential_infinity", Items.DRAGON_EGG
        );

        AdvancementHolder parent = root;
        for (CraftorioPointsAdvancements.Milestone milestone : CraftorioPointsAdvancements.POSITIVE_MILESTONES) {
            parent = pointsMilestone(saver, existingFileHelper, parent, milestone, icons.get(milestone.path()));
        }

        pointsMilestone(saver, existingFileHelper, parent, CraftorioPointsAdvancements.NEGATIVE_MILESTONE, Items.WITHER_SKELETON_SKULL);
    }

    private AdvancementHolder pointsMilestone(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder parent,
                                               CraftorioPointsAdvancements.Milestone milestone, net.minecraft.world.item.Item icon) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon,
                        Component.translatable(getTitle(milestone.path())),
                        Component.translatable(getDescription(milestone.path())),
                        null, AdvancementType.CHALLENGE, true, true, false
                )
                .addCriterion("points", CraftorioPointsAdvancements.POINTS_TRIGGER.createCriterion(
                        new CraftorioPointsTrigger.TriggerInstance(java.util.Optional.empty(), milestone.threshold(), milestone.negative())))
                .save(saver, Craftorio.prefix(milestone.path()), existingFileHelper);
    }

    public String getTitle(String value){
        return "advancements.craftorio." + value + ".title";
    }

    public String getDescription(String value){
        return "advancements.craftorio." + value + ".description";
    }
}

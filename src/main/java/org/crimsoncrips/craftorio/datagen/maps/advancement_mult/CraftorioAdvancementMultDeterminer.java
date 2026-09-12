package org.crimsoncrips.craftorio.datagen.maps.advancement_mult;


import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

import static java.lang.Math.round;

@SuppressWarnings("Deprecated")
public class CraftorioAdvancementMultDeterminer extends DataMapProvider {

    //From Androsa (Gaia Dimension)
    public CraftorioAdvancementMultDeterminer(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    public static void advancementMultiplierMap(Builder<String, Advancement> multiplierValue) {

        addAdvancementMultiplier(multiplierValue, "adventure/adventuring_time", 500);
        addAdvancementMultiplier(multiplierValue, "adventure/arbalistic", 400);
        addAdvancementMultiplier(multiplierValue, "adventure/bullseye", 350);
        addAdvancementMultiplier(multiplierValue, "adventure/hero_of_the_village", 300);
        addAdvancementMultiplier(multiplierValue, "adventure/honey_block_slide", 130);
        addAdvancementMultiplier(multiplierValue, "adventure/kill_all_mobs", 600);
        addAdvancementMultiplier(multiplierValue, "adventure/kill_a_mob", 100);
        addAdvancementMultiplier(multiplierValue, "adventure/ol_betsy", 110);
        addAdvancementMultiplier(multiplierValue, "adventure/shoot_arrow", 100);
        addAdvancementMultiplier(multiplierValue, "adventure/sleep_in_bed", 100);
        addAdvancementMultiplier(multiplierValue, "adventure/sniper_duel", 250);
        addAdvancementMultiplier(multiplierValue, "adventure/summon_iron_golem", 150);
        addAdvancementMultiplier(multiplierValue, "adventure/throw_trident", 130);
        addAdvancementMultiplier(multiplierValue, "adventure/totem_of_undying", 200);
        addAdvancementMultiplier(multiplierValue, "adventure/trade", 110);
        addAdvancementMultiplier(multiplierValue, "adventure/two_birds_one_arrow", 350);
        addAdvancementMultiplier(multiplierValue, "adventure/very_very_frightening", 300);
        addAdvancementMultiplier(multiplierValue, "adventure/voluntary_exile", 200);
        addAdvancementMultiplier(multiplierValue, "adventure/whos_the_pillager_now", 120);
        addAdvancementMultiplier(multiplierValue, "end/dragon_breath", 250);
        addAdvancementMultiplier(multiplierValue, "end/dragon_egg", 350);
        addAdvancementMultiplier(multiplierValue, "end/elytra", 400);
        addAdvancementMultiplier(multiplierValue, "end/enter_end_gateway", 220);
        addAdvancementMultiplier(multiplierValue, "end/find_end_city", 350);
        addAdvancementMultiplier(multiplierValue, "end/kill_dragon", 500);
        addAdvancementMultiplier(multiplierValue, "end/levitate", 400);
        addAdvancementMultiplier(multiplierValue, "end/respawn_dragon", 450);
        addAdvancementMultiplier(multiplierValue, "husbandry/balanced_diet", 600);
        addAdvancementMultiplier(multiplierValue, "husbandry/bred_all_animals", 450);
        addAdvancementMultiplier(multiplierValue, "husbandry/breed_an_animal", 100);
        addAdvancementMultiplier(multiplierValue, "husbandry/complete_catalogue", 500);
        addAdvancementMultiplier(multiplierValue, "husbandry/fishy_business", 110);
        addAdvancementMultiplier(multiplierValue, "husbandry/obtain_netherite_hoe", 250);
        addAdvancementMultiplier(multiplierValue, "husbandry/plant_seed", 100);
        addAdvancementMultiplier(multiplierValue, "husbandry/safely_harvest_honey", 130);
        addAdvancementMultiplier(multiplierValue, "husbandry/silk_touch_nest", 200);
        addAdvancementMultiplier(multiplierValue, "husbandry/tactical_fishing", 250);
        addAdvancementMultiplier(multiplierValue, "husbandry/tame_an_animal", 120);
        addAdvancementMultiplier(multiplierValue, "nether/all_effects", 600);
        addAdvancementMultiplier(multiplierValue, "nether/all_potions", 450);
        addAdvancementMultiplier(multiplierValue, "nether/brew_potion", 140);
        addAdvancementMultiplier(multiplierValue, "nether/charge_respawn_anchor", 180);
        addAdvancementMultiplier(multiplierValue, "nether/create_beacon", 300);
        addAdvancementMultiplier(multiplierValue, "nether/create_full_beacon", 700);
        addAdvancementMultiplier(multiplierValue, "nether/distract_piglin", 130);
        addAdvancementMultiplier(multiplierValue, "nether/explore_nether", 350);
        addAdvancementMultiplier(multiplierValue, "nether/fast_travel", 450);
        addAdvancementMultiplier(multiplierValue, "nether/find_bastion", 200);
        addAdvancementMultiplier(multiplierValue, "nether/find_fortress", 200);
        addAdvancementMultiplier(multiplierValue, "nether/get_wither_skull", 350);
        addAdvancementMultiplier(multiplierValue, "nether/loot_bastion", 220);
        addAdvancementMultiplier(multiplierValue, "nether/netherite_armor", 400);
        addAdvancementMultiplier(multiplierValue, "nether/obtain_ancient_debris", 250);
        addAdvancementMultiplier(multiplierValue, "nether/obtain_blaze_rod", 220);
        addAdvancementMultiplier(multiplierValue, "nether/obtain_crying_obsidian", 170);
        addAdvancementMultiplier(multiplierValue, "nether/return_to_sender", 200);
        addAdvancementMultiplier(multiplierValue, "nether/ride_strider", 150);
        addAdvancementMultiplier(multiplierValue, "nether/summon_wither", 400);
        addAdvancementMultiplier(multiplierValue, "nether/uneasy_alliance", 500);
        addAdvancementMultiplier(multiplierValue, "nether/use_lodestone", 130);
        addAdvancementMultiplier(multiplierValue, "story/cure_zombie_villager", 350);
        addAdvancementMultiplier(multiplierValue, "story/deflect_arrow", 150);
        addAdvancementMultiplier(multiplierValue, "story/enchant_item", 150);
        addAdvancementMultiplier(multiplierValue, "story/enter_the_end", 400);
        addAdvancementMultiplier(multiplierValue, "story/enter_the_nether", 220);
        addAdvancementMultiplier(multiplierValue, "story/follow_ender_eye", 250);
        addAdvancementMultiplier(multiplierValue, "story/form_obsidian", 130);
        addAdvancementMultiplier(multiplierValue, "story/iron_tools", 120);
        addAdvancementMultiplier(multiplierValue, "story/lava_bucket", 120);
        addAdvancementMultiplier(multiplierValue, "story/mine_diamond", 200);
        addAdvancementMultiplier(multiplierValue, "story/mine_stone", 100);
        addAdvancementMultiplier(multiplierValue, "story/obtain_armor", 110);
        addAdvancementMultiplier(multiplierValue, "story/shiny_gear", 280);
        addAdvancementMultiplier(multiplierValue, "story/smelt_iron", 110);
        addAdvancementMultiplier(multiplierValue, "story/upgrade_tools", 100);
    }

    private static void addAdvancementMultiplier(Builder<String, Advancement> multiplierValue, String resourceLocation, double value) {
        ResourceLocation advancementLocation = ResourceLocation.withDefaultNamespace(resourceLocation);
        multiplierValue.add(advancementLocation, String.valueOf(value), false);
    }


    //(Work multiplier refers to how many hops it takes in terms of crafting to get to the result item)
    


}
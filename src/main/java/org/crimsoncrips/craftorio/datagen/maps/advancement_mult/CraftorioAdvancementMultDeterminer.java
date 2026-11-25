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

        addAdvancementMultiplier(multiplierValue, "adventure/adventuring_time", 2.0);
        addAdvancementMultiplier(multiplierValue, "adventure/arbalistic", 1.6);
        addAdvancementMultiplier(multiplierValue, "adventure/bullseye", 1.4);
        addAdvancementMultiplier(multiplierValue, "adventure/hero_of_the_village", 1.2);
        addAdvancementMultiplier(multiplierValue, "adventure/honey_block_slide", 0.52);
        addAdvancementMultiplier(multiplierValue, "adventure/kill_all_mobs", 2.4);
        addAdvancementMultiplier(multiplierValue, "adventure/kill_a_mob", 0.4);
        addAdvancementMultiplier(multiplierValue, "adventure/ol_betsy", 0.44);
        addAdvancementMultiplier(multiplierValue, "adventure/shoot_arrow", 0.4);
        addAdvancementMultiplier(multiplierValue, "adventure/sleep_in_bed", 0.4);
        addAdvancementMultiplier(multiplierValue, "adventure/sniper_duel", 1.0);
        addAdvancementMultiplier(multiplierValue, "adventure/summon_iron_golem", 0.6);
        addAdvancementMultiplier(multiplierValue, "adventure/throw_trident", 0.52);
        addAdvancementMultiplier(multiplierValue, "adventure/totem_of_undying", 0.8);
        addAdvancementMultiplier(multiplierValue, "adventure/trade", 0.44);
        addAdvancementMultiplier(multiplierValue, "adventure/two_birds_one_arrow", 1.4);
        addAdvancementMultiplier(multiplierValue, "adventure/very_very_frightening", 1.2);
        addAdvancementMultiplier(multiplierValue, "adventure/voluntary_exile", 0.8);
        addAdvancementMultiplier(multiplierValue, "adventure/whos_the_pillager_now", 0.48);
        addAdvancementMultiplier(multiplierValue, "end/dragon_breath", 1.0);
        addAdvancementMultiplier(multiplierValue, "end/dragon_egg", 1.4);
        addAdvancementMultiplier(multiplierValue, "end/elytra", 1.6);
        addAdvancementMultiplier(multiplierValue, "end/enter_end_gateway", 0.88);
        addAdvancementMultiplier(multiplierValue, "end/find_end_city", 1.4);
        addAdvancementMultiplier(multiplierValue, "end/kill_dragon", 2.0);
        addAdvancementMultiplier(multiplierValue, "end/levitate", 1.6);
        addAdvancementMultiplier(multiplierValue, "end/respawn_dragon", 1.8);
        addAdvancementMultiplier(multiplierValue, "husbandry/balanced_diet", 2.4);
        addAdvancementMultiplier(multiplierValue, "husbandry/bred_all_animals", 1.8);
        addAdvancementMultiplier(multiplierValue, "husbandry/breed_an_animal", 0.4);
        addAdvancementMultiplier(multiplierValue, "husbandry/complete_catalogue", 2.0);
        addAdvancementMultiplier(multiplierValue, "husbandry/fishy_business", 0.44);
        addAdvancementMultiplier(multiplierValue, "husbandry/obtain_netherite_hoe", 1.0);
        addAdvancementMultiplier(multiplierValue, "husbandry/plant_seed", 0.4);
        addAdvancementMultiplier(multiplierValue, "husbandry/safely_harvest_honey", 0.52);
        addAdvancementMultiplier(multiplierValue, "husbandry/silk_touch_nest", 0.8);
        addAdvancementMultiplier(multiplierValue, "husbandry/tactical_fishing", 1.0);
        addAdvancementMultiplier(multiplierValue, "husbandry/tame_an_animal", 0.48);
        addAdvancementMultiplier(multiplierValue, "nether/all_effects", 2.4);
        addAdvancementMultiplier(multiplierValue, "nether/all_potions", 1.8);
        addAdvancementMultiplier(multiplierValue, "nether/brew_potion", 0.56);
        addAdvancementMultiplier(multiplierValue, "nether/charge_respawn_anchor", 0.72);
        addAdvancementMultiplier(multiplierValue, "nether/create_beacon", 1.2);
        addAdvancementMultiplier(multiplierValue, "nether/create_full_beacon", 2.8);
        addAdvancementMultiplier(multiplierValue, "nether/distract_piglin", 0.52);
        addAdvancementMultiplier(multiplierValue, "nether/explore_nether", 1.4);
        addAdvancementMultiplier(multiplierValue, "nether/fast_travel", 1.8);
        addAdvancementMultiplier(multiplierValue, "nether/find_bastion", 0.8);
        addAdvancementMultiplier(multiplierValue, "nether/find_fortress", 0.8);
        addAdvancementMultiplier(multiplierValue, "nether/get_wither_skull", 1.4);
        addAdvancementMultiplier(multiplierValue, "nether/loot_bastion", 0.88);
        addAdvancementMultiplier(multiplierValue, "nether/netherite_armor", 1.6);
        addAdvancementMultiplier(multiplierValue, "nether/obtain_ancient_debris", 1.0);
        addAdvancementMultiplier(multiplierValue, "nether/obtain_blaze_rod", 0.88);
        addAdvancementMultiplier(multiplierValue, "nether/obtain_crying_obsidian", 0.68);
        addAdvancementMultiplier(multiplierValue, "nether/return_to_sender", 0.8);
        addAdvancementMultiplier(multiplierValue, "nether/ride_strider", 0.6);
        addAdvancementMultiplier(multiplierValue, "nether/summon_wither", 1.6);
        addAdvancementMultiplier(multiplierValue, "nether/uneasy_alliance", 2.0);
        addAdvancementMultiplier(multiplierValue, "nether/use_lodestone", 0.52);
        addAdvancementMultiplier(multiplierValue, "story/cure_zombie_villager", 1.4);
        addAdvancementMultiplier(multiplierValue, "story/deflect_arrow", 0.6);
        addAdvancementMultiplier(multiplierValue, "story/enchant_item", 0.6);
        addAdvancementMultiplier(multiplierValue, "story/enter_the_end", 1.6);
        addAdvancementMultiplier(multiplierValue, "story/enter_the_nether", 0.88);
        addAdvancementMultiplier(multiplierValue, "story/follow_ender_eye", 1.0);
        addAdvancementMultiplier(multiplierValue, "story/form_obsidian", 0.52);
        addAdvancementMultiplier(multiplierValue, "story/iron_tools", 0.48);
        addAdvancementMultiplier(multiplierValue, "story/lava_bucket", 0.48);
        addAdvancementMultiplier(multiplierValue, "story/mine_diamond", 0.8);
        addAdvancementMultiplier(multiplierValue, "story/mine_stone", 0.4);
        addAdvancementMultiplier(multiplierValue, "story/obtain_armor", 0.44);
        addAdvancementMultiplier(multiplierValue, "story/shiny_gear", 1.12);
        addAdvancementMultiplier(multiplierValue, "story/smelt_iron", 0.44);
        addAdvancementMultiplier(multiplierValue, "story/upgrade_tools", 0.4);
    }

    private static void addAdvancementMultiplier(Builder<String, Advancement> multiplierValue, String resourceLocation, double value) {
        ResourceLocation advancementLocation = ResourceLocation.withDefaultNamespace(resourceLocation);
        multiplierValue.add(advancementLocation, String.valueOf(value), false);
    }





}
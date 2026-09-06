package org.crimsoncrips.craftorio.datagen.maps;


import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;

import java.util.concurrent.CompletableFuture;

import static java.lang.Math.round;

@SuppressWarnings("Deprecated")
public class CraftorioPointsDeterminer extends DataMapProvider {

    //From Androsa (Gaia Dimension)
    public CraftorioPointsDeterminer(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        itemMap();
        effectMap();
        enchantMap();
        advancementMap();
    }

    protected void advancementMap() {
        Builder<String, Advancement> point_value = this.builder(CraftorioDataMaps.ADVANCEMENT_POINT_VALUE);

        addAdvancementValue(point_value, "adventure/adventuring_time", 1);
        addAdvancementValue(point_value, "adventure/arbalistic", 1);
        addAdvancementValue(point_value, "adventure/bullseye", 1);
        addAdvancementValue(point_value, "adventure/hero_of_the_village", 1);
        addAdvancementValue(point_value, "adventure/honey_block_slide", 1);
        addAdvancementValue(point_value, "adventure/kill_all_mobs", 1);
        addAdvancementValue(point_value, "adventure/kill_a_mob", 1);
        addAdvancementValue(point_value, "adventure/ol_betsy", 1);
        addAdvancementValue(point_value, "adventure/root", 1);
        addAdvancementValue(point_value, "adventure/shoot_arrow", 1);
        addAdvancementValue(point_value, "adventure/sleep_in_bed", 1);
        addAdvancementValue(point_value, "adventure/sniper_duel", 1);
        addAdvancementValue(point_value, "adventure/summon_iron_golem", 1);
        addAdvancementValue(point_value, "adventure/throw_trident", 1);
        addAdvancementValue(point_value, "adventure/totem_of_undying", 1);
        addAdvancementValue(point_value, "adventure/trade", 1);
        addAdvancementValue(point_value, "adventure/two_birds_one_arrow", 1);
        addAdvancementValue(point_value, "adventure/very_very_frightening", 1);
        addAdvancementValue(point_value, "adventure/voluntary_exile", 1);
        addAdvancementValue(point_value, "adventure/whos_the_pillager_now", 1);
        addAdvancementValue(point_value, "end/dragon_breath", 1);
        addAdvancementValue(point_value, "end/dragon_egg", 1);
        addAdvancementValue(point_value, "end/elytra", 1);
        addAdvancementValue(point_value, "end/enter_end_gateway", 1);
        addAdvancementValue(point_value, "end/find_end_city", 1);
        addAdvancementValue(point_value, "end/kill_dragon", 1);
        addAdvancementValue(point_value, "end/levitate", 1);
        addAdvancementValue(point_value, "end/respawn_dragon", 1);
        addAdvancementValue(point_value, "end/root", 1);
        addAdvancementValue(point_value, "husbandry/balanced_diet", 1);
        addAdvancementValue(point_value, "husbandry/bred_all_animals", 1);
        addAdvancementValue(point_value, "husbandry/breed_an_animal", 1);
        addAdvancementValue(point_value, "husbandry/complete_catalogue", 1);
        addAdvancementValue(point_value, "husbandry/fishy_business", 1);
        addAdvancementValue(point_value, "husbandry/obtain_netherite_hoe", 1);
        addAdvancementValue(point_value, "husbandry/plant_seed", 1);
        addAdvancementValue(point_value, "husbandry/root", 1);
        addAdvancementValue(point_value, "husbandry/safely_harvest_honey", 1);
        addAdvancementValue(point_value, "husbandry/silk_touch_nest", 1);
        addAdvancementValue(point_value, "husbandry/tactical_fishing", 1);
        addAdvancementValue(point_value, "husbandry/tame_an_animal", 1);
        addAdvancementValue(point_value, "nether/all_effects", 1);
        addAdvancementValue(point_value, "nether/all_potions", 1);
        addAdvancementValue(point_value, "nether/brew_potion", 1);
        addAdvancementValue(point_value, "nether/charge_respawn_anchor", 1);
        addAdvancementValue(point_value, "nether/create_beacon", 1);
        addAdvancementValue(point_value, "nether/create_full_beacon", 1);
        addAdvancementValue(point_value, "nether/distract_piglin", 1);
        addAdvancementValue(point_value, "nether/explore_nether", 1);
        addAdvancementValue(point_value, "nether/fast_travel", 1);
        addAdvancementValue(point_value, "nether/find_bastion", 1);
        addAdvancementValue(point_value, "nether/find_fortress", 1);
        addAdvancementValue(point_value, "nether/get_wither_skull", 1);
        addAdvancementValue(point_value, "nether/loot_bastion", 1);
        addAdvancementValue(point_value, "nether/netherite_armor", 1);
        addAdvancementValue(point_value, "nether/obtain_ancient_debris", 1);
        addAdvancementValue(point_value, "nether/obtain_blaze_rod", 1);
        addAdvancementValue(point_value, "nether/obtain_crying_obsidian", 1);
        addAdvancementValue(point_value, "nether/return_to_sender", 1);
        addAdvancementValue(point_value, "nether/ride_strider", 1);
        addAdvancementValue(point_value, "nether/root", 1);
        addAdvancementValue(point_value, "nether/summon_wither", 1);
        addAdvancementValue(point_value, "nether/uneasy_alliance", 1);
        addAdvancementValue(point_value, "nether/use_lodestone", 1);
        addAdvancementValue(point_value, "story/cure_zombie_villager", 1);
        addAdvancementValue(point_value, "story/deflect_arrow", 1);
        addAdvancementValue(point_value, "story/enchant_item", 1);
        addAdvancementValue(point_value, "story/enter_the_end", 1);
        addAdvancementValue(point_value, "story/enter_the_nether", 1);
        addAdvancementValue(point_value, "story/follow_ender_eye", 1);
        addAdvancementValue(point_value, "story/form_obsidian", 1);
        addAdvancementValue(point_value, "story/iron_tools", 1);
        addAdvancementValue(point_value, "story/lava_bucket", 1);
        addAdvancementValue(point_value, "story/mine_diamond", 1);
        addAdvancementValue(point_value, "story/mine_stone", 1);
        addAdvancementValue(point_value, "story/obtain_armor", 1);
        addAdvancementValue(point_value, "story/root", 1);
        addAdvancementValue(point_value, "story/shiny_gear", 1);
        addAdvancementValue(point_value, "story/smelt_iron", 1);
        addAdvancementValue(point_value, "story/upgrade_tools", 1);
    }

    protected void effectMap() {
        Builder<String, MobEffect> point_value = this.builder(CraftorioDataMaps.EFFECT_POINT_VALUE);
        addEffectValue(point_value, MobEffects.NIGHT_VISION,110);
        addEffectValue(point_value, MobEffects.INVISIBILITY,120);
        addEffectValue(point_value, MobEffects.JUMP,130);
        addEffectValue(point_value, MobEffects.FIRE_RESISTANCE,160);
        addEffectValue(point_value, MobEffects.MOVEMENT_SPEED,170);
        addEffectValue(point_value, MobEffects.MOVEMENT_SLOWDOWN,120);
        addEffectValue(point_value, MobEffects.DAMAGE_RESISTANCE,210);
        addEffectValue(point_value, MobEffects.WATER_BREATHING,120);
        addEffectValue(point_value, MobEffects.HEAL,110);
        addEffectValue(point_value, MobEffects.HARM,80);
        addEffectValue(point_value, MobEffects.POISON,130);
        addEffectValue(point_value, MobEffects.REGENERATION,150);
        addEffectValue(point_value, MobEffects.DAMAGE_BOOST,170);
        addEffectValue(point_value, MobEffects.WEAKNESS,90);
        addEffectValue(point_value, MobEffects.LUCK,300);
        addEffectValue(point_value, MobEffects.SLOW_FALLING,140);
        addEffectValue(point_value, MobEffects.WIND_CHARGED,90);
        addEffectValue(point_value, MobEffects.WEAVING,90);
        addEffectValue(point_value, MobEffects.OOZING,90);
        addEffectValue(point_value, MobEffects.INFESTED,90);
        addEffectValue(point_value, MobEffects.BAD_OMEN,60);

    }

    protected void enchantMap() {
        Builder<String, Enchantment> point_value = this.builder(CraftorioDataMaps.ENCHANTMENT_POINT_VALUE);
        addEnchantValue(point_value, Enchantments.AQUA_AFFINITY,130);
        addEnchantValue(point_value, Enchantments.BANE_OF_ARTHROPODS,110);
        addEnchantValue(point_value, Enchantments.BINDING_CURSE,80);
        addEnchantValue(point_value, Enchantments.BLAST_PROTECTION,120);
        addEnchantValue(point_value, Enchantments.BREACH,130);
        addEnchantValue(point_value, Enchantments.CHANNELING,140);
        addEnchantValue(point_value, Enchantments.DENSITY,130);
        addEnchantValue(point_value, Enchantments.DEPTH_STRIDER,130);
        addEnchantValue(point_value, Enchantments.EFFICIENCY,150);
        addEnchantValue(point_value, Enchantments.FEATHER_FALLING,120);
        addEnchantValue(point_value, Enchantments.FIRE_ASPECT,110);
        addEnchantValue(point_value, Enchantments.FIRE_PROTECTION,110);
        addEnchantValue(point_value, Enchantments.FLAME,120);
        addEnchantValue(point_value, Enchantments.FORTUNE,160);
        addEnchantValue(point_value, Enchantments.FROST_WALKER,120);
        addEnchantValue(point_value, Enchantments.IMPALING,130);
        addEnchantValue(point_value, Enchantments.INFINITY,170);
        addEnchantValue(point_value, Enchantments.KNOCKBACK,130);
        addEnchantValue(point_value, Enchantments.LOOTING,170);
        addEnchantValue(point_value, Enchantments.LOYALTY,150);
        addEnchantValue(point_value, Enchantments.LUCK_OF_THE_SEA,140);
        addEnchantValue(point_value, Enchantments.LURE,140);
        addEnchantValue(point_value, Enchantments.MENDING,190);
        addEnchantValue(point_value, Enchantments.MULTISHOT,130);
        addEnchantValue(point_value, Enchantments.PIERCING,130);
        addEnchantValue(point_value, Enchantments.POWER,150);
        addEnchantValue(point_value, Enchantments.PROJECTILE_PROTECTION,120);
        addEnchantValue(point_value, Enchantments.PROTECTION,150);
        addEnchantValue(point_value, Enchantments.PUNCH,120);
        addEnchantValue(point_value, Enchantments.QUICK_CHARGE,120);
        addEnchantValue(point_value, Enchantments.RESPIRATION,130);
        addEnchantValue(point_value, Enchantments.RIPTIDE,140);
        addEnchantValue(point_value, Enchantments.SHARPNESS,160);
        addEnchantValue(point_value, Enchantments.SILK_TOUCH,150);
        addEnchantValue(point_value, Enchantments.SMITE,130);
        addEnchantValue(point_value, Enchantments.SOUL_SPEED,210);
        addEnchantValue(point_value, Enchantments.SWEEPING_EDGE,140);
        addEnchantValue(point_value, Enchantments.SWIFT_SNEAK,240);
        addEnchantValue(point_value, Enchantments.THORNS,140);
        addEnchantValue(point_value, Enchantments.UNBREAKING,160);
        addEnchantValue(point_value, Enchantments.VANISHING_CURSE,80);
        addEnchantValue(point_value, Enchantments.WIND_BURST,220);

    }

    protected void itemMap() {
        Builder<String, Item> point_value = this.builder(CraftorioDataMaps.POINT_VALUE);
        addItemValue(point_value,ItemTags.LOGS, 2,0);
        int planks = 1;
        addItemValue(point_value,ItemTags.PLANKS, planks,1);
        int bamboo = 3;
        addItemValue(point_value,Items.BAMBOO.builtInRegistryHolder(),bamboo,0);
        addItemValue(point_value,ItemTags.BAMBOO_BLOCKS, (bamboo * 9),1);
        addItemValue(point_value,ItemTags.WOODEN_STAIRS, multiValCal(planks,6,4),3);
        addItemValue(point_value,ItemTags.WOODEN_SLABS, multiValCal(planks,3,6),3);
        addItemValue(point_value,ItemTags.WOODEN_FENCES, multiValCal(planks,5,3),3);
        addItemValue(point_value,ItemTags.FENCE_GATES, multiValCal(planks,4,1),3);
        addItemValue(point_value,ItemTags.WOODEN_DOORS, multiValCal(planks,6,3),3);
        addItemValue(point_value,ItemTags.WOODEN_TRAPDOORS, multiValCal(planks,6,2),3);
        addItemValue(point_value,ItemTags.WOODEN_PRESSURE_PLATES, (planks * 2),2);
        addItemValue(point_value,ItemTags.WOODEN_BUTTONS, planks,2);

        int sticks = multiValCal(planks,2,4);
        addItemValue(point_value,Items.STICK.builtInRegistryHolder(),sticks,2);

        int cobblestone = 2;
        addItemValue(point_value,Items.COBBLESTONE.builtInRegistryHolder(),cobblestone,0);
        addItemValue(point_value,Items.COBBLESTONE_STAIRS.builtInRegistryHolder(), multiValCal(cobblestone,6,4),2);
        addItemValue(point_value,Items.COBBLESTONE_SLAB.builtInRegistryHolder(), multiValCal(cobblestone,3,6),2);
        addItemValue(point_value,Items.COBBLESTONE_WALL.builtInRegistryHolder(), multiValCal(cobblestone,6,6),2);
        addItemValue(point_value,Items.MOSSY_COBBLESTONE.builtInRegistryHolder(),cobblestone,1);
        addItemValue(point_value,Items.MOSSY_COBBLESTONE_STAIRS.builtInRegistryHolder(), multiValCal(cobblestone,6,4),3);
        addItemValue(point_value,Items.MOSSY_COBBLESTONE_SLAB.builtInRegistryHolder(), multiValCal(cobblestone,3,6),3);
        addItemValue(point_value,Items.MOSSY_COBBLESTONE_WALL.builtInRegistryHolder(), multiValCal(cobblestone,6,6),3);

        int stone = 3;
        addItemValue(point_value,Items.STONE.builtInRegistryHolder(),stone,1);
        addItemValue(point_value,Items.STONE_STAIRS.builtInRegistryHolder(), multiValCal(stone,6,4),3);
        addItemValue(point_value,Items.STONE_SLAB.builtInRegistryHolder(), multiValCal(stone,3,6),3);
        addItemValue(point_value,Items.STONE_PRESSURE_PLATE.builtInRegistryHolder(),(stone * 2),2);
        addItemValue(point_value,Items.STONE_BUTTON.builtInRegistryHolder(),stone,2);
        addItemValue(point_value,Items.SMOOTH_STONE.builtInRegistryHolder(),stone,5);
        addItemValue(point_value,Items.SMOOTH_STONE_SLAB.builtInRegistryHolder(), multiValCal(stone + 3,3,6),7);

        addItemValue(point_value,Items.STONE_BRICKS.builtInRegistryHolder(),stone,2);
        addItemValue(point_value,Items.CRACKED_STONE_BRICKS.builtInRegistryHolder(),crackedBlock(stone),4);
        addItemValue(point_value,Items.STONE_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(stone,6,4),4);
        addItemValue(point_value,Items.STONE_BRICK_SLAB.builtInRegistryHolder(), multiValCal(stone,3,6),4);
        addItemValue(point_value,Items.STONE_BRICK_WALL.builtInRegistryHolder(), multiValCal(stone,6,6),4);
        addItemValue(point_value,Items.CHISELED_STONE_BRICKS.builtInRegistryHolder(), chiseledAndPillared(stone),5);
        addItemValue(point_value,Items.MOSSY_STONE_BRICKS.builtInRegistryHolder(),stone,3);
        addItemValue(point_value,Items.MOSSY_STONE_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(stone,6,4),5);
        addItemValue(point_value,Items.MOSSY_STONE_BRICK_SLAB.builtInRegistryHolder(), multiValCal(stone,3,6),5);
        addItemValue(point_value,Items.MOSSY_STONE_BRICK_WALL.builtInRegistryHolder(), multiValCal(stone,6,6),5);

        int calcite = 4;
        addItemValue(point_value,Items.CALCITE.builtInRegistryHolder(),calcite,0);

        int granite = 4;
        addItemValue(point_value,Items.GRANITE.builtInRegistryHolder(),granite,0);
        addItemValue(point_value,Items.GRANITE_STAIRS.builtInRegistryHolder(), multiValCal(granite,6,4),2);
        addItemValue(point_value,Items.GRANITE_SLAB.builtInRegistryHolder(), multiValCal(granite,3,6),2);
        addItemValue(point_value,Items.GRANITE_WALL.builtInRegistryHolder(), multiValCal(granite,6,6),2);
        addItemValue(point_value,Items.POLISHED_GRANITE.builtInRegistryHolder(),granite,1);
        addItemValue(point_value,Items.POLISHED_GRANITE_STAIRS.builtInRegistryHolder(), multiValCal(granite,6,4),3);
        addItemValue(point_value,Items.POLISHED_GRANITE_SLAB.builtInRegistryHolder(), multiValCal(granite,3,6),3);

        int diorite = 3;
        addItemValue(point_value,Items.DIORITE.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.DIORITE_STAIRS.builtInRegistryHolder(), multiValCal(diorite,6,4),2);
        addItemValue(point_value,Items.DIORITE_SLAB.builtInRegistryHolder(), multiValCal(diorite,3,6),2);
        addItemValue(point_value,Items.DIORITE_WALL.builtInRegistryHolder(), multiValCal(diorite,6,6),2);
        addItemValue(point_value,Items.POLISHED_DIORITE.builtInRegistryHolder(),diorite,1);
        addItemValue(point_value,Items.POLISHED_DIORITE_STAIRS.builtInRegistryHolder(), multiValCal(diorite + 2,6,4),3);
        addItemValue(point_value,Items.POLISHED_DIORITE_SLAB.builtInRegistryHolder(), multiValCal(diorite + 2,3,6),3);

        int andesite = 4;
        addItemValue(point_value,Items.ANDESITE.builtInRegistryHolder(),andesite,0);
        addItemValue(point_value,Items.ANDESITE_STAIRS.builtInRegistryHolder(), multiValCal(andesite,6,4),2);
        addItemValue(point_value,Items.ANDESITE_SLAB.builtInRegistryHolder(), multiValCal(andesite,3,6),2);
        addItemValue(point_value,Items.ANDESITE_WALL.builtInRegistryHolder(), multiValCal(andesite,6,6),2);
        addItemValue(point_value,Items.POLISHED_ANDESITE.builtInRegistryHolder(),andesite ,1);
        addItemValue(point_value,Items.POLISHED_ANDESITE_STAIRS.builtInRegistryHolder(), multiValCal(andesite + 2,6,4),3);
        addItemValue(point_value,Items.POLISHED_ANDESITE_SLAB.builtInRegistryHolder(), multiValCal(andesite + 2,3,6),3);


        int cobbled_deepslate = 4;
        addItemValue(point_value,Items.COBBLED_DEEPSLATE.builtInRegistryHolder(),cobbled_deepslate,0);
        addItemValue(point_value,Items.COBBLED_DEEPSLATE_STAIRS.builtInRegistryHolder(), multiValCal(cobbled_deepslate,6,4),2);
        addItemValue(point_value,Items.COBBLED_DEEPSLATE_SLAB.builtInRegistryHolder(), multiValCal(cobbled_deepslate,3,6),2);
        addItemValue(point_value,Items.COBBLED_DEEPSLATE_WALL.builtInRegistryHolder(), multiValCal(cobbled_deepslate,6,6),2);

        int deepslate = 5;
        addItemValue(point_value,Items.DEEPSLATE.builtInRegistryHolder(),deepslate,1);
        addItemValue(point_value,Items.CHISELED_DEEPSLATE.builtInRegistryHolder(), chiseledAndPillared(deepslate),4);
        addItemValue(point_value,Items.POLISHED_DEEPSLATE.builtInRegistryHolder(),deepslate,2);
        addItemValue(point_value,Items.POLISHED_DEEPSLATE_STAIRS.builtInRegistryHolder(), multiValCal(deepslate,6,4),4);
        addItemValue(point_value,Items.POLISHED_DEEPSLATE_SLAB.builtInRegistryHolder(), multiValCal(deepslate,3,6),4);
        addItemValue(point_value,Items.POLISHED_DEEPSLATE_WALL.builtInRegistryHolder(), multiValCal(deepslate,6,6),4);

        addItemValue(point_value,Items.DEEPSLATE_BRICKS.builtInRegistryHolder(),deepslate,2);
        addItemValue(point_value,Items.CRACKED_DEEPSLATE_BRICKS.builtInRegistryHolder(),crackedBlock(deepslate),4);
        addItemValue(point_value,Items.DEEPSLATE_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(deepslate,6,4),4);
        addItemValue(point_value,Items.DEEPSLATE_BRICK_SLAB.builtInRegistryHolder(), multiValCal(deepslate,3,6),4);
        addItemValue(point_value,Items.DEEPSLATE_BRICK_WALL.builtInRegistryHolder(), multiValCal(deepslate,6,6),4);

        addItemValue(point_value,Items.DEEPSLATE_TILES.builtInRegistryHolder(),deepslate,3);
        addItemValue(point_value,Items.CRACKED_DEEPSLATE_TILES.builtInRegistryHolder(),crackedBlock(deepslate + 4),5);
        addItemValue(point_value,Items.DEEPSLATE_TILE_STAIRS.builtInRegistryHolder(), multiValCal(deepslate + 4,6,4),5);
        addItemValue(point_value,Items.DEEPSLATE_TILE_SLAB.builtInRegistryHolder(), multiValCal(deepslate + 4,3,6),5);
        addItemValue(point_value,Items.DEEPSLATE_TILE_WALL.builtInRegistryHolder(), multiValCal(deepslate + 4,6,6),5);

        int tuff = 3;
        addItemValue(point_value,Items.TUFF.builtInRegistryHolder(),tuff,0);
        addItemValue(point_value,Items.TUFF_STAIRS.builtInRegistryHolder(), multiValCal(tuff,6,4),2);
        addItemValue(point_value,Items.TUFF_SLAB.builtInRegistryHolder(), multiValCal(tuff,3,6),2);
        addItemValue(point_value,Items.TUFF_WALL.builtInRegistryHolder(), multiValCal(tuff,6,6),2);
        addItemValue(point_value,Items.CHISELED_TUFF.builtInRegistryHolder(), chiseledAndPillared(tuff),3);
        addItemValue(point_value,Items.POLISHED_TUFF.builtInRegistryHolder(),tuff,1);
        addItemValue(point_value,Items.POLISHED_TUFF_STAIRS.builtInRegistryHolder(), multiValCal(tuff,6,4),3);
        addItemValue(point_value,Items.POLISHED_TUFF_SLAB.builtInRegistryHolder(), multiValCal(tuff,3,6),3);
        addItemValue(point_value,Items.POLISHED_TUFF_WALL.builtInRegistryHolder(), multiValCal(tuff,6,6),3);

        addItemValue(point_value,Items.TUFF_BRICKS.builtInRegistryHolder(),tuff,2);
        addItemValue(point_value,Items.TUFF_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(tuff,6,4),4);
        addItemValue(point_value,Items.TUFF_BRICK_SLAB.builtInRegistryHolder(), multiValCal(tuff,3,6),4);
        addItemValue(point_value,Items.TUFF_BRICK_WALL.builtInRegistryHolder(), multiValCal(tuff,6,6),4);
        addItemValue(point_value,Items.CHISELED_TUFF_BRICKS.builtInRegistryHolder(), chiseledAndPillared(tuff),4);

        int brick = 6;
        addItemValue(point_value,Items.BRICK.builtInRegistryHolder(),brick,0);
        int brickblock = brick * 4;
        addItemValue(point_value,Items.BRICKS.builtInRegistryHolder(),brickblock,1);
        addItemValue(point_value,Items.BRICK_STAIRS.builtInRegistryHolder(), multiValCal(brickblock,6,4),3);
        addItemValue(point_value,Items.BRICK_SLAB.builtInRegistryHolder(), multiValCal(brickblock,3,6),3);
        addItemValue(point_value,Items.BRICK_WALL.builtInRegistryHolder(), multiValCal(brickblock,6,6),3);
        int wheat = 6;
        int mud = 2 + wheat;
        int carrot = 7;
        int beetroot = 5;
        addItemValue(point_value,Items.COCOA_BEANS.builtInRegistryHolder(),11,0);
        addItemValue(point_value,Items.BEETROOT.builtInRegistryHolder(),beetroot,0);
        addItemValue(point_value,Items.CARROT.builtInRegistryHolder(),carrot,0);
        addItemValue(point_value,Items.POTATO.builtInRegistryHolder(),7,0);
        addItemValue(point_value,Items.WHEAT.builtInRegistryHolder(),wheat,0);
        addItemValue(point_value,Items.HAY_BLOCK.builtInRegistryHolder(), (wheat * 9),2);
        addItemValue(point_value,Items.PACKED_MUD.builtInRegistryHolder(),mud,1);
        addItemValue(point_value,Items.MUD_BRICKS.builtInRegistryHolder(),mud * 4,2);
        addItemValue(point_value,Items.MUD_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(mud * 4,6,4),4);
        addItemValue(point_value,Items.MUD_BRICK_SLAB.builtInRegistryHolder(), multiValCal(mud * 4,3,6),4);
        addItemValue(point_value,Items.MUD_BRICK_WALL.builtInRegistryHolder(), multiValCal(mud * 4,6,6),4);

        int sand = 2;
        int sandstone = sand + 2;
        addItemValue(point_value,Items.GRAVEL.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.SAND.builtInRegistryHolder(),sand,0);

        addItemValue(point_value,Items.SANDSTONE.builtInRegistryHolder(),sandstone,1);
        addItemValue(point_value,Items.SANDSTONE_STAIRS.builtInRegistryHolder(), multiValCal(sandstone,6,4),3);
        addItemValue(point_value,Items.SANDSTONE_SLAB.builtInRegistryHolder(), multiValCal(sandstone,3,6),3);
        addItemValue(point_value,Items.SANDSTONE_WALL.builtInRegistryHolder(), multiValCal(sandstone,6,6),3);
        addItemValue(point_value,Items.CHISELED_SANDSTONE.builtInRegistryHolder(), chiseledAndPillared(sandstone),3);
        addItemValue(point_value,Items.SMOOTH_SANDSTONE.builtInRegistryHolder(),sandstone,2);
        addItemValue(point_value,Items.SMOOTH_SANDSTONE_STAIRS.builtInRegistryHolder(), multiValCal(sandstone,6,4),4);
        addItemValue(point_value,Items.SMOOTH_SANDSTONE_SLAB.builtInRegistryHolder(), multiValCal(sandstone,3,6),4);
        addItemValue(point_value,Items.CUT_SANDSTONE.builtInRegistryHolder(),sandstone,3);
        addItemValue(point_value,Items.CUT_STANDSTONE_SLAB.builtInRegistryHolder(), multiValCal(sandstone,3,6),5);

        int red_sandstone = 5;
        addItemValue(point_value,Items.RED_SANDSTONE.builtInRegistryHolder(),red_sandstone,1);
        addItemValue(point_value,Items.RED_SANDSTONE_STAIRS.builtInRegistryHolder(), multiValCal(red_sandstone,6,4),3);
        addItemValue(point_value,Items.RED_SANDSTONE_SLAB.builtInRegistryHolder(), multiValCal(red_sandstone,3,6),3);
        addItemValue(point_value,Items.RED_SANDSTONE_WALL.builtInRegistryHolder(), multiValCal(red_sandstone,6,3),3);
        addItemValue(point_value,Items.CHISELED_RED_SANDSTONE.builtInRegistryHolder(), chiseledAndPillared(red_sandstone),4);
        addItemValue(point_value,Items.SMOOTH_RED_SANDSTONE.builtInRegistryHolder(),red_sandstone,2);
        addItemValue(point_value,Items.SMOOTH_RED_SANDSTONE_STAIRS.builtInRegistryHolder(), multiValCal(red_sandstone,6,4),4);
        addItemValue(point_value,Items.SMOOTH_RED_SANDSTONE_SLAB.builtInRegistryHolder(), multiValCal(red_sandstone,3,6),4);
        addItemValue(point_value,Items.CUT_RED_SANDSTONE.builtInRegistryHolder(),red_sandstone,3);
        addItemValue(point_value,Items.CUT_RED_SANDSTONE_SLAB.builtInRegistryHolder(), multiValCal(red_sandstone,3,6),5);

        int prismarine = 8;
        addItemValue(point_value,Items.PRISMARINE_CRYSTALS.builtInRegistryHolder(),11,0);
        addItemValue(point_value,Items.SEA_LANTERN.builtInRegistryHolder(),(prismarine * 4) + (11 * 5),2);
        addItemValue(point_value,Items.PRISMARINE_SHARD.builtInRegistryHolder(),prismarine,0);
        addItemValue(point_value,Items.PRISMARINE.builtInRegistryHolder(),(prismarine * 4),1);
        addItemValue(point_value,Items.PRISMARINE_STAIRS.builtInRegistryHolder(), multiValCal((prismarine * 4),6,4),3);
        addItemValue(point_value,Items.PRISMARINE_SLAB.builtInRegistryHolder(), multiValCal((prismarine * 4),3,6),3);
        addItemValue(point_value,Items.PRISMARINE_WALL.builtInRegistryHolder(), multiValCal((prismarine * 4),6,6),3);
        addItemValue(point_value,Items.PRISMARINE_BRICKS.builtInRegistryHolder(),(prismarine * 9),2);
        addItemValue(point_value,Items.PRISMARINE_BRICK_STAIRS.builtInRegistryHolder(), multiValCal((prismarine * 9),6,4),4);
        addItemValue(point_value,Items.PRISMARINE_BRICK_SLAB.builtInRegistryHolder(), multiValCal((prismarine * 9),3,6),4);
        addItemValue(point_value,Items.DARK_PRISMARINE.builtInRegistryHolder(),(prismarine * 8),4);
        addItemValue(point_value,Items.DARK_PRISMARINE_STAIRS.builtInRegistryHolder(), multiValCal((prismarine * 8),6,4),6);
        addItemValue(point_value,Items.DARK_PRISMARINE_SLAB.builtInRegistryHolder(), multiValCal((prismarine * 8),3,6),6);

        int netherbrick = 3;
        int netherbricks = netherbrick * 4;
        addItemValue(point_value,Items.NETHERRACK.builtInRegistryHolder(),1,0);
        addItemValue(point_value,Items.NETHER_BRICK.builtInRegistryHolder(),netherbrick,1);
        addItemValue(point_value,Items.NETHER_BRICKS.builtInRegistryHolder(),netherbricks,2);
        addItemValue(point_value,Items.CRACKED_NETHER_BRICKS.builtInRegistryHolder(),crackedBlock(netherbricks),4);
        addItemValue(point_value,Items.NETHER_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(netherbricks,6,4),4);
        addItemValue(point_value,Items.NETHER_BRICK_SLAB.builtInRegistryHolder(), multiValCal(netherbricks,3,6),4);
        addItemValue(point_value,Items.NETHER_BRICK_WALL.builtInRegistryHolder(), multiValCal(netherbricks,6,6),4);
        addItemValue(point_value,Items.NETHER_BRICK_FENCE.builtInRegistryHolder(), multiValCal(netherbricks,5,3),4);
        addItemValue(point_value,Items.CHISELED_NETHER_BRICKS.builtInRegistryHolder(), chiseledAndPillared(netherbricks),5);

        int netherwart = 11;
        addItemValue(point_value,Items.NETHER_WART.builtInRegistryHolder(),netherwart,0);
        int red_netherbricks = (netherbrick * 2) + (netherwart * 2);
        addItemValue(point_value,Items.RED_NETHER_BRICKS.builtInRegistryHolder(),red_netherbricks,1);
        addItemValue(point_value,Items.RED_NETHER_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(red_netherbricks,6,4),3);
        addItemValue(point_value,Items.RED_NETHER_BRICK_SLAB.builtInRegistryHolder(), multiValCal(red_netherbricks,3,6),3);
        addItemValue(point_value,Items.RED_NETHER_BRICK_WALL.builtInRegistryHolder(), multiValCal(red_netherbricks,6,6),3);

        int basalt = 3;
        addItemValue(point_value,Items.BASALT.builtInRegistryHolder(),basalt,0);
        addItemValue(point_value,Items.SMOOTH_BASALT.builtInRegistryHolder(),basalt,2);
        addItemValue(point_value,Items.POLISHED_BASALT.builtInRegistryHolder(),basalt,1);

        int blackstone = 4;
        addItemValue(point_value,Items.BLACKSTONE.builtInRegistryHolder(),blackstone,0);
        addItemValue(point_value,Items.GILDED_BLACKSTONE.builtInRegistryHolder(),40,0);
        addItemValue(point_value,Items.BLACKSTONE_STAIRS.builtInRegistryHolder(), multiValCal(blackstone,6,4),2);
        addItemValue(point_value,Items.BLACKSTONE_SLAB.builtInRegistryHolder(), multiValCal(blackstone,3,6),2);
        addItemValue(point_value,Items.BLACKSTONE_WALL.builtInRegistryHolder(), multiValCal(blackstone,6,6),2);

        int blackstone_block = blackstone * 4;
        addItemValue(point_value,Items.CHISELED_POLISHED_BLACKSTONE.builtInRegistryHolder(), chiseledAndPillared(blackstone_block),4);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE.builtInRegistryHolder(),blackstone_block,1);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_STAIRS.builtInRegistryHolder(), multiValCal(blackstone_block,6,4),3);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_SLAB.builtInRegistryHolder(), multiValCal(blackstone_block,3,6),3);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_WALL.builtInRegistryHolder(), multiValCal(blackstone_block,6,6),3);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_PRESSURE_PLATE.builtInRegistryHolder(),(blackstone_block * 2),2);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_BUTTON.builtInRegistryHolder(),blackstone_block,2);

        addItemValue(point_value,Items.POLISHED_BLACKSTONE_BRICKS.builtInRegistryHolder(),blackstone_block * 4,2);
        addItemValue(point_value,Items.CRACKED_POLISHED_BLACKSTONE_BRICKS.builtInRegistryHolder(),crackedBlock(blackstone_block * 4),4);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(blackstone_block * 4,6,4),4);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_BRICK_SLAB.builtInRegistryHolder(), multiValCal(blackstone_block * 4,3,6),4);
        addItemValue(point_value,Items.POLISHED_BLACKSTONE_BRICK_WALL.builtInRegistryHolder(), multiValCal(blackstone_block * 4,6,6),4);

        int endstone = 12;
        addItemValue(point_value,Items.END_STONE.builtInRegistryHolder(),endstone,0);
        addItemValue(point_value,Items.END_STONE_BRICKS.builtInRegistryHolder(),endstone + 2,1);
        addItemValue(point_value,Items.END_STONE_BRICK_STAIRS.builtInRegistryHolder(), multiValCal(endstone + 2,6,4),3);
        addItemValue(point_value,Items.END_STONE_BRICK_SLAB.builtInRegistryHolder(), multiValCal(endstone + 2,3,6),3);
        addItemValue(point_value,Items.END_STONE_BRICK_WALL.builtInRegistryHolder(), multiValCal(endstone + 2,6,6),3);

        int chorusFruit = 7;
        int purpur = chorusFruit + 1;
        addItemValue(point_value,Items.CHORUS_FRUIT.builtInRegistryHolder(),chorusFruit,0);
        addItemValue(point_value,Items.POPPED_CHORUS_FRUIT.builtInRegistryHolder(),purpur,1);
        addItemValue(point_value,Items.PURPUR_BLOCK.builtInRegistryHolder(),purpur,2);
        addItemValue(point_value,Items.PURPUR_PILLAR.builtInRegistryHolder(), chiseledAndPillared(purpur),5);
        addItemValue(point_value,Items.PURPUR_STAIRS.builtInRegistryHolder(), multiValCal(purpur,6,4),4);
        addItemValue(point_value,Items.PURPUR_SLAB.builtInRegistryHolder(), multiValCal(purpur,3,6),4);

        int iron = 30;
        int gold = 40;
        int iron_nugget = round((float) iron / 9);
        int gold_nugget = round((float) gold / 9);
        int coal = 10;
        int redstone = 20;
        int emerald = 70;
        int lapis_lazuli = 25;
        int diamond = 120;
        int netherite_scrap = 200;
        int netherite_ingot = (gold * 4) + (netherite_scrap * 4);
        int copper_ingot = 15;
        addItemValue(point_value,Items.IRON_NUGGET.builtInRegistryHolder(),iron_nugget,0);
        addItemValue(point_value,Items.GOLD_NUGGET.builtInRegistryHolder(),gold_nugget,0);
        addItemValue(point_value,Items.ANCIENT_DEBRIS.builtInRegistryHolder(),150,1);

        int chain = iron + (iron_nugget * 2);
        addItemValue(point_value,Items.CHARCOAL.builtInRegistryHolder(),8,0);
        addItemValue(point_value,Items.COAL.builtInRegistryHolder(),coal,0);
        addItemValue(point_value,Items.IRON_INGOT.builtInRegistryHolder(),iron,0);
        addItemValue(point_value,Items.GOLD_INGOT.builtInRegistryHolder(),gold,0);
        addItemValue(point_value,Items.REDSTONE.builtInRegistryHolder(),redstone,0);
        addItemValue(point_value,Items.EMERALD.builtInRegistryHolder(),emerald,0);
        addItemValue(point_value,Items.LAPIS_LAZULI.builtInRegistryHolder(),lapis_lazuli,0);
        addItemValue(point_value,Items.DIAMOND.builtInRegistryHolder(),diamond,0);
        addItemValue(point_value,Items.NETHERITE_SCRAP.builtInRegistryHolder(),netherite_scrap,1);
        addItemValue(point_value,Items.COPPER_INGOT.builtInRegistryHolder(),copper_ingot,0);
        addItemValue(point_value,Items.NETHERITE_INGOT.builtInRegistryHolder(),netherite_ingot,1);

        addItemValue(point_value,Items.IRON_BARS.builtInRegistryHolder(), multiValCal(iron,6,16),2);
        addItemValue(point_value,Items.IRON_DOOR.builtInRegistryHolder(), multiValCal(iron,6,3),2);
        addItemValue(point_value,Items.IRON_TRAPDOOR.builtInRegistryHolder(), multiValCal(iron,4,1),2);
        addItemValue(point_value,Items.HEAVY_WEIGHTED_PRESSURE_PLATE.builtInRegistryHolder(),(iron * 2),1);
        addItemValue(point_value,Items.CHAIN.builtInRegistryHolder(),chain,3);
        addItemValue(point_value,Items.LIGHT_WEIGHTED_PRESSURE_PLATE.builtInRegistryHolder(),(gold * 2),1);


        addItemValue(point_value,Items.COAL_BLOCK.builtInRegistryHolder(),(coal * 9),1);
        addItemValue(point_value,Items.IRON_BLOCK.builtInRegistryHolder(),(iron * 9),1);
        addItemValue(point_value,Items.GOLD_BLOCK.builtInRegistryHolder(),(gold * 9),1);
        addItemValue(point_value,Items.REDSTONE_BLOCK.builtInRegistryHolder(),(redstone * 9),1);
        addItemValue(point_value,Items.EMERALD_BLOCK.builtInRegistryHolder(),(emerald * 9),1);
        addItemValue(point_value,Items.LAPIS_BLOCK.builtInRegistryHolder(),(lapis_lazuli * 9),1);
        addItemValue(point_value,Items.DIAMOND_BLOCK.builtInRegistryHolder(),(diamond * 9),1);
        addItemValue(point_value,Items.NETHERITE_BLOCK.builtInRegistryHolder(),(netherite_ingot * 9),1);

        addItemValue(point_value,Items.COAL_ORE.builtInRegistryHolder(),coal,0);
        addItemValue(point_value,Items.DEEPSLATE_COAL_ORE.builtInRegistryHolder(),coal,5);
        addItemValue(point_value,Items.IRON_ORE.builtInRegistryHolder(),iron,0);
        addItemValue(point_value,Items.DEEPSLATE_IRON_ORE.builtInRegistryHolder(),iron,5);
        addItemValue(point_value,Items.COPPER_ORE.builtInRegistryHolder(),copper_ingot,0);
        addItemValue(point_value,Items.DEEPSLATE_COPPER_ORE.builtInRegistryHolder(),copper_ingot,5);
        addItemValue(point_value,Items.GOLD_ORE.builtInRegistryHolder(),gold,0);
        addItemValue(point_value,Items.DEEPSLATE_GOLD_ORE.builtInRegistryHolder(),gold,5);
        addItemValue(point_value,Items.REDSTONE_ORE.builtInRegistryHolder(),redstone,0);
        addItemValue(point_value,Items.DEEPSLATE_REDSTONE_ORE.builtInRegistryHolder(),redstone,5);
        addItemValue(point_value,Items.EMERALD_ORE.builtInRegistryHolder(),emerald,0);
        addItemValue(point_value,Items.DEEPSLATE_EMERALD_ORE.builtInRegistryHolder(),emerald,5);
        addItemValue(point_value,Items.LAPIS_ORE.builtInRegistryHolder(),lapis_lazuli,0);
        addItemValue(point_value,Items.DEEPSLATE_LAPIS_ORE.builtInRegistryHolder(),lapis_lazuli,5);
        addItemValue(point_value,Items.DIAMOND_ORE.builtInRegistryHolder(),diamond,0);
        addItemValue(point_value,Items.DEEPSLATE_DIAMOND_ORE.builtInRegistryHolder(),diamond,5);

        int quartz = 8;
        addItemValue(point_value,Items.QUARTZ.builtInRegistryHolder(),quartz,0);
        int quartzBlock = (quartz * 4);
        addItemValue(point_value,Items.QUARTZ_BLOCK.builtInRegistryHolder(),quartzBlock,1);
        addItemValue(point_value,Items.QUARTZ_STAIRS.builtInRegistryHolder(), multiValCal(quartzBlock,6,4),3);
        addItemValue(point_value,Items.QUARTZ_SLAB.builtInRegistryHolder(), multiValCal(quartzBlock,3,6),3);
        addItemValue(point_value,Items.CHISELED_QUARTZ_BLOCK.builtInRegistryHolder(), chiseledAndPillared(quartzBlock),5);
        addItemValue(point_value,Items.QUARTZ_BRICKS.builtInRegistryHolder(),quartzBlock,2);
        addItemValue(point_value,Items.QUARTZ_PILLAR.builtInRegistryHolder(), chiseledAndPillared(quartzBlock),5);
        addItemValue(point_value,Items.SMOOTH_QUARTZ.builtInRegistryHolder(), quartzBlock, 3);
        addItemValue(point_value,Items.SMOOTH_QUARTZ_STAIRS.builtInRegistryHolder(), multiValCal(quartzBlock,6,4), 3);
        addItemValue(point_value,Items.SMOOTH_QUARTZ_SLAB.builtInRegistryHolder(), multiValCal(quartzBlock,3,6), 3);

        int amethyst = 5;
        addItemValue(point_value,Items.AMETHYST_SHARD.builtInRegistryHolder(),amethyst,0);
        addItemValue(point_value,Items.AMETHYST_BLOCK.builtInRegistryHolder(),(amethyst * 4),1);

        int copper_block = (15 * 9);
        addItemValue(point_value, CraftorioItemTagGen.COPPER,copper_block,1);
        addItemValue(point_value, CraftorioItemTagGen.CHISELED_COPPER,chiseledAndPillared(copper_block),4);
        addItemValue(point_value, CraftorioItemTagGen.COPPER_GRATE, multiValCal(copper_block,4,4),4);
        addItemValue(point_value, CraftorioItemTagGen.CUT_COPPER, multiValCal(copper_block,4,4),2);
        addItemValue(point_value, CraftorioItemTagGen.CUT_COPPER_STAIRS, multiValCal(multiValCal(copper_block,4,4),6,4),4);
        addItemValue(point_value, CraftorioItemTagGen.CUT_COPPER_SLAB, multiValCal(multiValCal(copper_block,4,4),3,6),4);
        addItemValue(point_value, CraftorioItemTagGen.COPPER_DOOR, multiValCal(copper_ingot,6,3),4);
        addItemValue(point_value, CraftorioItemTagGen.COPPER_TRAPDOOR, multiValCal(copper_ingot,6,2),4);
        addItemValue(point_value, CraftorioItemTagGen.COPPER_BULB, multiValCal(copper_block,3,4),6);

        int string = 6;
        int wool = string * 4;
        int honeycomb = 12;
        addItemValue(point_value,Items.STRING.builtInRegistryHolder(),string,0);
        addItemValue(point_value,ItemTags.WOOL,wool,1);
        addItemValue(point_value,ItemTags.WOOL_CARPETS, multiValCal(wool,2,3),2);

        addItemValue(point_value,Items.HONEYCOMB.builtInRegistryHolder(),honeycomb,0);
        addItemValue(point_value,Items.HONEYCOMB_BLOCK.builtInRegistryHolder(),(honeycomb * 4),1);
        addItemValue(point_value,Items.BEE_NEST.builtInRegistryHolder(),30,2);
        addItemValue(point_value,Items.BEEHIVE.builtInRegistryHolder(),(honeycomb * 3) + (planks * 3),2);

        addItemValue(point_value,Items.CLAY_BALL.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.CLAY.builtInRegistryHolder(),(2 * 4),1);
        addItemValue(point_value,Tags.Items.DYES,2,1);
        addItemValue(point_value,ItemTags.TERRACOTTA,(2 * 4),2);
        addItemValue(point_value,Tags.Items.GLAZED_TERRACOTTAS,(2 * 4),3);
        addItemValue(point_value,Tags.Items.CONCRETE_POWDERS, multiValCal(sand,8,8),1);
        addItemValue(point_value,Tags.Items.CONCRETES, multiValCal(2,8,8),2);

        int glass = 3;
        addItemValue(point_value,Tags.Items.GLASS_BLOCKS,glass,1);
        addItemValue(point_value,Tags.Items.GLASS_PANES, multiValCal(glass,6,16),2);
        addItemValue(point_value,ItemTags.CANDLES,string + honeycomb,1);
        addItemValue(point_value,Items.SHULKER_SHELL.builtInRegistryHolder(),32,0);
        addItemValue(point_value,Tags.Items.SHULKER_BOXES,(32 * 2),1);
        addItemValue(point_value,ItemTags.BEDS,(wool * 3) + (planks * 3),2);
        addItemValue(point_value,ItemTags.BANNERS, multiValCal(wool,6,1),2);
        addItemValue(point_value,ItemTags.DIRT,2,0);
        addItemValue(point_value,Items.RED_SAND.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.ICE.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.PACKED_ICE.builtInRegistryHolder(), multiValCal(3,9,1),1);
        addItemValue(point_value,Items.BLUE_ICE.builtInRegistryHolder(), multiValCal(multiValCal(3,9,1),9,1),2);
        addItemValue(point_value,Items.SNOWBALL.builtInRegistryHolder(),1,0);
        addItemValue(point_value,Items.SNOW.builtInRegistryHolder(),1 * 2,1);
        addItemValue(point_value,Items.SNOW_BLOCK.builtInRegistryHolder(),(1 * 9),1);
        addItemValue(point_value,Items.MOSS_BLOCK.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.MOSS_CARPET.builtInRegistryHolder(), multiValCal(2,2,3),1);

        int obsidian = 60;
        int slime_ball = 9;

        int blaze_rod = 12;
        int blaze_powder = multiValCal(blaze_rod,1,2);
        addItemValue(point_value,Items.SLIME_BALL.builtInRegistryHolder(),slime_ball,0);
        addItemValue(point_value,Items.SLIME_BLOCK.builtInRegistryHolder(),(slime_ball * 4),1);

        addItemValue(point_value,Items.OBSIDIAN.builtInRegistryHolder(),obsidian,0);
        addItemValue(point_value,Items.CRYING_OBSIDIAN.builtInRegistryHolder(),obsidian,1);
        addItemValue(point_value,Items.POINTED_DRIPSTONE.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.DRIPSTONE_BLOCK.builtInRegistryHolder(), multiValCal(2,4,1),1);
        addItemValue(point_value,Items.MAGMA_CREAM.builtInRegistryHolder(),slime_ball + blaze_powder,1);
        addItemValue(point_value,Items.MAGMA_BLOCK.builtInRegistryHolder(),(6 * 4),2);
        addItemValue(point_value,Items.CRIMSON_NYLIUM.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.WARPED_NYLIUM.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.SOUL_SAND.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.SOUL_SOIL.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.BONE.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.BONE_MEAL.builtInRegistryHolder(), multiValCal(4,1,3),1);
        addItemValue(point_value,Items.BONE_BLOCK.builtInRegistryHolder(), multiValCal(4,1,3) * 9 ,2);

        addItemValue(point_value,Items.NETHER_GOLD_ORE.builtInRegistryHolder(),gold_nugget * 3,0);
        addItemValue(point_value,Items.NETHER_QUARTZ_ORE.builtInRegistryHolder(),quartz,0);

        addItemValue(point_value,Items.RAW_IRON.builtInRegistryHolder(),iron - 2,0);
        addItemValue(point_value,Items.RAW_GOLD.builtInRegistryHolder(),gold - 2,0);
        addItemValue(point_value,Items.RAW_COPPER.builtInRegistryHolder(),copper_ingot - 2,0);
        addItemValue(point_value,Items.RAW_IRON_BLOCK.builtInRegistryHolder(),(iron - 2) * 4,0);
        addItemValue(point_value,Items.RAW_GOLD_BLOCK.builtInRegistryHolder(),(gold - 2) * 4,0);
        addItemValue(point_value,Items.RAW_COPPER_BLOCK.builtInRegistryHolder(),(copper_ingot - 2) * 4,0);

        int glowstone_dust = 12;
        addItemValue(point_value,Items.GLOWSTONE_DUST.builtInRegistryHolder(),glowstone_dust,0);
        addItemValue(point_value,Items.GLOWSTONE.builtInRegistryHolder(),(glowstone_dust * 4),1);

        addItemValue(point_value,Items.MANGROVE_ROOTS.builtInRegistryHolder(),1,0);
        addItemValue(point_value,Items.MUDDY_MANGROVE_ROOTS.builtInRegistryHolder(),5,1);

        addItemValue(point_value,Items.MUSHROOM_STEM.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.BROWN_MUSHROOM_BLOCK.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.RED_MUSHROOM_BLOCK.builtInRegistryHolder(),3,0);

        addItemValue(point_value,ItemTags.LEAVES,2,0);
        addItemValue(point_value,ItemTags.WART_BLOCKS,9,0);

        addItemValue(point_value,Items.SHROOMLIGHT.builtInRegistryHolder(),10,0);
        addItemValue(point_value,ItemTags.SAPLINGS,2,0);

        int mushrooms = 2;
        addItemValue(point_value,Tags.Items.MUSHROOMS,mushrooms,0);
        addItemValue(point_value,Items.CRIMSON_FUNGUS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.WARPED_FUNGUS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.SHORT_GRASS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.FERN.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.CRIMSON_ROOTS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.WARPED_ROOTS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.NETHER_SPROUTS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.TALL_GRASS.builtInRegistryHolder(),2,0);
        addItemValue(point_value,ItemTags.FLOWERS,2,0);
        addItemValue(point_value,Items.LARGE_FERN.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.VINE.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.TWISTING_VINES.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.WEEPING_VINES.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.BIG_DRIPLEAF.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.SMALL_DRIPLEAF.builtInRegistryHolder(),4,0);

        int sugar_cane = 7;
        addItemValue(point_value,Items.SUGAR_CANE.builtInRegistryHolder(),sugar_cane,0);
        addItemValue(point_value,Items.CACTUS.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.CHORUS_PLANT.builtInRegistryHolder(),chorusFruit,0);
        addItemValue(point_value,Items.CHORUS_FLOWER.builtInRegistryHolder(),13,0);
        addItemValue(point_value,Items.GLOW_LICHEN.builtInRegistryHolder(),6,0);
        addItemValue(point_value,Items.HANGING_ROOTS.builtInRegistryHolder(),6,0);
        addItemValue(point_value,Items.FROGSPAWN.builtInRegistryHolder(),16,0);
        addItemValue(point_value,Items.TURTLE_EGG.builtInRegistryHolder(),14,0);
        addItemValue(point_value,Items.SNIFFER_EGG.builtInRegistryHolder(),22,0);
        addItemValue(point_value,Tags.Items.SEEDS,2,0);
        addItemValue(point_value,Items.GLOW_BERRIES.builtInRegistryHolder(),6,0);
        addItemValue(point_value,Items.SWEET_BERRIES.builtInRegistryHolder(),4,0);
        addItemValue(point_value,Items.LILY_PAD.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.SEAGRASS.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.KELP.builtInRegistryHolder(),2,0);
        addItemValue(point_value,Items.SEA_PICKLE.builtInRegistryHolder(),5,0);
        addItemValue(point_value,Items.DRIED_KELP.builtInRegistryHolder(),6,0);
        addItemValue(point_value,Items.DRIED_KELP_BLOCK.builtInRegistryHolder(),(6 * 9),0);
        addItemValue(point_value,Items.DEAD_BUSH.builtInRegistryHolder(),4,0);


        addItemValue(point_value, CraftorioItemTagGen.CORAL_BLOCKS,4,0);
        addItemValue(point_value, CraftorioItemTagGen.CORAL,3,0);
        addItemValue(point_value, CraftorioItemTagGen.DEAD_CORAL_BLOCKS,2,0);
        addItemValue(point_value, CraftorioItemTagGen.DEAD_CORAL,1,0);

        addItemValue(point_value,Items.SPONGE.builtInRegistryHolder(),20,0);
        addItemValue(point_value,Items.WET_SPONGE.builtInRegistryHolder(),20,1);




        int melonslice = 5;
        addItemValue(point_value,Items.MELON_SLICE.builtInRegistryHolder(),melonslice,0);
        addItemValue(point_value,Items.MELON.builtInRegistryHolder(), (melonslice * 9),1);

        int torch =  multiValCal(coal  + sticks,1,4);
        int redstone_torch = redstone + sticks;
        addItemValue(point_value,Items.TORCH.builtInRegistryHolder(), torch,3);
        addItemValue(point_value,Items.SOUL_TORCH.builtInRegistryHolder(), torch + 2,3);
        addItemValue(point_value,Items.REDSTONE_TORCH.builtInRegistryHolder(),redstone_torch,3);

        int pumpkin = 10;
        addItemValue(point_value,Items.PUMPKIN.builtInRegistryHolder(),pumpkin,0);
        addItemValue(point_value,Items.CARVED_PUMPKIN.builtInRegistryHolder(),pumpkin,1);
        addItemValue(point_value,Items.JACK_O_LANTERN.builtInRegistryHolder(),pumpkin + torch,3);
        addItemValue(point_value,Items.PITCHER_POD.builtInRegistryHolder(),5,0);

        addItemValue(point_value,Items.HONEY_BOTTLE.builtInRegistryHolder(),12,0);
        addItemValue(point_value,Items.HONEY_BLOCK.builtInRegistryHolder(), (12 * 4),1);
        addItemValue(point_value, CraftorioItemTagGen.FROGLIGHT,35,0);

        addItemValue(point_value,Items.SCULK.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.SCULK_VEIN.builtInRegistryHolder(),3,0);
        addItemValue(point_value,Items.SCULK_CATALYST.builtInRegistryHolder(),5,0);
        addItemValue(point_value,Items.SCULK_SHRIEKER.builtInRegistryHolder(),7,0);
        addItemValue(point_value,Items.SCULK_SENSOR.builtInRegistryHolder(),8,0);
        addItemValue(point_value,Items.COBWEB.builtInRegistryHolder(),15,0);

        addItemValue(point_value,Items.LANTERN.builtInRegistryHolder(),(iron_nugget * 8) + torch,3);
        addItemValue(point_value,Items.SOUL_LANTERN.builtInRegistryHolder(),(iron_nugget * 8) + torch,3);

        addItemValue(point_value,Items.END_ROD.builtInRegistryHolder(),blaze_rod + purpur,2);
        addItemValue(point_value,Items.BLAZE_ROD.builtInRegistryHolder(),blaze_rod,0);
        addItemValue(point_value,Items.REDSTONE_LAMP.builtInRegistryHolder(),(redstone * 4) + (glowstone_dust * 4),1);
        addItemValue(point_value,Items.CRAFTING_TABLE.builtInRegistryHolder(),(planks * 4),2);
        addItemValue(point_value,Items.STONECUTTER.builtInRegistryHolder(),(stone * 3) + iron,2);
        int paper = multiValCal(sugar_cane,3,3) + 5;
        int leather = 6;
        int book = (paper * 3) + leather;
        addItemValue(point_value,Items.LEATHER.builtInRegistryHolder(), leather,0);
        addItemValue(point_value,Items.PAPER.builtInRegistryHolder(), paper,1);
        addItemValue(point_value,Items.BOOK.builtInRegistryHolder(), book,2);

        int flint = 5;
        addItemValue(point_value,Items.FLINT.builtInRegistryHolder(),flint,0);

        addItemValue(point_value,Items.CARTOGRAPHY_TABLE.builtInRegistryHolder(),(planks * 4 ) + (paper * 2),2);
        addItemValue(point_value,Items.FLETCHING_TABLE.builtInRegistryHolder(),(planks * 4) + (flint * 2),2);
        addItemValue(point_value,Items.SMITHING_TABLE.builtInRegistryHolder(),(planks * 4) + (iron * 2),2);
        addItemValue(point_value,Items.GRINDSTONE.builtInRegistryHolder(),(planks * 2) + 4 + stone,2);
        addItemValue(point_value,Items.LOOM.builtInRegistryHolder(),(planks * 2) + (string * 2),1);
        addItemValue(point_value,Items.FURNACE.builtInRegistryHolder(),(cobblestone * 8),1);
        addItemValue(point_value,Items.SMOKER.builtInRegistryHolder(),(cobblestone * 8) + (2 * 4),1);
        addItemValue(point_value,Items.BLAST_FURNACE.builtInRegistryHolder(),(cobblestone * 8) + ((stone + 3) * 3) + (iron * 5),3);
        addItemValue(point_value,Items.CAMPFIRE.builtInRegistryHolder(),(sticks * 3) + coal + (2 * 3),3);
        addItemValue(point_value,Items.SOUL_CAMPFIRE.builtInRegistryHolder(),(sticks * 3) + 3 + (2 * 3),3);
        addItemValue(point_value,Items.ANVIL.builtInRegistryHolder(),((iron * 9) * 3) + (iron * 5),1);
        addItemValue(point_value,Items.CHIPPED_ANVIL.builtInRegistryHolder(), round(((iron * 9) * 3) + (float) (iron * 5) / 2),1);
        addItemValue(point_value,Items.DAMAGED_ANVIL.builtInRegistryHolder(), round(((iron * 9) * 3) + (float) (iron * 5) / 3),1);
        addItemValue(point_value,Items.COMPOSTER.builtInRegistryHolder(), (planks * 5),2);
        addItemValue(point_value,Items.NOTE_BLOCK.builtInRegistryHolder(), (planks * 8) + redstone + 10,2);
        addItemValue(point_value,Items.JUKEBOX.builtInRegistryHolder(), (planks * 8) + diamond + 20,2);
        addItemValue(point_value,Items.ENCHANTING_TABLE.builtInRegistryHolder(), (diamond * 2) + (obsidian * 4) + book,3);


        addItemValue(point_value,Items.BLAZE_POWDER.builtInRegistryHolder(),  blaze_powder,1);
        addItemValue(point_value,Items.ENDER_PEARL.builtInRegistryHolder(), 11,0);
        addItemValue(point_value,Items.ENDER_EYE.builtInRegistryHolder(),  blaze_powder + 11,1);

        int ghast_tear = 21;
        addItemValue(point_value,Items.GHAST_TEAR.builtInRegistryHolder(), ghast_tear,0);
        addItemValue(point_value,Items.END_CRYSTAL.builtInRegistryHolder(), (glass * 7) + ghast_tear + blaze_powder,2);
        addItemValue(point_value,Items.BREWING_STAND.builtInRegistryHolder(), (cobblestone * 3) + blaze_rod,1);
        addItemValue(point_value,Items.CAULDRON.builtInRegistryHolder(), (iron * 7),1);
        addItemValue(point_value,Items.BELL.builtInRegistryHolder(), (emerald * 10),0);
        addItemValue(point_value,Items.NETHER_STAR.builtInRegistryHolder(), 400,0);
        addItemValue(point_value,Items.BEACON.builtInRegistryHolder(), 400 + (glass * 5) + (obsidian * 3),2);
        addItemValue(point_value,Items.NAUTILUS_SHELL.builtInRegistryHolder(), 96,0);
        addItemValue(point_value,Items.HEART_OF_THE_SEA.builtInRegistryHolder(), 216,0);
        addItemValue(point_value,Items.CONDUIT.builtInRegistryHolder(), (96 * 8) + 216,1);
        addItemValue(point_value,Items.LODESTONE.builtInRegistryHolder(), (chiseledAndPillared(stone) * 8) + netherite_ingot,3);
        addItemValue(point_value,Items.LADDER.builtInRegistryHolder(), multiValCal(sticks,7, 3),3);
        addItemValue(point_value,Items.SCAFFOLDING.builtInRegistryHolder(), multiValCal(sticks,6, 6) + string,3);
        addItemValue(point_value,Items.SUSPICIOUS_SAND.builtInRegistryHolder(), 10,0);
        addItemValue(point_value,Items.SUSPICIOUS_GRAVEL.builtInRegistryHolder(), 10,0);
        addItemValue(point_value,Items.LIGHTNING_ROD.builtInRegistryHolder(), (copper_ingot * 3),1);
        addItemValue(point_value,Items.FLOWER_POT.builtInRegistryHolder(), (brick * 3),1);
        addItemValue(point_value,Items.DECORATED_POT.builtInRegistryHolder(), brickblock,1);
        addItemValue(point_value,Items.ARMOR_STAND.builtInRegistryHolder(), (sticks * 6) + 4,3);
        addItemValue(point_value,Items.ITEM_FRAME.builtInRegistryHolder(), (sticks * 8) + leather,3);
        addItemValue(point_value,Items.GLOW_INK_SAC.builtInRegistryHolder(), 7,0);
        addItemValue(point_value,Items.GLOW_ITEM_FRAME.builtInRegistryHolder(), (sticks * 8) + leather + 7,4);
        addItemValue(point_value,Items.PAINTING.builtInRegistryHolder(), (sticks * 8) + (string * 4),3);

        addItemValue(point_value,Items.BOOKSHELF.builtInRegistryHolder(), (planks * 6) + (book * 3),3);
        addItemValue(point_value,Items.CHISELED_BOOKSHELF.builtInRegistryHolder(), (planks * 7),2);
        addItemValue(point_value,Items.LECTERN.builtInRegistryHolder(), (planks * 6) + (book * 3) + (planks * 4),2);
        addItemValue(point_value,ItemTags.SIGNS, multiValCal(1,6,3) + sticks,3);
        addItemValue(point_value,ItemTags.HANGING_SIGNS, multiValCal(2,6,6) + (chain * 2),2);

        int chest = (planks * 8);
        addItemValue(point_value, Items.CHEST.builtInRegistryHolder(), chest,2);
        addItemValue(point_value, Items.BARREL.builtInRegistryHolder(), (planks * 7),2);
        addItemValue(point_value, Items.ENDER_CHEST.builtInRegistryHolder(), (obsidian * 8) + multiValCal(blaze_rod,1,2) + blaze_powder + 11,2);
        addItemValue(point_value, Items.RESPAWN_ANCHOR.builtInRegistryHolder(), (obsidian * 6 + 5) + (glowstone_dust * 4 * 3),2);
        addItemValue(point_value, ItemTags.SKULLS, 100,0);
        addItemValue(point_value, Items.WITHER_SKELETON_SKULL.builtInRegistryHolder(), 200,0);
        addItemValue(point_value, Items.DRAGON_HEAD.builtInRegistryHolder(), 600,0);
        addItemValue(point_value, Items.DRAGON_EGG.builtInRegistryHolder(), 100000,0);
        addItemValue(point_value, Items.REPEATER.builtInRegistryHolder(), (stone * 3) + redstone + (redstone_torch * 2),4);
        addItemValue(point_value, Items.COMPARATOR.builtInRegistryHolder(), (stone * 3) + quartz + (redstone_torch * 3),4);
        addItemValue(point_value, Items.TARGET.builtInRegistryHolder(), (redstone * 4) + (wheat * 9),1);
        addItemValue(point_value, Items.LEVER.builtInRegistryHolder(), stone + sticks,3);
        addItemValue(point_value, Items.CALIBRATED_SCULK_SENSOR.builtInRegistryHolder(), 8 + (3 * amethyst),1);

        int tripwire_hook = multiValCal(iron + sticks + planks,1,2);
        addItemValue(point_value, Items.TRIPWIRE_HOOK.builtInRegistryHolder(), tripwire_hook,3);
        addItemValue(point_value, Items.DAYLIGHT_DETECTOR.builtInRegistryHolder(), (glass * 3) + (quartz * 3) + (planks * 2),3);
        addItemValue(point_value, Items.PISTON.builtInRegistryHolder(), (cobblestone * 4) + redstone + iron + (planks * 3),3);
        addItemValue(point_value, Items.STICKY_PISTON.builtInRegistryHolder(), (cobblestone * 4) + redstone + iron + (planks * 3) + slime_ball,4);
        addItemValue(point_value, Items.DISPENSER.builtInRegistryHolder(), (cobblestone * 7) + redstone + (string * 3) + (sticks * 3),3);
        addItemValue(point_value, Items.BOW.builtInRegistryHolder(), (string * 3) + (sticks * 3),3);
        addItemValue(point_value, Items.DROPPER.builtInRegistryHolder(), (cobblestone * 7) + redstone,1);
        addItemValue(point_value, Items.CRAFTER.builtInRegistryHolder(), (iron * 5) + (planks * 4) + (cobblestone * 7) + (redstone * 2),2);
        addItemValue(point_value, Items.HOPPER.builtInRegistryHolder(), (iron * 5) + chest,3);
        addItemValue(point_value, Items.TRAPPED_CHEST.builtInRegistryHolder(), (tripwire_hook) + (planks * 8),4);
        addItemValue(point_value, Items.OBSERVER.builtInRegistryHolder(), (cobblestone * 6) + (redstone * 2) + quartz,1);
        addItemValue(point_value, Items.RAIL.builtInRegistryHolder(), multiValCal(iron,6,16) + sticks,3);
        addItemValue(point_value, Items.POWERED_RAIL.builtInRegistryHolder(), multiValCal(gold,6,6) + sticks + redstone,3);
        addItemValue(point_value, Items.DETECTOR_RAIL.builtInRegistryHolder(), multiValCal(iron,6,6) + (stone * 2) + redstone,3);
        addItemValue(point_value, Items.ACTIVATOR_RAIL.builtInRegistryHolder(), multiValCal(iron,6,6) + (sticks * 2) + redstone_torch,4);

        int minecart = (iron * 5);
        addItemValue(point_value, Items.MINECART.builtInRegistryHolder(), minecart,1);
        addItemValue(point_value, Items.HOPPER_MINECART.builtInRegistryHolder(), minecart + (iron * 5) + chest,4);
        addItemValue(point_value, Items.CHEST_MINECART.builtInRegistryHolder(), minecart + chest,3);
        addItemValue(point_value, Items.FURNACE_MINECART.builtInRegistryHolder(), minecart + (cobblestone * 8),2);

        int gunpowder = 9;
        addItemValue(point_value, Items.GUNPOWDER.builtInRegistryHolder(), gunpowder,0);
        addItemValue(point_value, Items.TNT.builtInRegistryHolder(), (sand * 4) + (gunpowder * 5),1);
        addItemValue(point_value, Items.TNT_MINECART.builtInRegistryHolder(), minecart + (sand * 4) + (gunpowder * 5),2);
        addItemValue(point_value, ItemTags.BOATS, (planks * 5),3);
        addItemValue(point_value, ItemTags.CHEST_BOATS, (planks * 13),3);

        addItemValue(point_value, Items.WOODEN_SHOVEL.builtInRegistryHolder(),(sticks * 2) + planks,3);
        addItemValue(point_value, Items.WOODEN_SWORD.builtInRegistryHolder(),sticks + (planks * 2),3);
        addItemValue(point_value, Items.WOODEN_AXE.builtInRegistryHolder(),(sticks * 2) + (planks * 3),3);
        addItemValue(point_value, Items.WOODEN_HOE.builtInRegistryHolder(),(sticks * 2) + (planks * 2),3);
        addItemValue(point_value, Items.WOODEN_PICKAXE.builtInRegistryHolder(),(sticks * 2) + (planks * 3),3);

        addItemValue(point_value, Items.STONE_SHOVEL.builtInRegistryHolder(),(sticks * 2) + cobblestone,3);
        addItemValue(point_value, Items.STONE_SWORD.builtInRegistryHolder(),sticks + (cobblestone * 2),3);
        addItemValue(point_value, Items.STONE_AXE.builtInRegistryHolder(),(sticks * 2) + (cobblestone * 3),3);
        addItemValue(point_value, Items.STONE_HOE.builtInRegistryHolder(),(sticks * 2) + (cobblestone * 2),3);
        addItemValue(point_value, Items.STONE_PICKAXE.builtInRegistryHolder(),(sticks * 2) + (cobblestone * 3),3);

        addItemValue(point_value, Items.IRON_SHOVEL.builtInRegistryHolder(),(sticks * 2) + iron,3);
        addItemValue(point_value, Items.IRON_SWORD.builtInRegistryHolder(),sticks + (iron * 2),3);
        addItemValue(point_value, Items.IRON_AXE.builtInRegistryHolder(),(sticks * 2) + (iron * 3),3);
        addItemValue(point_value, Items.IRON_HOE.builtInRegistryHolder(),(sticks * 2) + (iron * 2),3);
        addItemValue(point_value, Items.IRON_PICKAXE.builtInRegistryHolder(),(sticks * 2) + (iron * 3),3);

        addItemValue(point_value, Items.GOLDEN_SHOVEL.builtInRegistryHolder(),(sticks * 2) + gold,3);
        addItemValue(point_value, Items.GOLDEN_SWORD.builtInRegistryHolder(),sticks + (gold * 2),3);
        addItemValue(point_value, Items.GOLDEN_AXE.builtInRegistryHolder(),(sticks * 2) + (gold * 3),3);
        addItemValue(point_value, Items.GOLDEN_HOE.builtInRegistryHolder(),(sticks * 2) + (gold * 2),3);
        addItemValue(point_value, Items.GOLDEN_PICKAXE.builtInRegistryHolder(),(sticks * 2) + (gold * 3),3);

        addItemValue(point_value, Items.DIAMOND_SHOVEL.builtInRegistryHolder(),(sticks * 2) + diamond,3);
        addItemValue(point_value, Items.DIAMOND_SWORD.builtInRegistryHolder(),sticks + (diamond * 2),3);
        addItemValue(point_value, Items.DIAMOND_AXE.builtInRegistryHolder(),(sticks * 2) + (diamond * 3),3);
        addItemValue(point_value, Items.DIAMOND_HOE.builtInRegistryHolder(),(sticks * 2) + (diamond * 2),3);
        addItemValue(point_value, Items.DIAMOND_PICKAXE.builtInRegistryHolder(),(sticks * 2) + (diamond * 3),3);

        int smithingTemplate = 270;
        addItemValue(point_value, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.builtInRegistryHolder(),smithingTemplate,0);
        addItemValue(point_value, Items.NETHERITE_SHOVEL.builtInRegistryHolder(),(sticks * 2) + netherite_ingot + smithingTemplate,4);
        addItemValue(point_value, Items.NETHERITE_SWORD.builtInRegistryHolder(),sticks + (netherite_ingot * 2) + smithingTemplate,4);
        addItemValue(point_value, Items.NETHERITE_AXE.builtInRegistryHolder(),(sticks * 2) + (netherite_ingot * 3) + smithingTemplate,4);
        addItemValue(point_value, Items.NETHERITE_HOE.builtInRegistryHolder(),(sticks * 2) + (netherite_ingot * 2) + smithingTemplate,4);
        addItemValue(point_value, Items.NETHERITE_PICKAXE.builtInRegistryHolder(),(sticks * 2) + (netherite_ingot * 3) + smithingTemplate,4);

        addItemValue(point_value, Items.COD.builtInRegistryHolder(),14,0);
        addItemValue(point_value, Items.SALMON.builtInRegistryHolder(),16,0);
        addItemValue(point_value, Items.TROPICAL_FISH.builtInRegistryHolder(),21,0);
        addItemValue(point_value, Items.PUFFERFISH.builtInRegistryHolder(),25,0);

        int bucket = (iron * 3);
        addItemValue(point_value, Items.BUCKET.builtInRegistryHolder(),bucket,1);
        addItemValue(point_value, Items.WATER_BUCKET.builtInRegistryHolder(),bucket,2);
        addItemValue(point_value, Items.COD_BUCKET.builtInRegistryHolder(),bucket + 7,2);
        addItemValue(point_value, Items.SALMON_BUCKET.builtInRegistryHolder(),bucket + 8,2);
        addItemValue(point_value, Items.TROPICAL_FISH_BUCKET.builtInRegistryHolder(),bucket + 12,2);
        addItemValue(point_value, Items.PUFFERFISH_BUCKET.builtInRegistryHolder(),bucket + 15,2);
        addItemValue(point_value, Items.AXOLOTL_BUCKET.builtInRegistryHolder(),bucket + 56,2);
        addItemValue(point_value, Items.TADPOLE_BUCKET.builtInRegistryHolder(),bucket + 28,2);
        addItemValue(point_value, Items.LAVA_BUCKET.builtInRegistryHolder(),bucket + 10,2);
        addItemValue(point_value, Items.POWDER_SNOW_BUCKET.builtInRegistryHolder(),bucket + 4,2);
        addItemValue(point_value, Items.MILK_BUCKET.builtInRegistryHolder(),bucket + 8,2);

        int fishing_rod = (sticks * 3) + (string * 2);
        addItemValue(point_value, Items.FISHING_ROD.builtInRegistryHolder(),fishing_rod,3);
        addItemValue(point_value, Items.FLINT_AND_STEEL.builtInRegistryHolder(),flint + iron,1);
        addItemValue(point_value, Items.FIRE_CHARGE.builtInRegistryHolder(),multiValCal(coal + blaze_powder + gunpowder,1,3),2);
        addItemValue(point_value, Items.SHEARS.builtInRegistryHolder(),(iron * 2),1);

        int feather = 5;
        addItemValue(point_value, Items.FEATHER.builtInRegistryHolder(),feather,1);
        addItemValue(point_value, Items.BRUSH.builtInRegistryHolder(),sticks + copper_ingot + feather,3);
        addItemValue(point_value, Items.NAME_TAG.builtInRegistryHolder(),80,0);
        addItemValue(point_value, Items.LEAD.builtInRegistryHolder(),(string * 5) + slime_ball,1);
        addItemValue(point_value, Items.COMPASS.builtInRegistryHolder(),(iron * 4) + redstone,1);

        int echo_shard = 96;
        addItemValue(point_value, Items.RECOVERY_COMPASS.builtInRegistryHolder(),(iron * 4) + redstone + (echo_shard * 8),1);
        addItemValue(point_value, Items.CLOCK.builtInRegistryHolder(),(gold * 4) + redstone,1);
        addItemValue(point_value, Items.SPYGLASS.builtInRegistryHolder(),(copper_ingot * 2) + amethyst,1);
        addItemValue(point_value, Items.MAP.builtInRegistryHolder(),(paper * 8) + (iron * 4) + redstone,2);
        addItemValue(point_value, Items.FILLED_MAP.builtInRegistryHolder(),(paper * 8) + (iron * 4) + redstone,2);

        int ink_sac = 7;
        addItemValue(point_value, Items.INK_SAC.builtInRegistryHolder(),ink_sac,0);
        addItemValue(point_value, Items.WRITABLE_BOOK.builtInRegistryHolder(),book + feather + ink_sac,3);
        addItemValue(point_value, Items.WRITTEN_BOOK.builtInRegistryHolder(),book + 2,3);

        int breeze_rod = 16;
        addItemValue(point_value, Items.BREEZE_ROD.builtInRegistryHolder(),breeze_rod,0);
        addItemValue(point_value, Items.WIND_CHARGE.builtInRegistryHolder(),multiValCal(breeze_rod,1,4),1);
        addItemValue(point_value, Items.ELYTRA.builtInRegistryHolder(),800,0);
        addItemValue(point_value, Items.FIREWORK_ROCKET.builtInRegistryHolder(),gunpowder + paper,2);
        addItemValue(point_value, Items.SADDLE.builtInRegistryHolder(),(leather * 5),0);
        addItemValue(point_value, Items.CARROT_ON_A_STICK.builtInRegistryHolder(),fishing_rod + carrot,4);
        addItemValue(point_value, Items.WARPED_FUNGUS_ON_A_STICK.builtInRegistryHolder(),fishing_rod + 2,4);
        addItemValue(point_value, Items.GOAT_HORN.builtInRegistryHolder(),85,0);

        addItemValue(point_value, Tags.Items.MUSIC_DISCS,100,0);
        addItemValue(point_value, Items.TRIDENT.builtInRegistryHolder(),120,0);
        addItemValue(point_value, Items.HEAVY_CORE.builtInRegistryHolder(),600,0);
        addItemValue(point_value, Items.MACE.builtInRegistryHolder(),600 + breeze_rod,1);
        addItemValue(point_value, Items.SHIELD.builtInRegistryHolder(),(planks * 6) + iron,2);

        addItemValue(point_value, Items.LEATHER_HELMET.builtInRegistryHolder(),(leather * 5),1);
        addItemValue(point_value, Items.LEATHER_CHESTPLATE.builtInRegistryHolder(),(leather * 8),1);
        addItemValue(point_value, Items.LEATHER_LEGGINGS.builtInRegistryHolder(),(leather * 7),1);
        addItemValue(point_value, Items.LEATHER_BOOTS.builtInRegistryHolder(),(leather * 4),1);

        addItemValue(point_value, Items.CHAINMAIL_HELMET.builtInRegistryHolder(),(chain * 5),2);
        addItemValue(point_value, Items.CHAINMAIL_CHESTPLATE.builtInRegistryHolder(),(chain * 8),2);
        addItemValue(point_value, Items.CHAINMAIL_LEGGINGS.builtInRegistryHolder(),(chain * 7),2);
        addItemValue(point_value, Items.CHAINMAIL_BOOTS.builtInRegistryHolder(),(chain * 4),2);

        addItemValue(point_value, Items.IRON_HELMET.builtInRegistryHolder(),(iron * 5),1);
        addItemValue(point_value, Items.IRON_CHESTPLATE.builtInRegistryHolder(),(iron * 8),1);
        addItemValue(point_value, Items.IRON_LEGGINGS.builtInRegistryHolder(),(iron * 7),1);
        addItemValue(point_value, Items.IRON_BOOTS.builtInRegistryHolder(),(iron * 4),1);

        addItemValue(point_value, Items.GOLDEN_HELMET.builtInRegistryHolder(),(gold * 5),1);
        addItemValue(point_value, Items.GOLDEN_CHESTPLATE.builtInRegistryHolder(),(gold * 8),1);
        addItemValue(point_value, Items.GOLDEN_LEGGINGS.builtInRegistryHolder(),(gold * 7),1);
        addItemValue(point_value, Items.GOLDEN_BOOTS.builtInRegistryHolder(),(gold * 4),1);

        addItemValue(point_value, Items.DIAMOND_HELMET.builtInRegistryHolder(),(diamond * 5),1);
        addItemValue(point_value, Items.DIAMOND_CHESTPLATE.builtInRegistryHolder(),(diamond * 8),1);
        addItemValue(point_value, Items.DIAMOND_LEGGINGS.builtInRegistryHolder(),(diamond * 7),1);
        addItemValue(point_value, Items.DIAMOND_BOOTS.builtInRegistryHolder(),(diamond * 4),1);

        addItemValue(point_value, Items.NETHERITE_HELMET.builtInRegistryHolder(),(netherite_ingot * 5),3);
        addItemValue(point_value, Items.NETHERITE_CHESTPLATE.builtInRegistryHolder(),(netherite_ingot * 8),3);
        addItemValue(point_value, Items.NETHERITE_LEGGINGS.builtInRegistryHolder(),(netherite_ingot * 7),3);
        addItemValue(point_value, Items.NETHERITE_BOOTS.builtInRegistryHolder(),(netherite_ingot * 4),3);

        int turtle_scute = 19;
        addItemValue(point_value, Items.TURTLE_SCUTE.builtInRegistryHolder(),turtle_scute,0);
        addItemValue(point_value, Items.TURTLE_HELMET.builtInRegistryHolder(),(turtle_scute * 5),1);
        addItemValue(point_value, Items.LEATHER_HORSE_ARMOR.builtInRegistryHolder(),(leather * 7),1);
        addItemValue(point_value, Items.IRON_HORSE_ARMOR.builtInRegistryHolder(),(iron * 7),0);
        addItemValue(point_value, Items.GOLDEN_HORSE_ARMOR.builtInRegistryHolder(),(gold * 7),0);
        addItemValue(point_value, Items.DIAMOND_HORSE_ARMOR.builtInRegistryHolder(),(diamond * 7),0);

        addItemValue(point_value, Items.ARMADILLO_SCUTE.builtInRegistryHolder(),20,0);
        addItemValue(point_value, Items.WOLF_ARMOR.builtInRegistryHolder(),(20 * 7),1);

        addItemValue(point_value, Items.TOTEM_OF_UNDYING.builtInRegistryHolder(),130,0);

        int egg = 12;
        addItemValue(point_value, Items.EGG.builtInRegistryHolder(),egg,0);
        addItemValue(point_value, Items.CROSSBOW.builtInRegistryHolder(),(sticks * 3) + (string * 2) + tripwire_hook + iron,3);
        int arrow = multiValCal(flint + sticks + feather,1,4);
        addItemValue(point_value, Items.ARROW.builtInRegistryHolder(),arrow,3);
        addItemValue(point_value, Items.SPECTRAL_ARROW.builtInRegistryHolder(),multiValCal (glowstone_dust,4,2) + arrow,4);

        int glass_bottle = multiValCal(glass,3,3);
        int dragonmint = 56;
        addItemValue(point_value,Items.GLASS_BOTTLE.builtInRegistryHolder(), glass_bottle,2);
        addItemValue(point_value,Items.DRAGON_BREATH.builtInRegistryHolder(), dragonmint,1);
        int potion = glass_bottle + blaze_powder + netherwart;
        addItemValue(point_value,Items.POTION.builtInRegistryHolder(), potion,4);
        addItemValue(point_value,Items.SPLASH_POTION.builtInRegistryHolder(),potion + gunpowder,5);
        addItemValue(point_value,Items.LINGERING_POTION.builtInRegistryHolder(),potion + gunpowder + dragonmint,6);

        addItemValue(point_value,Items.TIPPED_ARROW.builtInRegistryHolder(),multiValCal(arrow,8,8) + potion + gunpowder + dragonmint,7);

        addItemValue(point_value,Items.APPLE.builtInRegistryHolder(),10,0);
        addItemValue(point_value,Items.GOLDEN_APPLE.builtInRegistryHolder(),10 + (gold * 8),1);
        addItemValue(point_value,Items.ENCHANTED_GOLDEN_APPLE.builtInRegistryHolder(),400,0);
        addItemValue(point_value,Items.GOLDEN_CARROT.builtInRegistryHolder(),carrot + (gold_nugget * 8),1);
        addItemValue(point_value,Items.BAKED_POTATO.builtInRegistryHolder(),14,1);
        addItemValue(point_value,Items.POISONOUS_POTATO.builtInRegistryHolder(),3,0);

        addItemValue(point_value,Items.BEEF.builtInRegistryHolder(),12,0);
        addItemValue(point_value,Items.COOKED_BEEF.builtInRegistryHolder(),14,1);
        addItemValue(point_value,Items.PORKCHOP.builtInRegistryHolder(),11,0);
        addItemValue(point_value,Items.COOKED_PORKCHOP.builtInRegistryHolder(),13,1);
        addItemValue(point_value,Items.MUTTON.builtInRegistryHolder(),13,0);
        addItemValue(point_value,Items.COOKED_MUTTON.builtInRegistryHolder(),15,1);
        addItemValue(point_value,Items.CHICKEN.builtInRegistryHolder(),9,0);
        addItemValue(point_value,Items.COOKED_CHICKEN.builtInRegistryHolder(),11,1);
        addItemValue(point_value,Items.RABBIT.builtInRegistryHolder(),14,0);
        addItemValue(point_value,Items.COOKED_RABBIT.builtInRegistryHolder(),16,1);
        addItemValue(point_value,Items.COOKED_COD.builtInRegistryHolder(),16,1);
        addItemValue(point_value,Items.COOKED_SALMON.builtInRegistryHolder(),18,1);

        addItemValue(point_value,Items.BREAD.builtInRegistryHolder(),(wheat * 3),1);
        addItemValue(point_value,Items.COOKIE.builtInRegistryHolder(),multiValCal((wheat * 2) + 11,2,8),1);

        int sugar = sugar_cane + 2;
        addItemValue(point_value,Items.SUGAR.builtInRegistryHolder(),sugar,1);
        addItemValue(point_value,Items.CAKE.builtInRegistryHolder(),(bucket * 3) + (wheat * 3) + (sugar * 2) + egg,2);
        addItemValue(point_value,Items.PUMPKIN_PIE.builtInRegistryHolder(),pumpkin + sugar + egg,2);

        addItemValue(point_value,Items.ROTTEN_FLESH.builtInRegistryHolder(),11,0);
        int spidereye = 14;
        addItemValue(point_value,Items.SPIDER_EYE.builtInRegistryHolder(),spidereye,0);

        int bowl = planks * 3;
        addItemValue(point_value,Items.BOWL.builtInRegistryHolder(),bowl,1);
        addItemValue(point_value,Items.MUSHROOM_STEW.builtInRegistryHolder(),bowl + (mushrooms * 2),2);
        addItemValue(point_value,Items.BEETROOT.builtInRegistryHolder(),bowl + (beetroot * 6),2);
        addItemValue(point_value,Items.RABBIT_STEW.builtInRegistryHolder(),bowl + carrot + 16 + mushrooms + 14,2);
        addItemValue(point_value,Items.SUSPICIOUS_STEW.builtInRegistryHolder(),bowl + (mushrooms * 2) + 2,2);

        addItemValue(point_value,Items.OMINOUS_BOTTLE.builtInRegistryHolder(),70,0);

        addItemValue(point_value,Items.RABBIT_HIDE.builtInRegistryHolder(),leather / 4,0);
        addItemValue(point_value,Items.DISC_FRAGMENT_5.builtInRegistryHolder(),10,0);
        addItemValue(point_value,Items.FIREWORK_STAR.builtInRegistryHolder(),gunpowder + 2,1);
        addItemValue(point_value,Items.FERMENTED_SPIDER_EYE.builtInRegistryHolder(),spidereye + sugar + mushrooms,2);
        addItemValue(point_value,Items.RABBIT_FOOT.builtInRegistryHolder(),136,0);
        addItemValue(point_value,Items.GLISTERING_MELON_SLICE.builtInRegistryHolder(),(gold_nugget * 8) + melonslice,1);
        addItemValue(point_value,Items.PHANTOM_MEMBRANE.builtInRegistryHolder(),14,0);

        addItemValue(point_value,Tags.Items.LOOM_PATTERNS,80,1);
        addItemValue(point_value,ItemTags.DECORATED_POT_SHERDS,40,0);
        addItemValue(point_value,ItemTags.TRIM_TEMPLATES,80,0);

        addItemValue(point_value,Items.EXPERIENCE_BOTTLE.builtInRegistryHolder(),110,0);
        addItemValue(point_value,Items.TRIAL_KEY.builtInRegistryHolder(),30,0);
        addItemValue(point_value,Items.OMINOUS_TRIAL_KEY.builtInRegistryHolder(),120,0);

        addItemValue(point_value,Items.ENCHANTED_BOOK.builtInRegistryHolder(),60,1);


    }


    //(Work multiplier refers to how many hops it takes in terms of crafting to get to the result item)

    //Items
    private void addItemValue(Builder<String, Item> pointValue, TagKey<Item> item, int value,int workMultiplier) {
        int multiplier = workMultiplier > 1 ? workMultiplier - 1 : 0;
        pointValue.add(item, String.valueOf((int) (value * (1 + (multiplier * 0.10)))),false);
    }

    public void addItemValue(Builder<String, Item> pointValue, Holder<Item> object, int value,int workMultiplier){
        int multiplier = workMultiplier > 1 ? workMultiplier - 1 : 0;
        pointValue.add(object, String.valueOf((int) (value * (1 + (multiplier * 0.10)))),false);
    }

    //Mob Effects
    private void addEffectValue(Builder<String, MobEffect> pointValue, Holder<MobEffect> mobEffect, int value) {
        pointValue.add(mobEffect, String.valueOf(value),false);
    }

    private void addAdvancementValue(Builder<String, Advancement> pointValue, String resourceLocation, int value) {
        ResourceLocation advancementLocation = ResourceLocation.withDefaultNamespace(resourceLocation);
        pointValue.add(advancementLocation, String.valueOf(value),false);
    }

    //Enchantment
    private void addEnchantValue(Builder<String, Enchantment> pointValue, ResourceKey<Enchantment> mobEffect, int value) {
        pointValue.add(mobEffect, String.valueOf(value),false);
    }

    public int multiValCal(int baseValue, int itemsNeeded, int outputNo){
        return round((float) (baseValue * itemsNeeded) / outputNo);
    }


    public int crackedBlock(int baseValue){
        return baseValue - 2;
    }

    public int chiseledAndPillared(int baseValue){
        return baseValue * 2;
    }


}
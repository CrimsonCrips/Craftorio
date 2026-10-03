package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.registries.contract.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;
import static org.crimsoncrips.craftorio.CraftorioMisc.toItem;

public class CraftorioContractBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioContract> context) {
        addItemContracts(context);
        addBuildContracts(context);
    }

    private static void addItemContracts(BootstrapContext<CraftorioContract> context) {
        context.register(
                key("cake_delivery"), new CraftorioContract(
                        List.of(
                                newContractItem(1, Items.CAKE)
                        ),"cake_delivery", 120, BigInteger.valueOf(300),
                        List.of(
                                newContractReward(1, Items.GOLD_INGOT)
                        ),
                        Optional.empty(),
                        10,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("1e4"),
                        Optional.empty()
                )
        );

        context.register(
                key("animal_feed"), new CraftorioContract(
                        List.of(
                                newContractItem(2, Items.HAY_BLOCK),
                                newContractItem(12, Items.POTATO),
                                newContractItem(6, Items.CARROT)
                        ),"animal_feed", 300, BigInteger.valueOf(350),
                        List.of(
                                newContractReward(1, Items.CAKE)
                        ),
                        Optional.empty(),
                        10,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("2e4"),
                        Optional.empty()
                )
        );

        context.register(
                key("care_package"), new CraftorioContract(
                        List.of(
                                newContractItem(1, Items.IRON_HELMET),
                                newContractItem(1, Items.IRON_CHESTPLATE),
                                newContractItem(1, Items.IRON_LEGGINGS),
                                newContractItem(1, Items.IRON_BOOTS),
                                newContractItem(1, Items.IRON_SWORD),
                                newContractItem(16, Items.BREAD)
                        ),"care_package", 500, BigInteger.valueOf(1200),
                        List.of(),
                        Optional.empty(),
                        10,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("2e5"),
                        Optional.empty()
                )
        );

        context.register(
                key("brewing_materials"), new CraftorioContract(
                        List.of(
                                newContractItem(10, Items.BLAZE_ROD),
                                newContractItem(5, Items.BREWING_STAND),
                                newContractItem(64, Items.GLASS_BOTTLE),
                                newContractItem(12, Items.NETHER_WART)
                        ),"brewing_materials", 1200, BigInteger.valueOf(700),
                        List.of(
                                newContractReward(5, Items.EXPERIENCE_BOTTLE)
                        ),
                        Optional.empty(),
                        10,
                        scientificToInt("1e3"),
                        BigInteger.ZERO,
                        scientificToInt("2e5"),
                        Optional.empty()
                )
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "gold_throne_construction")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(28, toItem("minecraft:gold_block")),
                                new CraftorioContractItem(10, toItem("minecraft:candle")),
                                new CraftorioContractItem(1, toItem("minecraft:red_carpet"))
                        ),
                        "gold_throne_construction", 3600, scientificToInt("12000"),
                        List.of(
                                new CraftorioContractItemReward(1, toItem("minecraft:diamond_block"))
                        ),
                        Optional.of(ResourceLocation.parse("craftorio:shop/kingdom_tariff")),
                        10,
                        scientificToInt("13000"), scientificToInt("5000"), scientificToInt("5000000"),
                        Optional.empty(),
                        Optional.empty()
                )
        );

        context.register(
                key("dyeabolical"), new CraftorioContract(
                        List.of(
                                newContractItem(100, Items.WHITE_DYE),
                                newContractItem(100, Items.BLACK_DYE),
                                newContractItem(100, Items.BLUE_DYE),
                                newContractItem(100, Items.BROWN_DYE),
                                newContractItem(100, Items.CYAN_DYE),
                                newContractItem(100, Items.GRAY_DYE),
                                newContractItem(100, Items.GREEN_DYE),
                                newContractItem(100, Items.LIGHT_BLUE_DYE),
                                newContractItem(100, Items.LIGHT_GRAY_DYE),
                                newContractItem(100, Items.LIME_DYE),
                                newContractItem(100, Items.MAGENTA_DYE),
                                newContractItem(100, Items.ORANGE_DYE),
                                newContractItem(100, Items.PINK_DYE),
                                newContractItem(100, Items.PURPLE_DYE),
                                newContractItem(100, Items.RED_DYE),
                                newContractItem(100, Items.YELLOW_DYE)
                        ),"dyeabolical", 7200, BigInteger.valueOf(7000),
                        List.of(
                                newContractReward(20, Items.EXPERIENCE_BOTTLE)
                        ),
                        Optional.empty(),
                        10,
                        scientificToInt("1e4"),
                        BigInteger.valueOf(3500),
                        scientificToInt("5e6"),
                        Optional.empty()
                )
        );

        context.register(
                key("archery_resupply"), new CraftorioContract(
                        List.of(
                                newContractItem(6, Items.BOW),
                                newContractItem(5, Items.CROSSBOW),
                                newContractItem(384, Items.ARROW),
                                newContractItem(192, Items.SPECTRAL_ARROW)
                        ),"archery_resupply", 3600, BigInteger.valueOf(7500),
                        List.of(),
                        Optional.empty(),
                        10,
                        scientificToInt("2e4"),
                        BigInteger.valueOf(5000),
                        scientificToInt("9e7"),
                        Optional.empty()
                )
        );

        context.register(
                key("terraforming"), new CraftorioContract(
                        List.of(
                                newContractItem(256, Items.TNT),
                                newContractItem(4, Items.DIAMOND_PICKAXE)
                        ),"terraforming", 3600, BigInteger.valueOf(20000),
                        List.of(),
                        Optional.empty(),
                        10,
                        scientificToInt("1e4"),
                        BigInteger.valueOf(10000),
                        scientificToInt("2e8"),
                        Optional.empty()
                )
        );

        context.register(
                key("kingdoms_feast"), new CraftorioContract(
                        List.of(
                                newContractItem(4, Items.GOLDEN_APPLE),
                                newContractItem(8, Items.MILK_BUCKET),
                                newContractItem(16, Items.HONEY_BOTTLE),
                                newContractItem(16, Items.COOKED_PORKCHOP),
                                newContractItem(64, Items.COOKED_BEEF),
                                newContractItem(16, Items.COOKED_CHICKEN),
                                newContractItem(32, Items.COOKED_MUTTON),
                                newContractItem(8, Items.PUMPKIN_PIE),
                                newContractItem(9, Items.CAKE)
                        ),"kingdoms_feast", 5400, BigInteger.valueOf(7541),
                        List.of(),
                        Optional.of(Craftorio.prefix("shop/kingdom_tariff")),
                        10,
                        scientificToInt("3e4"),
                        scientificToInt("1e4"),
                        scientificToInt("1e6"),
                        Optional.empty()
                )
        );

        context.register(
                key("inquisitors_burning"), new CraftorioContract(
                        List.of(
                                newContractItem(64, Items.PAPER),
                                newContractItem(64, Items.INK_SAC),
                                newContractItem(64, Items.FEATHER),
                                newContractItem(32, Items.DARK_OAK_PLANKS),
                                newContractItem(10, Items.FIRE_CHARGE),
                                newContractItem(32, Items.COOKED_CHICKEN)
                        ),"inquisitors_burning", 7200, BigInteger.valueOf(2000),
                        List.of(
                                newContractReward(1, CraftorioItems.MYSTERY_EFFECT_RUNE.get(), 2)
                        ),
                        Optional.of(Craftorio.prefix("general/inquisitors_wrath")),
                        10,
                        scientificToInt("1e5"),
                        scientificToInt("1e4"),
                        scientificToInt("1e9"),
                        Optional.empty()
                )
        );

        context.register(
                key("copernicium"), new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(1000, CraftorioItemTagGen.COPPER)
                        ),"copernicium", 6000, BigInteger.valueOf(135000),
                        List.of(
                                newContractReward(2, CraftorioItems.MYSTERY_EFFECT_RUNE.get(), 1),
                                newContractReward(1, CraftorioItems.EFFECT_RUNE.get(), 2)
                        ),
                        Optional.of(Craftorio.prefix("tag/copper_deficiency")),
                        10,
                        scientificToInt("3e5"),
                        scientificToInt("7e4"),
                        scientificToInt("1e15"),
                        Optional.empty()
                )
        );

        context.register(
                key("trophy_headed"), new CraftorioContract(
                        List.of(
                                newContractItem(1, Items.CREEPER_HEAD),
                                newContractItem(1, Items.ZOMBIE_HEAD),
                                newContractItem(1, Items.SKELETON_SKULL),
                                newContractItem(1, Items.WITHER_SKELETON_SKULL),
                                newContractItem(1, Items.DRAGON_HEAD),
                                newContractItem(1, Items.PIGLIN_HEAD)
                        ),"trophy_headed", 1200, BigInteger.valueOf(1200),
                        List.of(
                                newContractReward(1, CraftorioItems.MYSTERY_EFFECT_RUNE.get(), 3)
                        ),
                        Optional.empty(),
                        10,
                        scientificToInt("1e5"),
                        scientificToInt("4e4"),
                        scientificToInt("1e50"),
                        Optional.empty()
                )
        );

        context.register(
                key("multiversal_collection"), new CraftorioContract(
                        allObtainableVanillaItems(),"multiversal_collection", 36000, scientificToInt("1e18"),
                        List.of(),
                        Optional.of(Craftorio.prefix("general/trazyns_curse")),
                        10,
                        scientificToInt("1e9"),
                        scientificToInt("5e7"),
                        scientificToInt("1e309"),
                        Optional.empty(),
                        Optional.of(ResourceLocation.parse("craftorio:textures/gui/contract_textures/custom/trazyn.png")),
                        new ContractTextColors(Optional.of(0x00AA00), Optional.empty(), Optional.empty(), Optional.empty())
                )
        );


        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "monster_annihilator")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(64, toItem("minecraft:gunpowder")),
                                new CraftorioContractItem(64, toItem("minecraft:emerald")),
                                new CraftorioContractItem(64, toItem("minecraft:slime_ball")),
                                new CraftorioContractItem(64, toItem("minecraft:prismarine_shard")),
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(64, toItem("minecraft:ghast_tear")),
                                new CraftorioContractItem(64, toItem("minecraft:rotten_flesh")),
                                new CraftorioContractItem(64, toItem("minecraft:magma_cream")),
                                new CraftorioContractItem(1, toItem("minecraft:saddle")),
                                new CraftorioContractItem(1, toItem("minecraft:saddle")),
                                new CraftorioContractItem(1, toItem("minecraft:saddle")),
                                new CraftorioContractItem(1, toItem("minecraft:saddle")),
                                new CraftorioContractItem(1, toItem("minecraft:saddle")),
                                new CraftorioContractItem(1, toItem("minecraft:saddle")),
                                new CraftorioContractItem(64, toItem("minecraft:breeze_rod")),
                                new CraftorioContractItem(16, toItem("minecraft:ender_pearl")),
                                new CraftorioContractItem(64, toItem("minecraft:shulker_shell")),
                                new CraftorioContractItem(64, toItem("minecraft:blaze_rod")),
                                new CraftorioContractItem(64, toItem("minecraft:phantom_membrane")),
                                new CraftorioContractItem(16, toItem("minecraft:sculk_catalyst")),
                                new CraftorioContractItem(16, toItem("minecraft:wither_skeleton_skull")),
                                new CraftorioContractItem(64, toItem("minecraft:bone"))
                        ),
                        "monster_annihilator", 16000, scientificToInt("5e8"),
                        List.of(
                                new CraftorioContractItemReward(1, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:mystery_effect_rune")), 7)
                        ),
                        Optional.empty(),
                        10,
                        scientificToInt("1e8"), scientificToInt("5e7"), scientificToInt("3e20"),
                        Optional.empty()
                )
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "executed_escapee")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(1, toItem("minecraft:totem_of_undying")),
                                new CraftorioContractItem(1, toItem("minecraft:diamond_pickaxe"))
                        ),
                        "executed_escapee", 600, scientificToInt("1200"),
                        List.of(),
                        Optional.empty(),
                        10,
                        scientificToInt("1e3"), scientificToInt("0"), scientificToInt("1e6"),
                        Optional.empty()
                )
        );


        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "wasted_reimains")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe")),
                                new CraftorioContractItem(1, toItem("minecraft:netherite_hoe"))
                        ),
                        "wasted_reimains", 36000, scientificToInt("800000"),
                        List.of(
                                // no reward items were placed in the creator's slots
                        ),
                        Optional.of(ResourceLocation.parse("craftorio:reiminder")),
                        7,
                        scientificToInt("2700000"), scientificToInt("100000"), scientificToInt("1000000000"),
                        Optional.empty(),
                        Optional.of(ResourceLocation.parse("craftorio:textures/gui/contract_textures/custom/reim.png")),
                        new ContractTextColors(Optional.of(0x55FFFF), Optional.empty(), Optional.empty(), Optional.empty())
                )
        );
    }

    private static void addBuildContracts(BootstrapContext<CraftorioContract> context) {
        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "chicken_coop")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "chicken_coop", 900, scientificToInt("384"),
                        List.of(
                                new CraftorioContractItemReward(8, toItem("minecraft:egg")),
                                new CraftorioContractItemReward(32, toItem("minecraft:wheat_seeds"))
                        ),
                        Optional.empty(),
                        10,
                        scientificToInt("5000"), scientificToInt("1000"), scientificToInt("400000"),
                        Optional.empty(),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("craftorio:contracts/chicken_coop")))
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "matterial_calculation")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "matterial_calculation", 43800, scientificToInt("7e21"),
                        List.of(
                                new CraftorioContractItemReward(5, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:effect_rune")), 2),
                                new CraftorioContractItemReward(6, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:effect_rune")), 2),
                                new CraftorioContractItemReward(3, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:mystery_effect_rune")), 2),
                                new CraftorioContractItemReward(4, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:mystery_effect_rune")), 2)

                        ),
                        Optional.empty(),
                        3,
                        scientificToInt("5000000000"), scientificToInt("1000000000"), scientificToInt("1e309"),
                        Optional.empty(),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("craftorio:contracts/matt_calculator")))
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "mumbo_10_door")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "mumbo_10_door", 7800, scientificToInt("100000"),
                        List.of(
                                new CraftorioContractItemReward(64, toItem("minecraft:redstone_block"))
                        ),
                        Optional.empty(),
                        11,
                        scientificToInt("500000"), scientificToInt("200000"), scientificToInt("1e10"),
                        Optional.empty(),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("craftorio:contracts/mumbo_door")))
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "create_steam_engine")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "create_steam_engine", 1800, scientificToInt("76000"),
                        List.of(
                                new CraftorioContractItemReward(4, toItem("create:blaze_cake")),
                                new CraftorioContractItemReward(16, toItem("create:brass_ingot")),
                                new CraftorioContractItemReward(4, toItem("create:andesite_alloy_block"))
                        ),
                        Optional.empty(),
                        7,
                        scientificToInt("120000"), scientificToInt("40000"), scientificToInt("1000000000000"),
                        Optional.of("create"),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("craftorio:contracts/simple_steam_engine")))
        );


        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "witchy_business")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "witchy_business", 600, scientificToInt("4500"),
                        List.of(
                                // no reward items were placed in the creator's slots
                        ),
                        Optional.of(ResourceLocation.parse("craftorio:witch_curse")),
                        9,
                        scientificToInt("4500"), scientificToInt("1000"), scientificToInt("20000"),
                        Optional.empty(),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("craftorio:contracts/witch_hut")))
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "villager_house")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "villager_house", 1260, scientificToInt("6000"),
                        List.of(
                                // no reward items were placed in the creator's slots
                        ),
                        Optional.empty(),
                        10,
                        scientificToInt("500"), scientificToInt("0"), scientificToInt("100000"),
                        Optional.empty(),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("minecraft:village/plains/houses/plains_small_house_1")))
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "end_ship")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "end_ship", 10800, scientificToInt("250000"),
                        List.of(
                                // no reward items were placed in the creator's slots
                        ),
                        Optional.empty(),
                        7,
                        scientificToInt("100000"), scientificToInt("10000"), scientificToInt("100000000"),
                        Optional.empty(),
                        Optional.empty()
                ).withGoal(ContractGoal.building(ResourceLocation.parse("minecraft:end_city/ship")))
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("craftorio", "nether_beacon")),
                new CraftorioContract(
                        List.of(
                                // no bounty items were placed in the creator's slots
                        ),
                        "nether_beacon", 360000, scientificToInt("7e80"),
                        List.of(
                                new CraftorioContractItemReward(9, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:effect_rune")), 6),
                                new CraftorioContractItemReward(5, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:effect_rune")), 6),
                                new CraftorioContractItemReward(4, toItem("minecraft:bedrock")),
                                new CraftorioContractItemReward(9, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:mystery_effect_rune")), 6),
                                new CraftorioContractItemReward(6, BuiltInRegistries.ITEM.get(ResourceLocation.parse("craftorio:mystery_effect_rune")), 6)
                        ),
                        Optional.empty(),
                        1,
                        scientificToInt("27e60"), scientificToInt("1e50"), scientificToInt("1e309"),
                        Optional.empty(),
                        Optional.empty(),
                        new ContractTextColors(Optional.of(0xFFFF55), Optional.empty(), Optional.empty(), Optional.empty())
                ).withGoal(ContractGoal.building(ResourceLocation.parse("craftorio:contracts/netherite_beacon")))
        );
    }

    private static final Set<Item> UNOBTAINABLE_VANILLA_ITEMS = Set.of(
            Items.BARRIER, Items.STRUCTURE_BLOCK, Items.STRUCTURE_VOID, Items.JIGSAW,
            Items.COMMAND_BLOCK, Items.CHAIN_COMMAND_BLOCK, Items.REPEATING_COMMAND_BLOCK,
            Items.COMMAND_BLOCK_MINECART, Items.DEBUG_STICK, Items.LIGHT, Items.SPAWNER,
            Items.TRIAL_SPAWNER, Items.VAULT, Items.KNOWLEDGE_BOOK, Items.PETRIFIED_OAK_SLAB,
            Items.BEDROCK, Items.END_PORTAL_FRAME, Items.REINFORCED_DEEPSLATE
    );

    private static List<CraftorioContractItem> allObtainableVanillaItems() {
        List<CraftorioContractItem> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || UNOBTAINABLE_VANILLA_ITEMS.contains(item)) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals("minecraft") || id.getPath().endsWith("_spawn_egg")) continue;

            items.add(newContractItem(1, item));
        }
        return items;
    }

    private static ResourceKey<CraftorioContract> key(String path) {
        return ResourceKey.create(CraftorioContract.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }

    private static CraftorioContractItem newContractItem(int amountRequired, Item item){
        return new CraftorioContractItem(amountRequired,item);
    }

    private static CraftorioContractItemReward newContractReward(int amountGiven, Item item){
        return new CraftorioContractItemReward(amountGiven,item);
    }

    private static CraftorioContractItemReward newContractReward(int amountGiven, Item item, int randomEffectCount){
        return new CraftorioContractItemReward(amountGiven,item,randomEffectCount);
    }
}
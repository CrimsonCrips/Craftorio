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
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItem;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItemReward;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractTexture;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;
import static org.crimsoncrips.craftorio.CraftorioMisc.toItem;

public class CraftorioContractBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioContract> context) {

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
                key("gold_throne_construction"), new CraftorioContract(
                        List.of(
                                newContractItem(28, Items.GOLD_BLOCK),
                                newContractItem(10, Items.CANDLE),
                                newContractItem(1, Items.RED_CARPET)
                        ),"gold_throne_construction", 3600, BigInteger.valueOf(12000),
                        List.of(
                                newContractReward(1, Items.DIAMOND_BLOCK)
                        ),
                        Optional.of(Craftorio.prefix("shop/kingdom_tariff")),
                        10,
                        scientificToInt("5e3"),
                        BigInteger.valueOf(13000),
                        scientificToInt("5e6"),
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
                key("chicken_coop"), new CraftorioContract(
                        List.of(
                                newContractItem(16, Items.EGG),
                                newContractItem(32, Items.OAK_LOG),
                                newContractItem(64, Items.WHEAT_SEEDS)
                        ),"chicken_coop", 900, BigInteger.valueOf(384),
                        List.of(),
                        Optional.empty(),
                        10,
                        scientificToInt("5e3"),
                        scientificToInt("1e3"),
                        scientificToInt("4e5"),
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
                        Optional.of(CraftorioContractTexture.TRAZYN)
                )
        );

        context.register(
                key("hephaestus_vault"), new CraftorioContract(
                        List.of(
                                newContractItem(211179, Items.ANDESITE),
                                newContractItem(87, Items.ANVIL),
                                newContractItem(49, Items.BARREL),
                                newContractItem(4123, Items.BEACON),
                                newContractItem(40, Items.BIRCH_SIGN),
                                newContractItem(6, Items.BLACK_BED),
                                newContractItem(626, Items.BLACK_STAINED_GLASS),
                                newContractItem(238178, Items.BLACKSTONE),
                                newContractItem(31, Items.BLACKSTONE_WALL),
                                newContractItem(235, Items.CHAIN),
                                newContractItem(139, Items.CHEST),
                                newContractItem(116, Items.CHISELED_POLISHED_BLACKSTONE),
                                newContractItem(2110, Items.CLAY),
                                newContractItem(81563, Items.COAL_BLOCK),
                                newContractItem(865, Items.COARSE_DIRT),
                                newContractItem(162993, Items.COBBLESTONE),
                                newContractItem(80, Items.COBBLESTONE_STAIRS),
                                newContractItem(208, Items.COMPARATOR),
                                newContractItem(9, Items.COMPOSTER),
                                newContractItem(2277382, Items.CRYING_OBSIDIAN),
                                newContractItem(9, Items.CUT_SANDSTONE),
                                newContractItem(262777, Items.CYAN_TERRACOTTA),
                                newContractItem(16, Items.DARK_OAK_FENCE),
                                newContractItem(40, Items.DARK_OAK_LOG),
                                newContractItem(15, Items.DARK_OAK_PLANKS),
                                newContractItem(60, Items.DARK_OAK_SLAB),
                                newContractItem(40, Items.DARK_OAK_STAIRS),
                                newContractItem(7, Items.DARK_OAK_TRAPDOOR),
                                newContractItem(30800, Items.DARK_PRISMARINE),
                                newContractItem(800, Items.DARK_PRISMARINE_STAIRS),
                                newContractItem(44, Items.DAYLIGHT_DETECTOR),
                                newContractItem(16, Items.DEAD_FIRE_CORAL_BLOCK),
                                newContractItem(29, Items.DEAD_TUBE_CORAL_BLOCK),
                                newContractItem(1, Items.DIAMOND_BLOCK),
                                newContractItem(50420, Items.DIRT),
                                newContractItem(47, Items.DISPENSER),
                                newContractItem(169, Items.DROPPER),
                                newContractItem(2664, Items.EMERALD_BLOCK),
                                newContractItem(31, Items.ENCHANTING_TABLE),
                                newContractItem(1, Items.END_STONE_BRICK_SLAB),
                                newContractItem(4, Items.END_STONE_BRICK_STAIRS),
                                newContractItem(167, Items.ENDER_CHEST),
                                newContractItem(44, Items.GLASS),
                                newContractItem(11, Items.GLOWSTONE),
                                newContractItem(2, Items.GOLD_BLOCK),
                                newContractItem(2642, Items.GRASS_BLOCK),
                                newContractItem(24857, Items.GRAVEL),
                                newContractItem(2056, Items.GREEN_STAINED_GLASS),
                                newContractItem(2, Items.HAY_BLOCK),
                                newContractItem(1, Items.HEAVY_WEIGHTED_PRESSURE_PLATE),
                                newContractItem(6, Items.HONEY_BLOCK),
                                newContractItem(294, Items.HOPPER),
                                newContractItem(186, Items.IRON_BARS),
                                newContractItem(78, Items.IRON_BLOCK),
                                newContractItem(4463, Items.IRON_TRAPDOOR),
                                newContractItem(37, Items.JUNGLE_SIGN),
                                newContractItem(34, Items.LADDER),
                                newContractItem(1, Items.LAPIS_BLOCK),
                                newContractItem(94353, Items.LAVA_BUCKET),
                                newContractItem(1, Items.LECTERN),
                                newContractItem(50, Items.LEVER),
                                newContractItem(20, Items.LIGHT_GRAY_SHULKER_BOX),
                                newContractItem(2, Items.LIGHT_WEIGHTED_PRESSURE_PLATE),
                                newContractItem(123, Items.LIME_CARPET),
                                newContractItem(2919, Items.LIME_CONCRETE),
                                newContractItem(82379, Items.LIME_STAINED_GLASS),
                                newContractItem(1897, Items.MAGMA_BLOCK),
                                newContractItem(1506, Items.NETHERITE_BLOCK),
                                newContractItem(15783, Items.NOTE_BLOCK),
                                newContractItem(1, Items.OAK_BUTTON),
                                newContractItem(8, Items.OAK_FENCE),
                                newContractItem(1, Items.OAK_LEAVES),
                                newContractItem(15, Items.OAK_LOG),
                                newContractItem(1, Items.OAK_PRESSURE_PLATE),
                                newContractItem(16, Items.OAK_SIGN),
                                newContractItem(3, Items.OAK_SLAB),
                                newContractItem(433, Items.OAK_SIGN),
                                newContractItem(112855, Items.OBSERVER),
                                newContractItem(386693, Items.OBSIDIAN),
                                newContractItem(9, Items.PINK_CONCRETE),
                                newContractItem(184, Items.PISTON),
                                newContractItem(2183, Items.PODZOL),
                                newContractItem(282, Items.POLISHED_BLACKSTONE),
                                newContractItem(222, Items.POLISHED_BLACKSTONE_BRICK_SLAB),
                                newContractItem(2198, Items.POLISHED_BLACKSTONE_BRICK_STAIRS),
                                newContractItem(312, Items.POLISHED_BLACKSTONE_BRICK_WALL),
                                newContractItem(2337, Items.POLISHED_BLACKSTONE_BRICKS),
                                newContractItem(685, Items.POLISHED_BLACKSTONE_WALL),
                                newContractItem(3, Items.RAIL),
                                newContractItem(14, Items.RED_CONCRETE),
                                newContractItem(144, Items.REDSTONE_BLOCK),
                                newContractItem(120, Items.REDSTONE_LAMP),
                                newContractItem(307, Items.REDSTONE_TORCH),
                                newContractItem(164, Items.REDSTONE_TORCH),
                                newContractItem(23661, Items.REDSTONE),
                                newContractItem(1197, Items.REPEATER),
                                newContractItem(13937, Items.SAND),
                                newContractItem(6051, Items.SEA_LANTERN),
                                newContractItem(10, Items.SEA_PICKLE),
                                newContractItem(141, Items.SHORT_GRASS),
                                newContractItem(2179, Items.SHROOMLIGHT),
                                newContractItem(3473, Items.SLIME_BLOCK),
                                newContractItem(18, Items.SMOOTH_STONE_SLAB),
                                newContractItem(29, Items.SOUL_LANTERN),
                                newContractItem(1201, Items.SOUL_SAND),
                                newContractItem(553, Items.SOUL_SOIL),
                                newContractItem(10, Items.SPRUCE_SLAB),
                                newContractItem(25, Items.SPRUCE_TRAPDOOR),
                                newContractItem(430, Items.STICKY_PISTON),
                                newContractItem(87055, Items.STONE),
                                newContractItem(42, Items.STONE_BRICK_SLAB),
                                newContractItem(195, Items.STONE_BRICK_WALL),
                                newContractItem(23, Items.STONE_BRICKS),
                                newContractItem(70, Items.STONE_BUTTON),
                                newContractItem(7, Items.STONE_PRESSURE_PLATE),
                                newContractItem(8, Items.STONE_SLAB),
                                newContractItem(2, Items.TALL_GRASS),
                                newContractItem(406, Items.TARGET),
                                newContractItem(7853, Items.STRING),
                                newContractItem(4, Items.TRIPWIRE_HOOK),
                                newContractItem(350, Items.VINE),
                                newContractItem(1, Items.TORCH),
                                newContractItem(298, Items.WARPED_SIGN),
                                newContractItem(1111, Items.WATER_BUCKET),
                                newContractItem(14, Items.WHITE_BED),
                                newContractItem(60, Items.WHITE_CARPET),
                                newContractItem(1, Items.YELLOW_CARPET),
                                newContractItem(3, Items.YELLOW_CONCRETE)
                        ),"hephaestus_vault", 43200, BigInteger.valueOf(500_000_000L),
                        List.of(
                                newContractReward(5, CraftorioItems.EFFECT_RUNE.get(), 2),
                                newContractReward(3, CraftorioItems.MYSTERY_EFFECT_RUNE.get(), 2)
                        ),
                        Optional.of(Craftorio.prefix("general/commeupance_of_the_gods")),
                        10,
                        scientificToInt("1e10"),
                        scientificToInt("1e7"),
                        scientificToInt("1e309"),
                        Optional.empty()
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
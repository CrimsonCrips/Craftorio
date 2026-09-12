package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItem;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItemReward;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;

public class CraftorioShipmentBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioShipmentContract> context) {

        context.register(
                key("cake_delivery"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(1, Items.CAKE)
                        ),"cake_delivery", 120, BigInteger.valueOf(300),
                        List.of(
                                newShipmentReward(1, Items.GOLD_INGOT)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("1e4"),
                        Optional.empty()
                )
        );

        context.register(
                key("animal_feed"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(2, Items.HAY_BLOCK),
                                newShipmentItem(12, Items.POTATO),
                                newShipmentItem(6, Items.CARROT)
                        ),"animal_feed", 300, BigInteger.valueOf(350),
                        List.of(
                                newShipmentReward(1, Items.CAKE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("2e4"),
                        Optional.empty()
                )
        );

        context.register(
                key("care_package"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(1, Items.IRON_HELMET),
                                newShipmentItem(1, Items.IRON_CHESTPLATE),
                                newShipmentItem(1, Items.IRON_LEGGINGS),
                                newShipmentItem(1, Items.IRON_BOOTS),
                                newShipmentItem(1, Items.IRON_SWORD),
                                newShipmentItem(16, Items.BREAD)
                        ),"care_package", 500, BigInteger.valueOf(1200),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("2e5"),
                        Optional.empty()
                )
        );

        context.register(
                key("brewing_materials"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(10, Items.BLAZE_ROD),
                                newShipmentItem(5, Items.BREWING_STAND),
                                newShipmentItem(64, Items.GLASS_BOTTLE),
                                newShipmentItem(12, Items.NETHER_WART)
                        ),"brewing_materials", 1200, BigInteger.valueOf(700),
                        List.of(
                                newShipmentReward(5, Items.EXPERIENCE_BOTTLE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("1e3"),
                        BigInteger.ZERO,
                        scientificToInt("2e5"),
                        Optional.empty()
                )
        );


        context.register(
                key("gold_throne_construction"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(28, Items.GOLD_BLOCK),
                                newShipmentItem(10, Items.CANDLE),
                                newShipmentItem(1, Items.RED_CARPET)
                        ),"gold_throne_construction", 3600, BigInteger.valueOf(12000),
                        List.of(
                                newShipmentReward(1, Items.DIAMOND_BLOCK)
                        ),
                        DEFAULT_ICON,
                        Optional.of(Craftorio.prefix("shop/kingdom_tariff")),
                        1,
                        scientificToInt("5e3"),
                        BigInteger.ZERO,
                        scientificToInt("5e6"),
                        Optional.empty()
                )
        );

        context.register(
                key("dyeabolical"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(100, Items.WHITE_DYE),
                                newShipmentItem(100, Items.BLACK_DYE),
                                newShipmentItem(100, Items.BLUE_DYE),
                                newShipmentItem(100, Items.BROWN_DYE),
                                newShipmentItem(100, Items.CYAN_DYE),
                                newShipmentItem(100, Items.GRAY_DYE),
                                newShipmentItem(100, Items.GREEN_DYE),
                                newShipmentItem(100, Items.LIGHT_BLUE_DYE),
                                newShipmentItem(100, Items.LIGHT_GRAY_DYE),
                                newShipmentItem(100, Items.LIME_DYE),
                                newShipmentItem(100, Items.MAGENTA_DYE),
                                newShipmentItem(100, Items.ORANGE_DYE),
                                newShipmentItem(100, Items.PINK_DYE),
                                newShipmentItem(100, Items.PURPLE_DYE),
                                newShipmentItem(100, Items.RED_DYE),
                                newShipmentItem(100, Items.YELLOW_DYE)
                        ),"dyeabolical", 7200, BigInteger.valueOf(7000),
                        List.of(
                                newShipmentReward(20, Items.EXPERIENCE_BOTTLE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("1e4"),
                        BigInteger.ZERO,
                        scientificToInt("5e6"),
                        Optional.empty()
                )
        );

        context.register(
                key("archery_shipment"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(6, Items.BOW),
                                newShipmentItem(5, Items.CROSSBOW),
                                newShipmentItem(384, Items.ARROW),
                                newShipmentItem(192, Items.SPECTRAL_ARROW)
                        ),"archery_shipment", 3600, BigInteger.valueOf(7500),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("2e4"),
                        BigInteger.ZERO,
                        scientificToInt("9e7"),
                        Optional.empty()
                )
        );

        context.register(
                key("terraforming"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(256, Items.TNT),
                                newShipmentItem(4, Items.DIAMOND_PICKAXE)
                        ),"terraforming", 3600, BigInteger.valueOf(20000),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("1e4"),
                        BigInteger.ZERO,
                        scientificToInt("2e8"),
                        Optional.empty()
                )
        );

        context.register(
                key("kingdoms_feast"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(4, Items.GOLDEN_APPLE),
                                newShipmentItem(8, Items.MILK_BUCKET),
                                newShipmentItem(16, Items.HONEY_BOTTLE),
                                newShipmentItem(16, Items.COOKED_PORKCHOP),
                                newShipmentItem(64, Items.COOKED_BEEF),
                                newShipmentItem(16, Items.COOKED_CHICKEN),
                                newShipmentItem(32, Items.COOKED_MUTTON),
                                newShipmentItem(8, Items.PUMPKIN_PIE),
                                newShipmentItem(9, Items.CAKE)
                        ),"kingdoms_feast", 5400, BigInteger.valueOf(7541),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.of(Craftorio.prefix("shop/kingdom_tariff")),
                        1,
                        scientificToInt("3e4"),
                        scientificToInt("1e4"),
                        scientificToInt("1e6"),
                        Optional.empty()
                )
        );

        context.register(
                key("chicken_coop"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(16, Items.EGG),
                                newShipmentItem(32, Items.OAK_LOG),
                                newShipmentItem(64, Items.WHEAT_SEEDS)
                        ),"chicken_coop", 900, BigInteger.valueOf(384),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("5e3"),
                        scientificToInt("1e3"),
                        scientificToInt("4e5"),
                        Optional.empty()
                )
        );

        context.register(
                key("inquisitors_burning"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(64, Items.PAPER),
                                newShipmentItem(64, Items.INK_SAC),
                                newShipmentItem(64, Items.FEATHER),
                                newShipmentItem(32, Items.DARK_OAK_PLANKS),
                                newShipmentItem(10, Items.FIRE_CHARGE),
                                newShipmentItem(32, Items.COOKED_CHICKEN)
                        ),"inquisitors_burning", 7200, BigInteger.valueOf(2000),
                        List.of(
                                newShipmentReward(1, CraftorioItems.MYSTERY_EFFECT_RUNE.get()),
                                newShipmentReward(2, CraftorioItems.EFFECT_RUNE.get())
                        ),
                        DEFAULT_ICON,
                        Optional.of(Craftorio.prefix("general/inquisitors_wrath")),
                        1,
                        scientificToInt("1e5"),
                        scientificToInt("1e4"),
                        scientificToInt("1e9"),
                        Optional.empty()
                )
        );

        context.register(
                key("copernicium"), new CraftorioShipmentContract(
                        List.of(
                                new CraftorioShipmentItem(1000, CraftorioItemTagGen.COPPER)
                        ),"copernicium", 6000, BigInteger.valueOf(135000),
                        List.of(
                                newShipmentReward(3, CraftorioItems.MYSTERY_EFFECT_RUNE.get()),
                                newShipmentReward(1, CraftorioItems.EFFECT_RUNE.get())
                        ),
                        DEFAULT_ICON,
                        Optional.of(Craftorio.prefix("tag/copper_deficiency")),
                        1,
                        scientificToInt("3e5"),
                        scientificToInt("7e4"),
                        scientificToInt("1e15"),
                        Optional.empty()
                )
        );

        context.register(
                key("trophy_headed"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(1, Items.CREEPER_HEAD),
                                newShipmentItem(1, Items.ZOMBIE_HEAD),
                                newShipmentItem(1, Items.SKELETON_SKULL),
                                newShipmentItem(1, Items.WITHER_SKELETON_SKULL),
                                newShipmentItem(1, Items.DRAGON_HEAD),
                                newShipmentItem(1, Items.PIGLIN_HEAD)
                        ),"trophy_headed", 1200, BigInteger.valueOf(1200),
                        List.of(
                                newShipmentReward(1, CraftorioItems.MYSTERY_EFFECT_RUNE.get())
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("1e5"),
                        scientificToInt("4e4"),
                        scientificToInt("1e50"),
                        Optional.empty()
                )
        );

        context.register(
                key("multiversal_collection"), new CraftorioShipmentContract(
                        allObtainableVanillaItems(),"multiversal_collection", 36000, scientificToInt("1e18"),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.of(Craftorio.prefix("general/trazyns_curse")),
                        1,
                        scientificToInt("1e9"),
                        scientificToInt("5e7"),
                        scientificToInt("1e309"),
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

    private static List<CraftorioShipmentItem> allObtainableVanillaItems() {
        List<CraftorioShipmentItem> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || UNOBTAINABLE_VANILLA_ITEMS.contains(item)) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals("minecraft") || id.getPath().endsWith("_spawn_egg")) continue;

            items.add(newShipmentItem(1, item));
        }
        return items;
    }

    private static ResourceKey<CraftorioShipmentContract> key(String path) {
        return ResourceKey.create(CraftorioShipmentContract.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }

    private static CraftorioShipmentItem newShipmentItem(int amountRequired, Item item){
        return new CraftorioShipmentItem(amountRequired,item);
    }

    private static CraftorioShipmentItemReward newShipmentReward(int amountGiven, Item item){
        return new CraftorioShipmentItemReward(amountGiven,item);
    }
}
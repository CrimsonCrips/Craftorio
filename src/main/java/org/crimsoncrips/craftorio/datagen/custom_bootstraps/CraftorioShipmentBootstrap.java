package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItem;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItemReward;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;

public class CraftorioShipmentBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioShipmentContract> context) {

        context.register(
                key("cake_delivery"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(1, Items.CAKE)
                        ),"cake_delivery", 120, BigInteger.valueOf(350),
                        List.of(
                                newShipmentReward(1, Items.CAKE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("1e4")
                )
        );

        context.register(
                key("animal_feed"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(2, Items.HAY_BLOCK),
                                newShipmentItem(12, Items.POTATO),
                                newShipmentItem(6, Items.CARROT)
                        ),"animal_feed", 600, BigInteger.valueOf(500),
                        List.of(
                                newShipmentReward(1, Items.CAKE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("2e4")
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
                        ),"care_package", 500, scientificToInt("5e3"),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        scientificToInt("8e4")
                )
        );

        context.register(
                key("brewing_materials"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(10, Items.BLAZE_ROD),
                                newShipmentItem(5, Items.BREWING_STAND),
                                newShipmentItem(64, Items.GLASS_BOTTLE),
                                newShipmentItem(12, Items.NETHER_WART)
                        ),"brewing_materials", 1200, scientificToInt("1e4"),
                        List.of(
                                newShipmentReward(5, Items.EXPERIENCE_BOTTLE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("1e3"),
                        BigInteger.ZERO,
                        scientificToInt("2e5")
                )
        );


        context.register(
                key("gold_throne_construction"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(28, Items.GOLD_BLOCK),
                                newShipmentItem(10, Items.CANDLE),
                                newShipmentItem(1, Items.RED_CARPET)
                        ),"gold_throne_construction", 3600, scientificToInt("9e4"),
                        List.of(
                                newShipmentReward(1, Items.DIAMOND_BLOCK)
                        ),
                        DEFAULT_ICON,
                        Optional.of(Craftorio.prefix("shop/kingdom_tariff")),
                        1,
                        scientificToInt("5e3"),
                        BigInteger.ZERO,
                        scientificToInt("5e6")
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
                        ),"dyeabolical", 7200, scientificToInt("5e4"),
                        List.of(
                                newShipmentReward(20, Items.EXPERIENCE_BOTTLE)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("1e4"),
                        BigInteger.ZERO,
                        scientificToInt("5e6")
                )
        );

        context.register(
                key("archery_shipment"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(6, Items.BOW),
                                newShipmentItem(5, Items.CROSSBOW),
                                newShipmentItem(384, Items.ARROW),
                                newShipmentItem(192, Items.SPECTRAL_ARROW)
                        ),"archery_shipment", 3600, scientificToInt("3e5"),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("2e4"),
                        BigInteger.ZERO,
                        scientificToInt("9e7")
                )
        );

        context.register(
                key("terraforming"), new CraftorioShipmentContract(
                        List.of(
                                newShipmentItem(256, Items.TNT),
                                newShipmentItem(4, Items.DIAMOND_PICKAXE)
                        ),"terraforming", 3600, scientificToInt("9e5"),
                        List.of(),
                        DEFAULT_ICON,
                        Optional.empty(),
                        1,
                        scientificToInt("6e4"),
                        BigInteger.ZERO,
                        scientificToInt("2e8")
                )
        );


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
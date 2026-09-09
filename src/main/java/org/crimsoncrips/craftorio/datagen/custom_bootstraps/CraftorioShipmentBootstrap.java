package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItem;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentItemReward;

import java.math.BigInteger;
import java.util.List;

public class CraftorioShipmentBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("locked.png");

    public static void bootstrap(BootstrapContext<CraftorioShipmentContract> context) {
        context.register(
                key("starter_contract"), new CraftorioShipmentContract(
                        List.of(
                        new CraftorioShipmentItem(32, Items.COPPER_INGOT),
                        new CraftorioShipmentItem(16, Items.IRON_INGOT)
                ),"Starter Contract", 500, BigInteger.valueOf(1000),
                        List.of(
                        new CraftorioShipmentItemReward(32, Items.COPPER_INGOT),
                        new CraftorioShipmentItemReward(16, Items.IRON_INGOT)),
                        DEFAULT_ICON
                )
        );
    }

    private static ResourceKey<CraftorioShipmentContract> key(String path) {
        return ResourceKey.create(CraftorioShipmentContract.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}
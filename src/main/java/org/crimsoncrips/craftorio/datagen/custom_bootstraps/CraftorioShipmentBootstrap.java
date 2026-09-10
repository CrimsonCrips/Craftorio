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
import java.util.Optional;

public class CraftorioShipmentBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");
    private static final ResourceLocation STARTER_PUNISHMENT = Craftorio.prefix("tag/copper_block_debuff");

    public static void bootstrap(BootstrapContext<CraftorioShipmentContract> context) {
        context.register(
                key("starter_contract"), new CraftorioShipmentContract(
                        List.of(
                        new CraftorioShipmentItem(32, Items.COPPER_INGOT),
                        new CraftorioShipmentItem(16, Items.IRON_INGOT)
                ),"Starter Contract", "misc.craftorio.starter_contract_description", 25, BigInteger.valueOf(1000),
                        List.of(
                        new CraftorioShipmentItemReward(32, Items.COPPER_INGOT),
                        new CraftorioShipmentItemReward(16, Items.IRON_INGOT)),
                        DEFAULT_ICON,
                        Optional.of(STARTER_PUNISHMENT),
                        false
                )
        );
    }

    private static ResourceKey<CraftorioShipmentContract> key(String path) {
        return ResourceKey.create(CraftorioShipmentContract.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}
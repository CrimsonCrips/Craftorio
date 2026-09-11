package org.crimsoncrips.craftorio.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

@EventBusSubscriber(modid = Craftorio.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CraftorioRegistries {

    public static Registry<MapCodec<? extends CraftorioEffects>> TYPE_REGISTRY;
    public static Registry<MapCodec<? extends CraftorioUpgrade>> UPGRADE_TYPE_REGISTRY;

    @SubscribeEvent
    static void newRegistry(NewRegistryEvent event) {
        RegistryBuilder<MapCodec<? extends CraftorioEffects>> builder =
                new RegistryBuilder<>(CraftorioEffects.TYPE_REGISTRY_KEY);
        TYPE_REGISTRY = event.create(builder);

        RegistryBuilder<MapCodec<? extends CraftorioUpgrade>> upgradeBuilder =
                new RegistryBuilder<>(CraftorioUpgrade.TYPE_REGISTRY_KEY);
        UPGRADE_TYPE_REGISTRY = event.create(upgradeBuilder);
    }

    @SubscribeEvent
    static void newDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(CraftorioEffects.REGISTRY_KEY, CraftorioEffects.dispatchCodec(), CraftorioEffects.dispatchCodec());
        event.dataPackRegistry(CraftorioShipmentContract.REGISTRY_KEY, CraftorioShipmentContract.CODEC, CraftorioShipmentContract.CODEC);
        event.dataPackRegistry(CraftorioUpgrade.REGISTRY_KEY, CraftorioUpgrade.dispatchCodec(), CraftorioUpgrade.dispatchCodec());
    }
}
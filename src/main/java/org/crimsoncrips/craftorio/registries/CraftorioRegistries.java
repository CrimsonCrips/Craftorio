package org.crimsoncrips.craftorio.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.contracts.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

@EventBusSubscriber(modid = Craftorio.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CraftorioRegistries {

    public static Registry<MapCodec<? extends CraftorioEffects>> TYPE_REGISTRY;

    @SubscribeEvent
    static void newRegistry(NewRegistryEvent event) {
        RegistryBuilder<MapCodec<? extends CraftorioEffects>> builder =
                new RegistryBuilder<>(CraftorioEffects.TYPE_REGISTRY_KEY);
        TYPE_REGISTRY = event.create(builder);
    }

    @SubscribeEvent
    static void newDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(CraftorioEffects.REGISTRY_KEY, CraftorioEffects.dispatchCodec());
        event.dataPackRegistry(CraftorioShipmentContract.REGISTRY_KEY, CraftorioShipmentContract.CODEC);
    }
}
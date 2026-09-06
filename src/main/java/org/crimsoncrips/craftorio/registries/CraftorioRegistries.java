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

    public static Registry<MapCodec<? extends CraftorioEffects>> EFFECT_REGISTRY_KEY;
    public static Registry<MapCodec<? extends CraftorioShipmentContract>> CONTRACT_REGISTRY_KEY;

    @SubscribeEvent
    static void newRegistry(NewRegistryEvent event) {
        EFFECT_REGISTRY_KEY = event.create(new RegistryBuilder<>(CraftorioEffects.EFFECT_REGISTRY_KEY));
        CONTRACT_REGISTRY_KEY = event.create(new RegistryBuilder<>(CraftorioShipmentContract.CONTRACT_REGISTRY_KEY));
    }

    @SubscribeEvent
    static void newDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(CraftorioEffects.REGISTRY_KEY, CraftorioEffects.dispatchCodec());
    }
}
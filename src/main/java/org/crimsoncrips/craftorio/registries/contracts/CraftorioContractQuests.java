package org.crimsoncrips.craftorio.registries.contracts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.CraftorioRegistries;
import org.crimsoncrips.craftorio.registries.contracts.shipment.CraftorioShipmentContract;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.List;

public abstract class CraftorioContractQuests {

    private String name;
    private int time;
    private CraftorioShipmentContract contract;

    public static final ResourceKey<Registry<MapCodec<? extends CraftorioContractQuests>>> TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "craftorio_contracts"));

    public static final ResourceKey<Registry<CraftorioContractQuests>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "contracts"));

    private static Codec<CraftorioContractQuests> codecInstance;

//    public static Codec<CraftorioContractQuests> dispatchCodec() {
//        if (codecInstance == null) {
//            codecInstance = CraftorioRegistries.TYPE_REGISTRY.byNameCodec()
//                    .dispatch(CraftorioContractQuests::codec, mapCodec -> mapCodec);
//        }
//        return codecInstance;
//    }

    public abstract MapCodec<? extends CraftorioContractQuests> codec();

    public CraftorioContractQuests(String name, int time, CraftorioShipmentContract contract){
        this.name = name;
        this.time = time;
        this.contract = contract;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void tick(){
        setTime(time - 1);
    }

    public boolean isCompleted(){
       return contract.isComplete();
    }
    

    public boolean shouldEnd(){
        return getTime() <= 0 || isCompleted();
    }
}
package org.crimsoncrips.craftorio.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioRegistries;

public abstract class CraftorioEffects {

    private String name;
    private int time;

    public static final ResourceKey<Registry<MapCodec<? extends CraftorioEffects>>> TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "craftorio_effect_type"));

    public static final ResourceKey<Registry<CraftorioEffects>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "effect"));

    private static Codec<CraftorioEffects> codecInstance;

    public static Codec<CraftorioEffects> dispatchCodec() {
        if (codecInstance == null) {
            codecInstance = CraftorioRegistries.TYPE_REGISTRY.byNameCodec()
                    .dispatch(CraftorioEffects::codec, mapCodec -> mapCodec);
        }
        return codecInstance;
    }

    public abstract MapCodec<? extends CraftorioEffects> codec();

    public CraftorioEffects(String name, int time){
        this.name = name;
        this.time = time;
    }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }
    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }
    public void tick(){ setTime(time - 1); }
    public boolean shouldEnd(){ return time <= 0; }
}
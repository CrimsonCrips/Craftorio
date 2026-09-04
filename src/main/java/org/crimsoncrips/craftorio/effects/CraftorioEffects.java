package org.crimsoncrips.craftorio.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.crimsoncrips.craftorio.Craftorio;

public abstract class CraftorioEffects {

    private String name;
    private int time;


    public static final ResourceKey<Registry<MapCodec<? extends CraftorioEffects>>> TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "craftorio_effects"));

    public static final Registry<MapCodec<? extends CraftorioEffects>> TYPE_REGISTRY =
            new RegistryBuilder<>(TYPE_REGISTRY_KEY).create();

    public static final Codec<CraftorioEffects> CODEC =
            TYPE_REGISTRY.byNameCodec()
                    .dispatch(CraftorioEffects::codec, mapCodec -> mapCodec);

    public abstract MapCodec<? extends CraftorioEffects> codec();
    

    public CraftorioEffects(String name, int time){
        this.name = name;
        this.time = time;
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

    public boolean shouldEnd(){
        return time <= 0;
    }
}

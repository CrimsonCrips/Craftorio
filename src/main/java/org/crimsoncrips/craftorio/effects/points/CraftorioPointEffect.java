package org.crimsoncrips.craftorio.effects.points;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.effects.CraftorioEffects;

public class CraftorioPointEffect extends CraftorioEffects {

    private final float multiplier;

    public CraftorioPointEffect(float multiplier, String name, int time){
        super(name,time);
        this.multiplier = multiplier;
    }

    public float getMultiplier(){
        return multiplier;
    }

    @Override
    public MapCodec<? extends CraftorioEffects> codec() {
        return null;
    }
}

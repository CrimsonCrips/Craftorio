package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

public abstract class CraftorioPointEffect extends CraftorioEffects {

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

    @Override
    public abstract CraftorioEffects copy();
}

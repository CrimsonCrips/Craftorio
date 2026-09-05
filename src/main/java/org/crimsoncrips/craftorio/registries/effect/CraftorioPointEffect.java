package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.MapCodec;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

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

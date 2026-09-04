package org.crimsoncrips.craftorio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.effects.CraftorioEffects;
import org.crimsoncrips.craftorio.effects.points.CraftorioPointEffect;
import org.crimsoncrips.craftorio.effects.points.GeneralMultiplierEffect;

import java.util.function.Supplier;

public class CraftorioPointEffectTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioEffects>> TYPES =
            DeferredRegister.create(CraftorioEffects.TYPE_REGISTRY_KEY, Craftorio.MODID);

    public static final Supplier<MapCodec<GeneralMultiplierEffect>> GENERAL_MULTIPLIER =
            TYPES.register("general_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getName),
                            Codec.INT.fieldOf("time").forGetter(GeneralMultiplierEffect::getTime)
                    ).apply(instance, GeneralMultiplierEffect::new)
            ));

}
package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.Optional;

public class GeneralMultiplierEffect extends CraftorioEffects {

    private final float multiplier;

    public static final Codec<GeneralMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getNameKey),
                    Codec.INT.fieldOf("seconds").forGetter(effect -> effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS),
                    ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(effect -> Optional.ofNullable(effect.getIcon())),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(GeneralMultiplierEffect::getWeight),
                    Codec.BOOL.optionalFieldOf("unobtainable", false).forGetter(GeneralMultiplierEffect::isUnobtainable)
            ).apply(instance, (multiplier, name, seconds, icon, weight, unobtainable) ->
                    new GeneralMultiplierEffect(multiplier, name, seconds, icon.orElse(null), weight, unobtainable))
    );

    public static final StreamCodec<ByteBuf, GeneralMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, GeneralMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, GeneralMultiplierEffect::getNameKey,
            ByteBufCodecs.INT, GeneralMultiplierEffect::getTime,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), effect -> Optional.ofNullable(effect.getIcon()),
            ByteBufCodecs.INT, GeneralMultiplierEffect::getWeight,
            ByteBufCodecs.BOOL, GeneralMultiplierEffect::isUnobtainable,
            (multiplier, name, time, icon, weight, unobtainable) -> {
                GeneralMultiplierEffect effect = new GeneralMultiplierEffect(multiplier, name, time / CraftorioMisc.SECONDS_TO_TICKS, icon.orElse(null), weight, unobtainable);
                effect.setTime(time);
                return effect;
            }
    );

    public float getMultiplier(){
        return multiplier;
    }

    @Override
    public MapCodec<? extends CraftorioEffects> codec() {
        return CraftorioEffectTypes.GENERAL_MULTIPLIER.get();
    }

    public GeneralMultiplierEffect(float multiplier,String key,int seconds, ResourceLocation icon, int weight, boolean unobtainable){
        super(key, seconds * CraftorioMisc.SECONDS_TO_TICKS, icon, weight, unobtainable);
        this.multiplier = multiplier;
    }

    @Override
    public GeneralMultiplierEffect copy() {
        GeneralMultiplierEffect copy = new GeneralMultiplierEffect(getMultiplier(), getNameKey(), getTime() / CraftorioMisc.SECONDS_TO_TICKS, getIcon(), getWeight(), isUnobtainable());
        copy.setTime(getTime());
        return copy;
    }

}

package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class GeneralMultiplierEffect extends CraftorioEffects {

    private final float multiplier;

    public static final Codec<GeneralMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getNameKey),
                    Codec.INT.fieldOf("time").forGetter(GeneralMultiplierEffect::getTime),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(GeneralMultiplierEffect::getIcon)
            ).apply(instance, GeneralMultiplierEffect::new)
    );

    public static final StreamCodec<ByteBuf, GeneralMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, GeneralMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, GeneralMultiplierEffect::getNameKey,
            ByteBufCodecs.INT, GeneralMultiplierEffect::getTime,
            ResourceLocation.STREAM_CODEC, GeneralMultiplierEffect::getIcon,
            GeneralMultiplierEffect::new
    );

    public float getMultiplier(){
        return multiplier;
    }

    @Override
    public MapCodec<? extends CraftorioEffects> codec() {
        return CraftorioEffectTypes.GENERAL_MULTIPLIER.get();
    }

    public GeneralMultiplierEffect(float multiplier,String key,int time, ResourceLocation icon){
        super(key,time,icon);
        this.multiplier = multiplier;
    }

    @Override
    public GeneralMultiplierEffect copy() {
        return new GeneralMultiplierEffect(getMultiplier(), getNameKey(), getTime(), getIcon());
    }

}

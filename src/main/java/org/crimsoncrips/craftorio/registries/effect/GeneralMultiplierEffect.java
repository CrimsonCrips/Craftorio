package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class GeneralMultiplierEffect extends CraftorioPointEffect {

    public static final Codec<GeneralMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getNameKey),
                    Codec.INT.fieldOf("time").forGetter(GeneralMultiplierEffect::getTime)
            ).apply(instance, GeneralMultiplierEffect::new)
    );

    public static final StreamCodec<ByteBuf, GeneralMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, GeneralMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, GeneralMultiplierEffect::getNameKey,
            ByteBufCodecs.INT, GeneralMultiplierEffect::getTime,
            GeneralMultiplierEffect::new
    );


    @Override
    public MapCodec<? extends CraftorioPointEffect> codec() {
        return CraftorioPointEffectTypes.GENERAL_MULTIPLIER.get();
    }

    public GeneralMultiplierEffect(float multiplier,String key,int time){
        super(multiplier,key,time);
    }

    @Override
    public GeneralMultiplierEffect copy() {
        return new GeneralMultiplierEffect(getMultiplier(), getNameKey(), getTime());
    }

}

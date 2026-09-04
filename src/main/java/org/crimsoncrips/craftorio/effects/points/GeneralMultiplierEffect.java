package org.crimsoncrips.craftorio.effects.points;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.crimsoncrips.craftorio.CraftorioPointEffectTypes;

public class GeneralMultiplierEffect extends CraftorioPointEffect {

    public static final Codec<GeneralMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getName),
                    Codec.INT.fieldOf("time").forGetter(GeneralMultiplierEffect::getTime)
            ).apply(instance, GeneralMultiplierEffect::new)
    );

    public static final StreamCodec<ByteBuf, GeneralMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, GeneralMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, GeneralMultiplierEffect::getName,
            ByteBufCodecs.INT, GeneralMultiplierEffect::getTime,
            GeneralMultiplierEffect::new
    );


    @Override
    public MapCodec<? extends CraftorioPointEffect> codec() {
        return CraftorioPointEffectTypes.GENERAL_MULTIPLIER.get();
    }

    public GeneralMultiplierEffect(float multiplier,String name,int time){
        super(multiplier,name,time);
    }

}

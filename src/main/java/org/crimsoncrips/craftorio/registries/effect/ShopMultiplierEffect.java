package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ShopMultiplierEffect extends CraftorioEffects {

    private final float multiplier;

    public static final Codec<ShopMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(ShopMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(ShopMultiplierEffect::getNameKey),
                    Codec.INT.fieldOf("time").forGetter(ShopMultiplierEffect::getTime)
            ).apply(instance, ShopMultiplierEffect::new)
    );

    public static final StreamCodec<ByteBuf, ShopMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ShopMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, ShopMultiplierEffect::getNameKey,
            ByteBufCodecs.INT, ShopMultiplierEffect::getTime,
            ShopMultiplierEffect::new
    );

    public float getMultiplier(){
        return multiplier;
    }


    @Override
    public MapCodec<? extends CraftorioEffects> codec() {
        return CraftorioEffectTypes.SHOP_MULTIPLIER.get();
    }

    public ShopMultiplierEffect(float multiplier, String key, int time){
        super(key,time);
        this.multiplier = multiplier;
    }

    @Override
    public ShopMultiplierEffect copy() {
        return new ShopMultiplierEffect(getMultiplier(), getNameKey(), getTime());
    }

}

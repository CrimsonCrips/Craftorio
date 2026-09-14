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

public class ShopMultiplierEffect extends CraftorioEffects {

    private final float multiplier;

    public static final Codec<ShopMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(ShopMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(ShopMultiplierEffect::getNameKey),
                    Codec.INT.fieldOf("seconds").forGetter(effect -> effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS),
                    ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(effect -> Optional.ofNullable(effect.getIcon())),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(ShopMultiplierEffect::getWeight),
                    Codec.BOOL.optionalFieldOf("unobtainable", false).forGetter(ShopMultiplierEffect::isUnobtainable)
            ).apply(instance, (multiplier, name, seconds, icon, weight, unobtainable) ->
                    new ShopMultiplierEffect(multiplier, name, seconds, icon.orElse(null), weight, unobtainable))
    );

    public static final StreamCodec<ByteBuf, ShopMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ShopMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, ShopMultiplierEffect::getNameKey,
            ByteBufCodecs.INT, ShopMultiplierEffect::getTime,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), effect -> Optional.ofNullable(effect.getIcon()),
            ByteBufCodecs.INT, ShopMultiplierEffect::getWeight,
            ByteBufCodecs.BOOL, ShopMultiplierEffect::isUnobtainable,
            (multiplier, name, time, icon, weight, unobtainable) -> {
                ShopMultiplierEffect effect = new ShopMultiplierEffect(multiplier, name, time / CraftorioMisc.SECONDS_TO_TICKS, icon.orElse(null), weight, unobtainable);
                effect.setTime(time);
                return effect;
            }
    );

    public float getMultiplier(){
        return multiplier;
    }


    @Override
    public MapCodec<? extends CraftorioEffects> codec() {
        return CraftorioEffectTypes.SHOP_MULTIPLIER.get();
    }

    public ShopMultiplierEffect(float multiplier, String key, int seconds, ResourceLocation icon, boolean unobtainable){
        this(multiplier, "registry." + key, seconds, icon, 0, unobtainable);
    }

    public ShopMultiplierEffect(float multiplier, String key, int seconds, ResourceLocation icon, int weight){
        this(multiplier, "registry." + key, seconds, icon, weight, false);
    }

    public ShopMultiplierEffect(float multiplier, String name, int seconds, ResourceLocation icon, int weight, boolean unobtainable){
        super(name, seconds * CraftorioMisc.SECONDS_TO_TICKS, icon, weight, unobtainable);
        this.multiplier = multiplier;
    }

    @Override
    public ShopMultiplierEffect copy() {
        ShopMultiplierEffect copy = new ShopMultiplierEffect(getMultiplier(), getNameKey(), getTime() / CraftorioMisc.SECONDS_TO_TICKS, getIcon(), getWeight(), isUnobtainable());
        copy.setTime(getTime());
        return copy;
    }

}

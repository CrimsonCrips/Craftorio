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
                    Codec.BOOL.optionalFieldOf("unobtainable", false).forGetter(ShopMultiplierEffect::isUnobtainable),
                    Codec.BOOL.optionalFieldOf("loan_marked", false).forGetter(ShopMultiplierEffect::isLoanMarked),
                    EffectOperation.CODEC.optionalFieldOf("operation", EffectOperation.ADD).forGetter(ShopMultiplierEffect::getOperation)
            ).apply(instance, (multiplier, name, seconds, icon, weight, unobtainable, loanMarked, operation) -> {
                    ShopMultiplierEffect effect = new ShopMultiplierEffect(multiplier, name, seconds, icon.orElse(null), weight, unobtainable);
                    effect.setLoanMarked(loanMarked);
                    effect.setOperation(operation);
                    return effect;
            })
    );

    private record WeightFlags(int weight, boolean unobtainable, boolean loanMarked, EffectOperation operation) {}

    private static final StreamCodec<ByteBuf, WeightFlags> WEIGHT_FLAGS_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, WeightFlags::weight,
            ByteBufCodecs.BOOL, WeightFlags::unobtainable,
            ByteBufCodecs.BOOL, WeightFlags::loanMarked,
            EffectOperation.STREAM_CODEC, WeightFlags::operation,
            WeightFlags::new
    );

    public static final StreamCodec<ByteBuf, ShopMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ShopMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, ShopMultiplierEffect::getNameKey,
            ByteBufCodecs.INT, ShopMultiplierEffect::getTime,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), effect -> Optional.ofNullable(effect.getIcon()),
            WEIGHT_FLAGS_STREAM, effect -> new WeightFlags(effect.getWeight(), effect.isUnobtainable(), effect.isLoanMarked(), effect.getOperation()),
            (multiplier, name, time, icon, flags) -> {
                ShopMultiplierEffect effect = new ShopMultiplierEffect(multiplier, name, time / CraftorioMisc.SECONDS_TO_TICKS, icon.orElse(null), flags.weight(), flags.unobtainable());
                effect.setTime(time);
                effect.setLoanMarked(flags.loanMarked());
                effect.setOperation(flags.operation());
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
        copy.setLoanMarked(isLoanMarked());
        copy.setOperation(getOperation());
        return copy;
    }

}

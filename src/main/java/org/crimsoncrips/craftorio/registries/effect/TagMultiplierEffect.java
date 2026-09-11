package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.Optional;

public class TagMultiplierEffect extends CraftorioEffects {

    private final float multiplier;
    private final TagKey<Item> itemTag;

    public static final Codec<TagMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(TagMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(TagMultiplierEffect::getNameKey),
                    TagKey.hashedCodec(Registries.ITEM).fieldOf("item_tag").forGetter(TagMultiplierEffect::getItemTag),
                    Codec.INT.fieldOf("seconds").forGetter(effect -> effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS),
                    ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(effect -> Optional.ofNullable(effect.getIcon())),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(TagMultiplierEffect::getWeight),
                    Codec.BOOL.optionalFieldOf("ambient", true).forGetter(TagMultiplierEffect::isAmbient)
            ).apply(instance, (multiplier, name, itemTag, seconds, icon, weight, ambient) ->
                    new TagMultiplierEffect(multiplier, name, itemTag, seconds, icon.orElse(null), weight, ambient))
    );

    private static final StreamCodec<ByteBuf, WeightAmbient> WEIGHT_AMBIENT_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, WeightAmbient::weight,
            ByteBufCodecs.BOOL, WeightAmbient::ambient,
            WeightAmbient::new
    );

    public static final StreamCodec<ByteBuf, TagMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TagMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, TagMultiplierEffect::getNameKey,
            ByteBufCodecs.fromCodec(TagKey.hashedCodec(Registries.ITEM)), TagMultiplierEffect::getItemTag,
            ByteBufCodecs.INT, TagMultiplierEffect::getTime,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), effect -> Optional.ofNullable(effect.getIcon()),
            WEIGHT_AMBIENT_STREAM, effect -> new WeightAmbient(effect.getWeight(), effect.isAmbient()),
            (multiplier, name, itemTag, time, icon, weightAmbient) -> {
                TagMultiplierEffect effect = new TagMultiplierEffect(multiplier, name, itemTag, time / CraftorioMisc.SECONDS_TO_TICKS, icon.orElse(null), weightAmbient.weight(), weightAmbient.ambient());
                effect.setTime(time);
                return effect;
            }
    );

    private record WeightAmbient(int weight, boolean ambient) {}

    public TagMultiplierEffect(float multiplier, String key, TagKey<Item> itemTag, int seconds, ResourceLocation icon, int weight, boolean ambient){
        super(key, seconds * CraftorioMisc.SECONDS_TO_TICKS, icon, weight, ambient);
        this.multiplier = multiplier;
        this.itemTag = itemTag;
    }

    public float getMultiplier(){
        return multiplier;
    }

    public float getTagMultiplier(ItemStack item) {
        return item.is(itemTag) ? getMultiplier() : 0F;
    }

    public TagKey<Item> getItemTag() {
        return itemTag;
    }

    @Override
    public MapCodec<? extends CraftorioEffects> codec() {
        return CraftorioEffectTypes.TAG_MULTIPLIER.get();
    }

    @Override
    public TagMultiplierEffect copy() {
        TagMultiplierEffect copy = new TagMultiplierEffect(getMultiplier(), getNameKey(), getItemTag(), getTime() / CraftorioMisc.SECONDS_TO_TICKS, getIcon(), getWeight(), isAmbient());
        copy.setTime(getTime());
        return copy;
    }
}

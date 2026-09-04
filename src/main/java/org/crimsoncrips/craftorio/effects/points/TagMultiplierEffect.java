package org.crimsoncrips.craftorio.effects.points;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagMultiplierEffect extends CraftorioPointEffect {

    private final TagKey<Item> itemTag;

    public static final Codec<TagMultiplierEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(TagMultiplierEffect::getMultiplier),
                    Codec.STRING.fieldOf("name").forGetter(TagMultiplierEffect::getName),
                    TagKey.hashedCodec(Registries.ITEM).fieldOf("item_tag").forGetter(TagMultiplierEffect::getItemTag),
                    Codec.INT.fieldOf("time").forGetter(TagMultiplierEffect::getTime)
            ).apply(instance, TagMultiplierEffect::new)
    );

    public static final StreamCodec<ByteBuf, TagMultiplierEffect> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TagMultiplierEffect::getMultiplier,
            ByteBufCodecs.STRING_UTF8, TagMultiplierEffect::getName,
            ByteBufCodecs.fromCodec(TagKey.hashedCodec(Registries.ITEM)),TagMultiplierEffect::getItemTag,
            ByteBufCodecs.INT, TagMultiplierEffect::getTime,
            TagMultiplierEffect::new
    );

    public TagMultiplierEffect(float multiplier, String name,TagKey<Item> itemTag,int time){
        super(multiplier,name,time);
        this.itemTag = itemTag;
    }


    public float getTagMultiplier(ItemStack item) {
        return item.is(itemTag) ? getMultiplier() : 0F;
    }

    public TagKey<Item> getItemTag() {
        return itemTag;
    }
}

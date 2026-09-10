package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.Optional;
import java.util.function.Supplier;

public class CraftorioEffectTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioEffects>> TYPES =
            DeferredRegister.create(CraftorioEffects.TYPE_REGISTRY_KEY, Craftorio.MODID);

    public static final Supplier<MapCodec<GeneralMultiplierEffect>> GENERAL_MULTIPLIER =
            TYPES.register("general_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getNameKey),
                            Codec.INT.fieldOf("seconds").forGetter(effect -> effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS),
                            ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(effect -> Optional.ofNullable(effect.getIcon()))
                    ).apply(instance, (multiplier, name, seconds, icon) ->
                            new GeneralMultiplierEffect(multiplier, name, seconds, icon.orElse(null)))
            ));

    public static final Supplier<MapCodec<TagMultiplierEffect>> TAG_MULTIPLIER =
            TYPES.register("tag_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(TagMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(TagMultiplierEffect::getNameKey),
                            TagKey.hashedCodec(Registries.ITEM).fieldOf("item_tag").forGetter(TagMultiplierEffect::getItemTag),
                            Codec.INT.fieldOf("seconds").forGetter(effect -> effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS),
                            ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(effect -> Optional.ofNullable(effect.getIcon()))
                    ).apply(instance, (multiplier, name, itemTag, seconds, icon) ->
                            new TagMultiplierEffect(multiplier, name, itemTag, seconds, icon.orElse(null)))
            ));

    public static final Supplier<MapCodec<ShopMultiplierEffect>> SHOP_MULTIPLIER =
            TYPES.register("shop_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(ShopMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(ShopMultiplierEffect::getNameKey),
                            Codec.INT.fieldOf("seconds").forGetter(effect -> effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS),
                            ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(effect -> Optional.ofNullable(effect.getIcon()))
                    ).apply(instance, (multiplier, name, seconds, icon) ->
                            new ShopMultiplierEffect(multiplier, name, seconds, icon.orElse(null)))
            ));
}

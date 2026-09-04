package org.crimsoncrips.craftorio.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.effects.points.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.effects.points.TagMultiplierEffect;

import java.util.function.Supplier;

public class CraftorioPointEffectTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioEffects>> TYPES =
            DeferredRegister.create(CraftorioEffects.TYPE_REGISTRY_KEY, Craftorio.MODID);

    public static final Supplier<MapCodec<GeneralMultiplierEffect>> GENERAL_MULTIPLIER =
            TYPES.register("general_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(GeneralMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(GeneralMultiplierEffect::getName),
                            Codec.INT.fieldOf("time").forGetter(GeneralMultiplierEffect::getTime)
                    ).apply(instance, GeneralMultiplierEffect::new)
            ));

    public static final Supplier<MapCodec<TagMultiplierEffect>> TAG_MULTIPLIER =
            TYPES.register("tag_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(TagMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(TagMultiplierEffect::getName),
                            TagKey.hashedCodec(Registries.ITEM).fieldOf("item_tag").forGetter(TagMultiplierEffect::getItemTag),
                            Codec.INT.fieldOf("time").forGetter(TagMultiplierEffect::getTime)
                    ).apply(instance, TagMultiplierEffect::new)
            ));
}
package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.function.Supplier;

public class CraftorioPointEffectTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioEffects>> TYPES =
            DeferredRegister.create(CraftorioEffects.TYPE_REGISTRY_KEY, Craftorio.MODID);

    public static final Supplier<MapCodec<org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect>> GENERAL_MULTIPLIER =
            TYPES.register("general_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect::getName),
                            Codec.INT.fieldOf("time").forGetter(org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect::getTime)
                    ).apply(instance, org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect::new)
            ));

    public static final Supplier<MapCodec<org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect>> TAG_MULTIPLIER =
            TYPES.register("tag_multiplier", () -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect::getMultiplier),
                            Codec.STRING.fieldOf("name").forGetter(org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect::getName),
                            TagKey.hashedCodec(Registries.ITEM).fieldOf("item_tag").forGetter(org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect::getItemTag),
                            Codec.INT.fieldOf("time").forGetter(org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect::getTime)
                    ).apply(instance, org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect::new)
            ));
}
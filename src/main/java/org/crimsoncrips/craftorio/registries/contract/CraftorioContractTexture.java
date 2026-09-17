package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;

public record CraftorioContractTexture(ResourceLocation texture) {

    public static final ResourceKey<Registry<CraftorioContractTexture>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "contract_texture"));

    public static final ResourceKey<CraftorioContractTexture> DEFAULT =
            ResourceKey.create(REGISTRY_KEY, Craftorio.prefix("default"));

    public static final Codec<CraftorioContractTexture> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(CraftorioContractTexture::texture)
            ).apply(instance, CraftorioContractTexture::new)
    );
}

package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;

public record CraftorioDefaultContractTexture(ResourceLocation texture) {

    public static final ResourceKey<Registry<CraftorioDefaultContractTexture>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "default_contract_texture"));

    public static final ResourceKey<CraftorioDefaultContractTexture> DEFAULT =
            ResourceKey.create(REGISTRY_KEY, Craftorio.prefix("default"));

    public static final Codec<CraftorioDefaultContractTexture> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(CraftorioDefaultContractTexture::texture)
            ).apply(instance, CraftorioDefaultContractTexture::new)
    );
}

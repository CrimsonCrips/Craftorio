package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ContractGoal(ContractType type, Optional<ResourceLocation> structure) {

    public static final ContractGoal SINK = new ContractGoal(ContractType.SINK, Optional.empty());

    public static final Codec<ContractGoal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContractType.CODEC.optionalFieldOf("type", ContractType.SINK).forGetter(ContractGoal::type),
            ResourceLocation.CODEC.optionalFieldOf("structure").forGetter(ContractGoal::structure)
    ).apply(instance, ContractGoal::new));

    public static final StreamCodec<ByteBuf, ContractGoal> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> ContractType.values()[index], ContractType::ordinal), ContractGoal::type,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ContractGoal::structure,
            ContractGoal::new
    );

    public static ContractGoal building(ResourceLocation structure) {
        return new ContractGoal(ContractType.BUILDING, Optional.of(structure));
    }
}

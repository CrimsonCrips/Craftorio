package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record ContractProgress(int blocksPlaced, int blocksTotal, Optional<BuildPlacement> placement, Optional<UUID> instance, boolean submitted) {

    public static final ContractProgress EMPTY = new ContractProgress(0, 0, Optional.empty(), Optional.empty(), false);

    public static final Codec<ContractProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("blocks_placed", 0).forGetter(ContractProgress::blocksPlaced),
            Codec.INT.optionalFieldOf("blocks_total", 0).forGetter(ContractProgress::blocksTotal),
            BuildPlacement.CODEC.optionalFieldOf("placement").forGetter(ContractProgress::placement),
            UUIDUtil.CODEC.optionalFieldOf("instance").forGetter(ContractProgress::instance),
            Codec.BOOL.optionalFieldOf("submitted", false).forGetter(ContractProgress::submitted)
    ).apply(instance, ContractProgress::new));

    public static final StreamCodec<ByteBuf, ContractProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ContractProgress::blocksPlaced,
            ByteBufCodecs.VAR_INT, ContractProgress::blocksTotal,
            ByteBufCodecs.optional(BuildPlacement.STREAM_CODEC), ContractProgress::placement,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ContractProgress::instance,
            ByteBufCodecs.BOOL, ContractProgress::submitted,
            ContractProgress::new
    );

    public ContractProgress withBuild(int placed, int total) {
        return new ContractProgress(placed, total, placement, instance, submitted);
    }

    public ContractProgress withPlacement(Optional<BuildPlacement> value) {
        return new ContractProgress(0, blocksTotal, value, instance, submitted);
    }

    public ContractProgress withInstance(UUID value) {
        return new ContractProgress(blocksPlaced, blocksTotal, placement, Optional.of(value), submitted);
    }

    public ContractProgress withSubmitted(boolean value) {
        return new ContractProgress(blocksPlaced, blocksTotal, placement, instance, value);
    }
}

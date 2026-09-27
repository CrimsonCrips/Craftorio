package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Rotation;

public record BuildPlacement(GlobalPos origin, Rotation rotation) {

    public static final Codec<BuildPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("origin").forGetter(BuildPlacement::origin),
            Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(BuildPlacement::rotation)
    ).apply(instance, BuildPlacement::new));

    public static final StreamCodec<ByteBuf, BuildPlacement> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, BuildPlacement::origin,
            ByteBufCodecs.idMapper(index -> Rotation.values()[index], Rotation::ordinal), BuildPlacement::rotation,
            BuildPlacement::new
    );
}

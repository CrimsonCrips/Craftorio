package org.crimsoncrips.craftorio.item.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StructureWandSettings(String name) {

    public static final StructureWandSettings DEFAULT = new StructureWandSettings("");

    public static final Codec<StructureWandSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(StructureWandSettings::name)
    ).apply(instance, StructureWandSettings::new));

    public static final StreamCodec<ByteBuf, StructureWandSettings> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}

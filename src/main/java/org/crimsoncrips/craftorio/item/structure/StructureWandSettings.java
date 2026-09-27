package org.crimsoncrips.craftorio.item.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public record StructureWandSettings(boolean loadMode, String name, Rotation rotation, Mirror mirror, float integrity, long seed) {

    public static final StructureWandSettings DEFAULT = new StructureWandSettings(false, "", Rotation.NONE, Mirror.NONE, 1.0F, 0L);

    public static final Codec<StructureWandSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("load_mode", false).forGetter(StructureWandSettings::loadMode),
            Codec.STRING.optionalFieldOf("name", "").forGetter(StructureWandSettings::name),
            Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(StructureWandSettings::rotation),
            Mirror.CODEC.optionalFieldOf("mirror", Mirror.NONE).forGetter(StructureWandSettings::mirror),
            Codec.FLOAT.optionalFieldOf("integrity", 1.0F).forGetter(StructureWandSettings::integrity),
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(StructureWandSettings::seed)
    ).apply(instance, StructureWandSettings::new));

    public static final StreamCodec<ByteBuf, StructureWandSettings> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}

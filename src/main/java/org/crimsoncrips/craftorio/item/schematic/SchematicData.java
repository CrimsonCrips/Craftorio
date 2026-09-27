package org.crimsoncrips.craftorio.item.schematic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

import java.util.Optional;
import java.util.UUID;

public record SchematicData(UUID instance, ResourceLocation contract, ResourceLocation structure, String owner,
                            Rotation rotation, Optional<GlobalPos> origin, boolean converted) {

    public static final Codec<SchematicData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("instance").forGetter(SchematicData::instance),
            ResourceLocation.CODEC.fieldOf("contract").forGetter(SchematicData::contract),
            ResourceLocation.CODEC.fieldOf("structure").forGetter(SchematicData::structure),
            Codec.STRING.optionalFieldOf("owner", "").forGetter(SchematicData::owner),
            Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(SchematicData::rotation),
            GlobalPos.CODEC.optionalFieldOf("origin").forGetter(SchematicData::origin),
            Codec.BOOL.optionalFieldOf("converted", false).forGetter(SchematicData::converted)
    ).apply(instance, SchematicData::new));

    public static final StreamCodec<ByteBuf, SchematicData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public SchematicData withRotation(Rotation value) {
        return new SchematicData(instance, contract, structure, owner, value, origin, converted);
    }

    public SchematicData withOrigin(Optional<GlobalPos> value) {
        return new SchematicData(instance, contract, structure, owner, rotation, value, converted);
    }

    public SchematicData withConverted(boolean value) {
        return new SchematicData(instance, contract, structure, owner, rotation, origin, value);
    }
}

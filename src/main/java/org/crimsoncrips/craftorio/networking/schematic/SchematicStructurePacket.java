package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.schematic.ClientSchematics;

import java.util.Optional;

public record SchematicStructurePacket(ResourceLocation structure, Optional<CompoundTag> data) implements CustomPacketPayload {

    public static final Type<SchematicStructurePacket> TYPE = new Type<>(Craftorio.prefix("schematic_structure_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicStructurePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SchematicStructurePacket::structure,
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), SchematicStructurePacket::data,
            SchematicStructurePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SchematicStructurePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientSchematics.receive(message.structure(), message.data()));
    }
}

package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.schematic.ClientSchematics;

public record SaveCreateSchematicPacket(String fileName, CompoundTag data) implements CustomPacketPayload {

    public static final Type<SaveCreateSchematicPacket> TYPE = new Type<>(Craftorio.prefix("save_create_schematic_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveCreateSchematicPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SaveCreateSchematicPacket::fileName,
            ByteBufCodecs.COMPOUND_TAG, SaveCreateSchematicPacket::data,
            SaveCreateSchematicPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SaveCreateSchematicPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientSchematics.saveSchematic(message.fileName(), message.data()));
    }
}

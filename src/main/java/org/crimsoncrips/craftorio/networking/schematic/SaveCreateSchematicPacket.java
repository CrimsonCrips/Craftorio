package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.schematic.ClientSchematics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record SaveCreateSchematicPacket(String fileName, int index, int total, byte[] part) implements CustomPacketPayload {

    public static final Type<SaveCreateSchematicPacket> TYPE = new Type<>(Craftorio.prefix("save_create_schematic_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveCreateSchematicPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SaveCreateSchematicPacket::fileName,
            ByteBufCodecs.VAR_INT, SaveCreateSchematicPacket::index,
            ByteBufCodecs.VAR_INT, SaveCreateSchematicPacket::total,
            ByteBufCodecs.BYTE_ARRAY, SaveCreateSchematicPacket::part,
            SaveCreateSchematicPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static List<SaveCreateSchematicPacket> create(String fileName, CompoundTag data) throws IOException {
        List<byte[]> chunks = SchematicTransfer.split(data);
        List<SaveCreateSchematicPacket> packets = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            packets.add(new SaveCreateSchematicPacket(fileName, i, chunks.size(), chunks.get(i)));
        }
        return packets;
    }

    public static void handle(SaveCreateSchematicPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                CompoundTag tag = SchematicTransfer.accept("create:" + message.fileName(), message.index(), message.total(), message.part());
                if (tag != null) {
                    ClientSchematics.saveSchematic(message.fileName(), tag);
                }
            } catch (IOException | RuntimeException e) {
                Craftorio.LOGGER.error("Failed to receive the Create schematic {}", message.fileName(), e);
            }
        });
    }
}

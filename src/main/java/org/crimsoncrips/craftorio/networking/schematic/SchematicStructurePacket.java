package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.schematic.ClientSchematics;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public record SchematicStructurePacket(ResourceLocation structure, boolean found, int index, int total, byte[] part) implements CustomPacketPayload {

    public static final Type<SchematicStructurePacket> TYPE = new Type<>(Craftorio.prefix("schematic_structure_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicStructurePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SchematicStructurePacket::structure,
            ByteBufCodecs.BOOL, SchematicStructurePacket::found,
            ByteBufCodecs.VAR_INT, SchematicStructurePacket::index,
            ByteBufCodecs.VAR_INT, SchematicStructurePacket::total,
            ByteBufCodecs.BYTE_ARRAY, SchematicStructurePacket::part,
            SchematicStructurePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, ResourceLocation structure, Optional<CompoundTag> tag) {
        if (tag.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new SchematicStructurePacket(structure, false, 0, 1, new byte[0]));
            return;
        }
        try {
            List<byte[]> chunks = SchematicTransfer.split(tag.get());
            for (int i = 0; i < chunks.size(); i++) {
                PacketDistributor.sendToPlayer(player, new SchematicStructurePacket(structure, true, i, chunks.size(), chunks.get(i)));
            }
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to send the schematic structure {}", structure, e);
            PacketDistributor.sendToPlayer(player, new SchematicStructurePacket(structure, false, 0, 1, new byte[0]));
        }
    }

    public static void handle(SchematicStructurePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!message.found()) {
                ClientSchematics.receive(message.structure(), Optional.empty());
                return;
            }
            try {
                CompoundTag tag = SchematicTransfer.accept("structure:" + message.structure(), message.index(), message.total(), message.part());
                if (tag != null) {
                    ClientSchematics.receive(message.structure(), Optional.of(tag));
                }
            } catch (IOException | RuntimeException e) {
                Craftorio.LOGGER.error("Failed to read the schematic structure {}", message.structure(), e);
                ClientSchematics.receive(message.structure(), Optional.empty());
            }
        });
    }
}

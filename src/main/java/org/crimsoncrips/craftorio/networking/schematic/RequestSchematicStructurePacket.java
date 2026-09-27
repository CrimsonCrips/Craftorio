package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

public record RequestSchematicStructurePacket(ResourceLocation structure) implements CustomPacketPayload {

    private static final int MAX_BYTES = 900_000;

    public static final Type<RequestSchematicStructurePacket> TYPE = new Type<>(Craftorio.prefix("request_schematic_structure_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSchematicStructurePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RequestSchematicStructurePacket::structure,
            RequestSchematicStructurePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestSchematicStructurePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Optional<CompoundTag> tag = CraftorioSchematics.tag(player.server, message.structure());
            if (tag.isPresent() && size(tag.get()) > MAX_BYTES) {
                Craftorio.LOGGER.warn("Schematic structure {} is too large to send to clients", message.structure());
                tag = Optional.empty();
            }
            PacketDistributor.sendToPlayer(player, new SchematicStructurePacket(message.structure(), tag));
        });
    }

    private static int size(CompoundTag tag) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(tag, output);
        } catch (IOException e) {
            return Integer.MAX_VALUE;
        }
        return bytes.size();
    }
}

package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

import java.util.UUID;

public record RequestSchematicCopyPacket(UUID instance) implements CustomPacketPayload {

    public static final Type<RequestSchematicCopyPacket> TYPE = new Type<>(Craftorio.prefix("request_schematic_copy_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSchematicCopyPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestSchematicCopyPacket::instance,
            RequestSchematicCopyPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestSchematicCopyPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                CraftorioSchematics.giveSchematicCopy(player, message.instance());
            }
        });
    }
}

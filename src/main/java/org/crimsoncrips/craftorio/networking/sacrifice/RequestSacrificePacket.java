package org.crimsoncrips.craftorio.networking.sacrifice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;

public record RequestSacrificePacket() implements CustomPacketPayload {

    public static final Type<RequestSacrificePacket> TYPE = new Type<>(Craftorio.prefix("request_sacrifice_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSacrificePacket> STREAM_CODEC = StreamCodec.unit(new RequestSacrificePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestSacrificePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                CraftorioSacrifice.enter(player);
            }
        });
    }
}

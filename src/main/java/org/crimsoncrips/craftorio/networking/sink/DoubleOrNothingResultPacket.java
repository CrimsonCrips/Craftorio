package org.crimsoncrips.craftorio.networking.sink;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.machine.SinkScreen;

import java.math.BigInteger;

public record DoubleOrNothingResultPacket(boolean heads, BigInteger escrowPoints, int winStreak, BigInteger refundPoints) implements CustomPacketPayload {

    public static final Type<DoubleOrNothingResultPacket> TYPE = new Type<>(Craftorio.prefix("double_or_nothing_result_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleOrNothingResultPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DoubleOrNothingResultPacket::heads,
            ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()), DoubleOrNothingResultPacket::escrowPoints,
            ByteBufCodecs.VAR_INT, DoubleOrNothingResultPacket::winStreak,
            ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()), DoubleOrNothingResultPacket::refundPoints,
            DoubleOrNothingResultPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DoubleOrNothingResultPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof SinkScreen sinkScreen) {
                sinkScreen.onDoubleOrNothingResult(message.heads(), message.escrowPoints(), message.winStreak(), message.refundPoints());
            }
        });
    }
}

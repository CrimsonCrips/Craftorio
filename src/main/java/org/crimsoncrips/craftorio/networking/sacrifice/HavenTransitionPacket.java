package org.crimsoncrips.craftorio.networking.sacrifice;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.render.CraftorioHavenTransition;

public record HavenTransitionPacket(boolean instant, boolean entering) implements CustomPacketPayload {

    public static final Type<HavenTransitionPacket> TYPE = new Type<>(Craftorio.prefix("haven_transition_packet"));
    public static final StreamCodec<ByteBuf, HavenTransitionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, HavenTransitionPacket::instant,
            ByteBufCodecs.BOOL, HavenTransitionPacket::entering,
            HavenTransitionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HavenTransitionPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> CraftorioHavenTransition.start(message.instant(), message.entering()));
    }
}

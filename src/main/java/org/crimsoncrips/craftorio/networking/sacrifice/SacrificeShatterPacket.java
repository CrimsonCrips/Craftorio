package org.crimsoncrips.craftorio.networking.sacrifice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.render.CraftorioShatterEffect;

public record SacrificeShatterPacket() implements CustomPacketPayload {

    public static final Type<SacrificeShatterPacket> TYPE = new Type<>(Craftorio.prefix("sacrifice_shatter_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SacrificeShatterPacket> STREAM_CODEC = StreamCodec.unit(new SacrificeShatterPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SacrificeShatterPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(CraftorioShatterEffect::start);
    }
}

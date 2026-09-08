package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ClientEvents;

public record EffectTimerPacket(boolean enabled, int ticksRemaining) implements CustomPacketPayload {

    public static final Type<EffectTimerPacket> TYPE = new Type<>(Craftorio.prefix("effect_timer_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectTimerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EffectTimerPacket::enabled,
            ByteBufCodecs.VAR_INT, EffectTimerPacket::ticksRemaining,
            EffectTimerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EffectTimerPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.setEffectTimerDisplay(message.enabled(), message.ticksRemaining()));
    }
}

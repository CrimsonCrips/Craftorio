package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.CraftorioToastManager;

public record PunishmentToastPacket(Component message) implements CustomPacketPayload {

    public static final Type<PunishmentToastPacket> TYPE = new Type<>(Craftorio.prefix("punishment_toast_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PunishmentToastPacket> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, PunishmentToastPacket::message,
            PunishmentToastPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final long DISPLAY_TIME_MS = 5000L;

    public static void handle(PunishmentToastPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> CraftorioToastManager.addToast(message.message(), DISPLAY_TIME_MS));
    }
}

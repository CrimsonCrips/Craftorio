package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.CraftorioKeyMappings;
import org.crimsoncrips.craftorio.client.CraftorioToastManager;

public record WelcomeToastPacket() implements CustomPacketPayload {

    public static final Type<WelcomeToastPacket> TYPE = new Type<>(Craftorio.prefix("welcome_toast_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WelcomeToastPacket> STREAM_CODEC = StreamCodec.unit(new WelcomeToastPacket());

    public static final long DISPLAY_TIME_MS = 10000L;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WelcomeToastPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Component message1 = Component.translatable("misc.craftorio.welcome_toast_message",
                    CraftorioKeyMappings.OPEN_HUB.getTranslatedKeyMessage());
            CraftorioToastManager.addToast(message1, DISPLAY_TIME_MS);
        });
    }
}

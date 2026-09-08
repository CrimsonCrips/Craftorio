package org.crimsoncrips.craftorio.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.screen.ValueBrowserScreen;

public record OpenValueBrowserPacket() implements CustomPacketPayload {

    public static final Type<OpenValueBrowserPacket> TYPE = new Type<>(Craftorio.prefix("open_value_browser_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenValueBrowserPacket> STREAM_CODEC = StreamCodec.unit(new OpenValueBrowserPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenValueBrowserPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new ValueBrowserScreen()));
    }
}

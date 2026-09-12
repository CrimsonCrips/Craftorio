package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

import java.util.List;

public record OpenValueBrowserScreenPacket(boolean allUnlocked, List<ResourceLocation> unlockedItems) implements CustomPacketPayload {

    public static final Type<OpenValueBrowserScreenPacket> TYPE = new Type<>(Craftorio.prefix("open_value_browser_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenValueBrowserScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.allUnlocked,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), p -> p.unlockedItems,
            OpenValueBrowserScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenValueBrowserScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.openValueBrowserScreen(message));
    }
}

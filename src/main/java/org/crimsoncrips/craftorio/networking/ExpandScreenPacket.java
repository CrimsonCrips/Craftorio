package org.crimsoncrips.craftorio.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.BorderExpandScreen;

public record ExpandScreenPacket(boolean boolVal) implements CustomPacketPayload {


    //Thank you Drullkus
    public static final Type<ExpandScreenPacket> TYPE = new Type<>(Craftorio.prefix("expand_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExpandScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.boolVal,
            ExpandScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExpandScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new BorderExpandScreen());
        });
    }
}
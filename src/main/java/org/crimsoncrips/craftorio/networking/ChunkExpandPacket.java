package org.crimsoncrips.craftorio.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ChunkExpansionScreen;

public record ChunkExpandPacket(int amountToExpand) implements CustomPacketPayload {


    //Thank you Drullkus
    public static final Type<ChunkExpandPacket> TYPE = new Type<>(Craftorio.prefix("chunk_expand_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkExpandPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.amountToExpand,
            ChunkExpandPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChunkExpandPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new ChunkExpansionScreen());
        });
    }
}
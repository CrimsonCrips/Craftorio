package org.crimsoncrips.craftorio.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ChunkExpansionScreen;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

public record ChunkClaimPacket(boolean boolVal) implements CustomPacketPayload {


    //Thank you Drullkus
    public static final Type<ChunkClaimPacket> TYPE = new Type<>(Craftorio.prefix("chunk_claim_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkClaimPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.boolVal,
            ChunkClaimPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChunkClaimPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new ChunkExpansionScreen());
        });
    }
}
package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.List;

public record OwnLandPacket(List<ChunkPos> chunks, boolean claiming) implements CustomPacketPayload {


    //Thank you Drullkus
    public static final Type<OwnLandPacket> TYPE = new Type<>(Craftorio.prefix("own_land_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OwnLandPacket> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.CHUNK_POS.apply(ByteBufCodecs.list()), p -> p.chunks,
            ByteBufCodecs.BOOL, p -> p.claiming,
            OwnLandPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OwnLandPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CraftorioMisc.ownChunk(message.chunks,ctx.player().level(), message.claiming,ctx.player());
        });
    }
}
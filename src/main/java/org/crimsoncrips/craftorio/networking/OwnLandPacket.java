package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craft_Misc;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.List;

public record OwnLandPacket(List<ChunkPos> chunks, boolean claiming) implements CustomPacketPayload {

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
            for (ChunkPos chunkSelected : message.chunks) {
                Craft_Misc.ownLand(chunkSelected,ctx.player().level(), message.claiming,false);
            }
        });
    }
}
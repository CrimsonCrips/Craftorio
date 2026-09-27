package org.crimsoncrips.craftorio.networking.sacrifice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;

public record SacrificeIntroStartedPacket() implements CustomPacketPayload {

    public static final Type<SacrificeIntroStartedPacket> TYPE = new Type<>(Craftorio.prefix("sacrifice_intro_started_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SacrificeIntroStartedPacket> STREAM_CODEC = StreamCodec.unit(new SacrificeIntroStartedPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SacrificeIntroStartedPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                player.setData(CraftorioDataAttachments.SACRIFICE_INTRO_SEEN, true);
            }
        });
    }
}

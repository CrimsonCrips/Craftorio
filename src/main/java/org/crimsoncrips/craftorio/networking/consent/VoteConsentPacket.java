package org.crimsoncrips.craftorio.networking.consent;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.rebirth.CraftorioRebirthConsent;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;

public record VoteConsentPacket(ConsentKind kind, boolean agree) implements CustomPacketPayload {

    public static final Type<VoteConsentPacket> TYPE = new Type<>(Craftorio.prefix("vote_consent_packet"));
    public static final StreamCodec<ByteBuf, VoteConsentPacket> STREAM_CODEC = StreamCodec.composite(
            ConsentKind.STREAM_CODEC, VoteConsentPacket::kind,
            ByteBufCodecs.BOOL, VoteConsentPacket::agree,
            VoteConsentPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VoteConsentPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            switch (message.kind()) {
                case REBIRTH -> CraftorioRebirthConsent.vote(player, message.agree());
                case SACRIFICE -> CraftorioSacrifice.voteConsent(player, message.agree());
            }
        });
    }
}

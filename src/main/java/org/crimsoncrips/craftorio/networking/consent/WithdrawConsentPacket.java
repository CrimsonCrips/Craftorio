package org.crimsoncrips.craftorio.networking.consent;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.rebirth.CraftorioRebirthConsent;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;

public record WithdrawConsentPacket(ConsentKind kind) implements CustomPacketPayload {

    public static final Type<WithdrawConsentPacket> TYPE = new Type<>(Craftorio.prefix("withdraw_consent_packet"));
    public static final StreamCodec<ByteBuf, WithdrawConsentPacket> STREAM_CODEC = StreamCodec.composite(
            ConsentKind.STREAM_CODEC, WithdrawConsentPacket::kind,
            WithdrawConsentPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WithdrawConsentPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            switch (message.kind()) {
                case REBIRTH -> CraftorioRebirthConsent.withdraw(player);
                case SACRIFICE -> CraftorioSacrifice.withdrawConsent(player);
            }
        });
    }
}

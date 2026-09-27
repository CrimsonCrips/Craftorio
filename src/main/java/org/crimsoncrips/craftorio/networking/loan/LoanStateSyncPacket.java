package org.crimsoncrips.craftorio.networking.loan;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.events.ClientEvents;

import java.math.BigInteger;

public record LoanStateSyncPacket(BigInteger owed, BigInteger borrowed, boolean sacrificed, double interestPercent) implements CustomPacketPayload {

    public static final Type<LoanStateSyncPacket> TYPE = new Type<>(Craftorio.prefix("loan_state_sync_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LoanStateSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()), LoanStateSyncPacket::owed,
            ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()), LoanStateSyncPacket::borrowed,
            ByteBufCodecs.BOOL, LoanStateSyncPacket::sacrificed,
            ByteBufCodecs.DOUBLE, LoanStateSyncPacket::interestPercent,
            LoanStateSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LoanStateSyncPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.updateLoanState(message.owed(), message.borrowed(), message.sacrificed(), message.interestPercent()));
    }
}

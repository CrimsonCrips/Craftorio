package org.crimsoncrips.craftorio.networking.loan;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.loan.CraftorioLoanShark;

public record LoanSharkActionPacket(int action) implements CustomPacketPayload {

    public static final Type<LoanSharkActionPacket> TYPE = new Type<>(Craftorio.prefix("loan_shark_action_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LoanSharkActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LoanSharkActionPacket::action,
            LoanSharkActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LoanSharkActionPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                CraftorioLoanShark.handle(player, message.action());
            }
        });
    }
}

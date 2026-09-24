package org.crimsoncrips.craftorio.networking.sink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

public record CashOutDoubleOrNothingPacket() implements CustomPacketPayload {

    public static final Type<CashOutDoubleOrNothingPacket> TYPE = new Type<>(Craftorio.prefix("cash_out_double_or_nothing_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CashOutDoubleOrNothingPacket> STREAM_CODEC = StreamCodec.unit(new CashOutDoubleOrNothingPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CashOutDoubleOrNothingPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.containerMenu instanceof SinkerMenu sinkerMenu)) return;

            sinkerMenu.cashOutDoubleOrNothing();
        });
    }
}

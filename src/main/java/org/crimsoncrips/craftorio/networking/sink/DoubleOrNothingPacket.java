package org.crimsoncrips.craftorio.networking.sink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

public record DoubleOrNothingPacket(boolean forceHeads) implements CustomPacketPayload {

    public static final Type<DoubleOrNothingPacket> TYPE = new Type<>(Craftorio.prefix("double_or_nothing_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleOrNothingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DoubleOrNothingPacket::forceHeads,
            DoubleOrNothingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DoubleOrNothingPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.containerMenu instanceof SinkerMenu sinkerMenu)) return;

            boolean forceHeads = message.forceHeads() && serverPlayer.isCreative();

            Boolean heads = sinkerMenu.flipDoubleOrNothing(forceHeads);
            if (heads == null) return;

            PacketDistributor.sendToPlayer(serverPlayer, new DoubleOrNothingResultPacket(heads, sinkerMenu.getEscrowPoints(), sinkerMenu.getWinStreak(), sinkerMenu.getLastLossRefund()));
        });
    }
}

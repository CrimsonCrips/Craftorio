package org.crimsoncrips.craftorio.networking;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;

public record RefreshContractOfferPacket() implements CustomPacketPayload {

    public static final Type<RefreshContractOfferPacket> TYPE = new Type<>(Craftorio.prefix("refresh_contract_offer_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RefreshContractOfferPacket> STREAM_CODEC = StreamCodec.unit(new RefreshContractOfferPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RefreshContractOfferPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            Level level = player.level();

            BigInteger cost = CraftorioMisc.contractRefreshCost(player);

            BigInteger currentPoints = CraftorioMisc.getPoints(player);
            if (currentPoints.compareTo(cost) < 0) {
                player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
                return;
            }

            CraftorioMisc.setPoints(currentPoints.subtract(cost), player);

            if (CraftorioMisc.universalBased(level)) {
                CraftorioMisc.setContractRefreshTime(level, 0);
            } else {
                CraftorioMisc.setContractRefreshTime(player, 0);
            }

            org.crimsoncrips.craftorio.events.ServerEvents.requestInstantContractRefresh(player);
        });
    }
}

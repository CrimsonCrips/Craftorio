package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.List;

public record RequestContractOfferPacket() implements CustomPacketPayload {

    public static final Type<RequestContractOfferPacket> TYPE = new Type<>(Craftorio.prefix("request_contract_offer_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestContractOfferPacket> STREAM_CODEC = StreamCodec.unit(new RequestContractOfferPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestContractOfferPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            List<ResourceLocation> offer = CraftorioMisc.isContractOfferClaimed(serverPlayer)
                    ? List.of()
                    : CraftorioMisc.getContractOffer(serverPlayer);

            int ticksUntilRefresh = CraftorioMisc.universalBased(serverPlayer.level())
                    ? CraftorioMisc.getContractRefreshTime(serverPlayer.level())
                    : CraftorioMisc.getContractRefreshTime(serverPlayer);

            PacketDistributor.sendToPlayer(serverPlayer, new OpenContractOfferScreenPacket(offer, ticksUntilRefresh));
        });
    }
}

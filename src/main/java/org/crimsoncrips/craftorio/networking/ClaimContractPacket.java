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

public record ClaimContractPacket(ResourceLocation contractId) implements CustomPacketPayload {

    public static final Type<ClaimContractPacket> TYPE = new Type<>(Craftorio.prefix("claim_contract_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimContractPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ClaimContractPacket::contractId,
            ClaimContractPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClaimContractPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            if (CraftorioMisc.isContractOfferClaimed(serverPlayer)) return;

            List<ResourceLocation> offer = CraftorioMisc.getContractOffer(serverPlayer);
            if (!offer.contains(message.contractId())) return;

            CraftorioMisc.setContractOfferClaimed(serverPlayer, true);
            CraftorioMisc.grantContract(serverPlayer, message.contractId());

            if (CraftorioMisc.universalBased(serverPlayer.level())) {
                for (ServerPlayer other : serverPlayer.serverLevel().players()) {
                    PacketDistributor.sendToPlayer(other, new ContractOfferStatusPacket(false));
                }
            } else {
                PacketDistributor.sendToPlayer(serverPlayer, new ContractOfferStatusPacket(false));
            }
        });
    }
}

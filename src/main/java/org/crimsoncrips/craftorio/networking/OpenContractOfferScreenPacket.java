package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

import java.util.List;

public record OpenContractOfferScreenPacket(List<ResourceLocation> contractIds, int ticksUntilRefresh) implements CustomPacketPayload {

    public static final Type<OpenContractOfferScreenPacket> TYPE = new Type<>(Craftorio.prefix("open_contract_offer_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenContractOfferScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenContractOfferScreenPacket::contractIds,
            ByteBufCodecs.VAR_INT, OpenContractOfferScreenPacket::ticksUntilRefresh,
            OpenContractOfferScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenContractOfferScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.openContractOfferScreen(message));
    }
}

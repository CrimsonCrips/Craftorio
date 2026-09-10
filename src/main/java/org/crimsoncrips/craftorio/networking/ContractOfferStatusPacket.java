package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ClientContractOfferState;
import org.crimsoncrips.craftorio.client.CraftorioToastManager;

public record ContractOfferStatusPacket(boolean available) implements CustomPacketPayload {

    public static final Type<ContractOfferStatusPacket> TYPE = new Type<>(Craftorio.prefix("contract_offer_status_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ContractOfferStatusPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ContractOfferStatusPacket::available,
            ContractOfferStatusPacket::new
    );

    public static final long NEW_CONTRACTS_TOAST_DISPLAY_TIME_MS = 30000L;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ContractOfferStatusPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientContractOfferState.setAvailable(message.available());

            if (message.available()) {
                CraftorioToastManager.addToast(Component.translatable("misc.craftorio.new_contracts_toast"), NEW_CONTRACTS_TOAST_DISPLAY_TIME_MS);
            }
        });
    }
}

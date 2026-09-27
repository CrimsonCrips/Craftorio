package org.crimsoncrips.craftorio.networking.contract;

import org.crimsoncrips.craftorio.client.screen.contract.ContractRevealScreen;
import org.crimsoncrips.craftorio.client.screen.contract.ContractDetailsScreen;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.hud.CraftorioToastManager;
import org.crimsoncrips.craftorio.client.state.ClientContractOfferState;

public record ContractOfferStatusPacket(boolean available, boolean refreshOpenScreen) implements CustomPacketPayload {

    public static final Type<ContractOfferStatusPacket> TYPE = new Type<>(Craftorio.prefix("contract_offer_status_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ContractOfferStatusPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ContractOfferStatusPacket::available,
            ByteBufCodecs.BOOL, ContractOfferStatusPacket::refreshOpenScreen,
            ContractOfferStatusPacket::new
    );

    public ContractOfferStatusPacket(boolean available) {
        this(available, true);
    }

    public static final long NEW_CONTRACTS_TOAST_DISPLAY_TIME_MS = 30000L;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ContractOfferStatusPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientContractOfferState.setAvailable(message.available());

            Screen screen = Minecraft.getInstance().screen;
            boolean viewingOffer = screen instanceof ContractRevealScreen
                    || (screen instanceof ContractDetailsScreen details && details.parent() instanceof ContractRevealScreen);
            if (viewingOffer && message.refreshOpenScreen()) {
                PacketDistributor.sendToServer(new RequestContractOfferPacket());
                return;
            }
            if (!message.available()) return;

            CraftorioToastManager.addToast(Component.translatable("misc.craftorio.new_contracts_toast"), NEW_CONTRACTS_TOAST_DISPLAY_TIME_MS);
        });
    }
}

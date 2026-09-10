package org.crimsoncrips.craftorio.networking;


import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.crimsoncrips.craftorio.Craftorio;

public class PacketRegistration {

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Craftorio.MODID).versioned("1.0.0").optional();
        registrar.playToServer(OwnLandPacket.TYPE, OwnLandPacket.STREAM_CODEC, OwnLandPacket::handle);
        registrar.playToServer(SinkItemsPacket.TYPE, SinkItemsPacket.STREAM_CODEC, SinkItemsPacket::handle);
        registrar.playToServer(BorderExpandPacket.TYPE, BorderExpandPacket.STREAM_CODEC, BorderExpandPacket::handle);
        registrar.playToServer(ShopPurchasePacket.TYPE, ShopPurchasePacket.STREAM_CODEC, ShopPurchasePacket::handle);
        registrar.playToServer(ClaimItemPurchasePacket.TYPE, ClaimItemPurchasePacket.STREAM_CODEC, ClaimItemPurchasePacket::handle);
        registrar.playToServer(RequestOpenShopPacket.TYPE, RequestOpenShopPacket.STREAM_CODEC, RequestOpenShopPacket::handle);
        registrar.playToServer(RequestContractOfferPacket.TYPE, RequestContractOfferPacket.STREAM_CODEC, RequestContractOfferPacket::handle);
        registrar.playToServer(ClaimContractPacket.TYPE, ClaimContractPacket.STREAM_CODEC, ClaimContractPacket::handle);
        registrar.playToServer(AbandonContractPacket.TYPE, AbandonContractPacket.STREAM_CODEC, AbandonContractPacket::handle);
        registrar.playToServer(ForceContractRefreshPacket.TYPE, ForceContractRefreshPacket.STREAM_CODEC, ForceContractRefreshPacket::handle);
        registrar.playToServer(SetAutoSinkerThresholdPacket.TYPE, SetAutoSinkerThresholdPacket.STREAM_CODEC, SetAutoSinkerThresholdPacket::handle);

        registrar.playToClient(OpenShopScreenPacket.TYPE, OpenShopScreenPacket.STREAM_CODEC, OpenShopScreenPacket::handle);
        registrar.playToClient(EffectTimerPacket.TYPE, EffectTimerPacket.STREAM_CODEC, EffectTimerPacket::handle);
        registrar.playToClient(OpenContractOfferScreenPacket.TYPE, OpenContractOfferScreenPacket.STREAM_CODEC, OpenContractOfferScreenPacket::handle);
        registrar.playToClient(PunishmentToastPacket.TYPE, PunishmentToastPacket.STREAM_CODEC, PunishmentToastPacket::handle);
        registrar.playToClient(WelcomeToastPacket.TYPE, WelcomeToastPacket.STREAM_CODEC, WelcomeToastPacket::handle);
        registrar.playToClient(ContractOfferStatusPacket.TYPE, ContractOfferStatusPacket.STREAM_CODEC, ContractOfferStatusPacket::handle);
    }
}
package org.crimsoncrips.craftorio.networking;


import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.crimsoncrips.craftorio.Craftorio;

public class PacketRegistration {

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Craftorio.MODID).versioned("1.0.0").optional();
        registrar.playToServer(OwnLandPacket.TYPE, OwnLandPacket.STREAM_CODEC, OwnLandPacket::handle);
        registrar.playToServer(SinkItemsPacket.TYPE, SinkItemsPacket.STREAM_CODEC, SinkItemsPacket::handle);
        registrar.playToServer(DoubleOrNothingPacket.TYPE, DoubleOrNothingPacket.STREAM_CODEC, DoubleOrNothingPacket::handle);
        registrar.playToServer(CashOutDoubleOrNothingPacket.TYPE, CashOutDoubleOrNothingPacket.STREAM_CODEC, CashOutDoubleOrNothingPacket::handle);
        registrar.playToServer(BorderExpandPacket.TYPE, BorderExpandPacket.STREAM_CODEC, BorderExpandPacket::handle);
        registrar.playToServer(ShopPurchasePacket.TYPE, ShopPurchasePacket.STREAM_CODEC, ShopPurchasePacket::handle);
        registrar.playToServer(ClaimItemPurchasePacket.TYPE, ClaimItemPurchasePacket.STREAM_CODEC, ClaimItemPurchasePacket::handle);
        registrar.playToServer(RequestOpenShopPacket.TYPE, RequestOpenShopPacket.STREAM_CODEC, RequestOpenShopPacket::handle);
        registrar.playToServer(RequestContractOfferPacket.TYPE, RequestContractOfferPacket.STREAM_CODEC, RequestContractOfferPacket::handle);
        registrar.playToServer(ClaimContractPacket.TYPE, ClaimContractPacket.STREAM_CODEC, ClaimContractPacket::handle);
        registrar.playToServer(AbandonContractPacket.TYPE, AbandonContractPacket.STREAM_CODEC, AbandonContractPacket::handle);
        registrar.playToServer(ForceCompleteContractPacket.TYPE, ForceCompleteContractPacket.STREAM_CODEC, ForceCompleteContractPacket::handle);
        registrar.playToServer(ForceContractRefreshPacket.TYPE, ForceContractRefreshPacket.STREAM_CODEC, ForceContractRefreshPacket::handle);
        registrar.playToServer(RefreshContractOfferPacket.TYPE, RefreshContractOfferPacket.STREAM_CODEC, RefreshContractOfferPacket::handle);
        registrar.playToServer(UnlockUpgradePacket.TYPE, UnlockUpgradePacket.STREAM_CODEC, UnlockUpgradePacket::handle);
        registrar.playToServer(SetAutoSinkerThresholdPacket.TYPE, SetAutoSinkerThresholdPacket.STREAM_CODEC, SetAutoSinkerThresholdPacket::handle);
        registrar.playToServer(SetAutoSinkerOwnerPacket.TYPE, SetAutoSinkerOwnerPacket.STREAM_CODEC, SetAutoSinkerOwnerPacket::handle);
        registrar.playToServer(PrintScanPacket.TYPE, PrintScanPacket.STREAM_CODEC, PrintScanPacket::handle);
        registrar.playToServer(CondenseValuePacket.TYPE, CondenseValuePacket.STREAM_CODEC, CondenseValuePacket::handle);
        registrar.playToServer(RequestOpenValueBrowserPacket.TYPE, RequestOpenValueBrowserPacket.STREAM_CODEC, RequestOpenValueBrowserPacket::handle);
        registrar.playToServer(GiveScannerStickPacket.TYPE, GiveScannerStickPacket.STREAM_CODEC, GiveScannerStickPacket::handle);
        registrar.playToServer(OpenContractCreatorPacket.TYPE, OpenContractCreatorPacket.STREAM_CODEC, OpenContractCreatorPacket::handle);
        registrar.playToServer(GenerateContractCodePacket.TYPE, GenerateContractCodePacket.STREAM_CODEC, GenerateContractCodePacket::handle);
        registrar.playToServer(GenerateEffectCodePacket.TYPE, GenerateEffectCodePacket.STREAM_CODEC, GenerateEffectCodePacket::handle);
        registrar.playToServer(GenerateUpgradeCodePacket.TYPE, GenerateUpgradeCodePacket.STREAM_CODEC, GenerateUpgradeCodePacket::handle);
        registrar.playToServer(GenerateSkillTreeCodePacket.TYPE, GenerateSkillTreeCodePacket.STREAM_CODEC, GenerateSkillTreeCodePacket::handle);
        registrar.playToServer(CopyInventoryToContractCreatorPacket.TYPE, CopyInventoryToContractCreatorPacket.STREAM_CODEC, CopyInventoryToContractCreatorPacket::handle);
        registrar.playToServer(AddItemToContractCreatorPacket.TYPE, AddItemToContractCreatorPacket.STREAM_CODEC, AddItemToContractCreatorPacket::handle);
        registrar.playToServer(ClearContractCreatorGridPacket.TYPE, ClearContractCreatorGridPacket.STREAM_CODEC, ClearContractCreatorGridPacket::handle);
        registrar.playToServer(SetContractCreatorViewPacket.TYPE, SetContractCreatorViewPacket.STREAM_CODEC, SetContractCreatorViewPacket::handle);
        registrar.playToServer(ClearEffectsPacket.TYPE, ClearEffectsPacket.STREAM_CODEC, ClearEffectsPacket::handle);
        registrar.playToServer(RequestRebirthPacket.TYPE, RequestRebirthPacket.STREAM_CODEC, RequestRebirthPacket::handle);
        registrar.playToServer(UnlockRebirthUpgradePacket.TYPE, UnlockRebirthUpgradePacket.STREAM_CODEC, UnlockRebirthUpgradePacket::handle);

        registrar.playToClient(OpenShopScreenPacket.TYPE, OpenShopScreenPacket.STREAM_CODEC, OpenShopScreenPacket::handle);
        registrar.playToClient(OpenValueBrowserScreenPacket.TYPE, OpenValueBrowserScreenPacket.STREAM_CODEC, OpenValueBrowserScreenPacket::handle);
        registrar.playToClient(ItemDiscoveredPacket.TYPE, ItemDiscoveredPacket.STREAM_CODEC, ItemDiscoveredPacket::handle);
        registrar.playToClient(EffectTimerPacket.TYPE, EffectTimerPacket.STREAM_CODEC, EffectTimerPacket::handle);
        registrar.playToClient(OpenContractOfferScreenPacket.TYPE, OpenContractOfferScreenPacket.STREAM_CODEC, OpenContractOfferScreenPacket::handle);
        registrar.playToClient(PunishmentToastPacket.TYPE, PunishmentToastPacket.STREAM_CODEC, PunishmentToastPacket::handle);
        registrar.playToClient(WelcomeToastPacket.TYPE, WelcomeToastPacket.STREAM_CODEC, WelcomeToastPacket::handle);
        registrar.playToClient(ContractOfferStatusPacket.TYPE, ContractOfferStatusPacket.STREAM_CODEC, ContractOfferStatusPacket::handle);
        registrar.playToClient(ShopStatusPacket.TYPE, ShopStatusPacket.STREAM_CODEC, ShopStatusPacket::handle);
        registrar.playToClient(UniversalStateSyncPacket.TYPE, UniversalStateSyncPacket.STREAM_CODEC, UniversalStateSyncPacket::handle);
        registrar.playToClient(DoubleOrNothingResultPacket.TYPE, DoubleOrNothingResultPacket.STREAM_CODEC, DoubleOrNothingResultPacket::handle);
        registrar.playToClient(SkillTreeGenerateResultPacket.TYPE, SkillTreeGenerateResultPacket.STREAM_CODEC, SkillTreeGenerateResultPacket::handle);
        registrar.playToClient(UnlockUpgradeFailedPacket.TYPE, UnlockUpgradeFailedPacket.STREAM_CODEC, UnlockUpgradeFailedPacket::handle);
        registrar.playToClient(OpenRebirthSkillTreeScreenPacket.TYPE, OpenRebirthSkillTreeScreenPacket.STREAM_CODEC, OpenRebirthSkillTreeScreenPacket::handle);
    }
}
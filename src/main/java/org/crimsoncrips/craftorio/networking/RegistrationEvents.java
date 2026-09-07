package org.crimsoncrips.craftorio.networking;


import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.crimsoncrips.craftorio.Craftorio;

public class RegistrationEvents {

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Craftorio.MODID).versioned("1.0.0").optional();
        registrar.playToServer(OwnLandPacket.TYPE, OwnLandPacket.STREAM_CODEC, OwnLandPacket::handle);
        registrar.playToServer(SinkItemsPacket.TYPE, SinkItemsPacket.STREAM_CODEC, SinkItemsPacket::handle);
        registrar.playToServer(BorderExpandPacket.TYPE, BorderExpandPacket.STREAM_CODEC, BorderExpandPacket::handle);
        registrar.playToServer(ShopPurchasePacket.TYPE, ShopPurchasePacket.STREAM_CODEC, ShopPurchasePacket::handle);

        if (FMLEnvironment.dist.isClient()) {
            registrar.playToClient(ExpandScreenPacket.TYPE, ExpandScreenPacket.STREAM_CODEC, ExpandScreenPacket::handle);
            registrar.playToClient(OpenShopScreenPacket.TYPE, OpenShopScreenPacket.STREAM_CODEC, OpenShopScreenPacket::handle);
        }
    }
}
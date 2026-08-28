package org.crimsoncrips.craftorio.server.events;


import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.networking.BorderExpandPacket;
import org.crimsoncrips.craftorio.networking.ExpandScreenPacket;
import org.crimsoncrips.craftorio.networking.OwnLandPacket;
import org.crimsoncrips.craftorio.networking.SinkItemsPacket;

public class RegistrationEvents {

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Craftorio.MODID).versioned("1.0.0").optional();
        registrar.playToServer(OwnLandPacket.TYPE, OwnLandPacket.STREAM_CODEC, OwnLandPacket::handle);
        registrar.playToServer(SinkItemsPacket.TYPE, SinkItemsPacket.STREAM_CODEC, SinkItemsPacket::handle);
        registrar.playToClient(ExpandScreenPacket.TYPE, ExpandScreenPacket.STREAM_CODEC, ExpandScreenPacket::handle);
        registrar.playBidirectional(BorderExpandPacket.TYPE, BorderExpandPacket.STREAM_CODEC, BorderExpandPacket::handle);
    }
}
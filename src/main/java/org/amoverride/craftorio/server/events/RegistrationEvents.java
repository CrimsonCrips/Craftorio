package org.amoverride.craftorio.server.events;


import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.amoverride.craftorio.Craftorio;
import org.amoverride.craftorio.networking.OwnLandPacket;

public class RegistrationEvents {

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Craftorio.MODID).versioned("1.0.0").optional();
        registrar.playToServer(OwnLandPacket.TYPE, OwnLandPacket.STREAM_CODEC, OwnLandPacket::handle);
    }
}
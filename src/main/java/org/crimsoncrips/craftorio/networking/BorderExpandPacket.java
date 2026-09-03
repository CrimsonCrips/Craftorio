package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

public record BorderExpandPacket(long amountToExpand, boolean claiming) implements CustomPacketPayload {


    //Thank you Drullkus
    public static final Type<BorderExpandPacket> TYPE = new Type<>(Craftorio.prefix("chunk_expand_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BorderExpandPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, p -> p.amountToExpand,
            ByteBufCodecs.BOOL, p -> p.claiming,
            BorderExpandPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BorderExpandPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CraftorioMisc.expandBorder(message.amountToExpand,ctx.player().level(), message.claiming,ctx.player());
        });
    }
}
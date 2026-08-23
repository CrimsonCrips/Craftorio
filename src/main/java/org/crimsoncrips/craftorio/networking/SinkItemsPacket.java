package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

public record SinkItemsPacket(boolean sink) implements CustomPacketPayload {


    //Thank you Drullkus
    public static final Type<SinkItemsPacket> TYPE = new Type<>(Craftorio.prefix("sink_items_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SinkItemsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.sink,
            SinkItemsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SinkItemsPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            AbstractContainerMenu container = ctx.player().containerMenu;

            if (container instanceof SinkerMenu sinkerMenu && message.sink) {
                sinkerMenu.sinkPoints();
            }
        });
    }
}
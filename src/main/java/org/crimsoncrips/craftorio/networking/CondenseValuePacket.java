package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.ValueCondenserMenu;

public record CondenseValuePacket(boolean condense) implements CustomPacketPayload {

    public static final Type<CondenseValuePacket> TYPE = new Type<>(Craftorio.prefix("condense_value_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CondenseValuePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.condense,
            CondenseValuePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CondenseValuePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            AbstractContainerMenu container = ctx.player().containerMenu;

            if (container instanceof ValueCondenserMenu valueCondenserMenu && message.condense) {
                valueCondenserMenu.condenseValue();
            }
        });
    }
}

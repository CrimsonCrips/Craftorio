package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ClientShopState;

public record ShopStatusPacket(boolean enabled) implements CustomPacketPayload {

    public static final Type<ShopStatusPacket> TYPE = new Type<>(Craftorio.prefix("shop_status_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopStatusPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ShopStatusPacket::enabled,
            ShopStatusPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShopStatusPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientShopState.setEnabled(message.enabled()));
    }
}

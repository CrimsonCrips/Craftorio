package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.CraftorioShop;

public record ShopPurchasePacket(ResourceLocation item, int quantity) implements CustomPacketPayload {

    public static final Type<ShopPurchasePacket> TYPE = new Type<>(Craftorio.prefix("shop_purchase_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopPurchasePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, p -> p.item,
            ByteBufCodecs.VAR_INT, p -> p.quantity,
            ShopPurchasePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShopPurchasePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            CraftorioShop.purchase(serverPlayer, message.item(), message.quantity());
        });
    }
}

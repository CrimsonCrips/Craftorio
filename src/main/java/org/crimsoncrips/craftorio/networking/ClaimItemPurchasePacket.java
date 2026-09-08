package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.CraftorioClaimItemShop;

public record ClaimItemPurchasePacket(int quantity) implements CustomPacketPayload {

    public static final Type<ClaimItemPurchasePacket> TYPE = new Type<>(Craftorio.prefix("claim_item_purchase_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimItemPurchasePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, p -> p.quantity,
            ClaimItemPurchasePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClaimItemPurchasePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            CraftorioClaimItemShop.purchase(serverPlayer, message.quantity());
        });
    }
}

package org.crimsoncrips.craftorio.networking.shop;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.shop.CraftorioEffectRuneShop;

public record EffectRunePurchasePacket(boolean mystery, int effectCount) implements CustomPacketPayload {

    public static final Type<EffectRunePurchasePacket> TYPE = new Type<>(Craftorio.prefix("effect_rune_purchase_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectRunePurchasePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EffectRunePurchasePacket::mystery,
            ByteBufCodecs.VAR_INT, EffectRunePurchasePacket::effectCount,
            EffectRunePurchasePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EffectRunePurchasePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            CraftorioEffectRuneShop.purchase(serverPlayer, message.effectCount(), message.mystery());
        });
    }
}

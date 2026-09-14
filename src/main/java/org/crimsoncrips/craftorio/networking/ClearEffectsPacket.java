package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.List;

public record ClearEffectsPacket() implements CustomPacketPayload {

    public static final Type<ClearEffectsPacket> TYPE = new Type<>(Craftorio.prefix("clear_effects_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearEffectsPacket> STREAM_CODEC = StreamCodec.unit(new ClearEffectsPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearEffectsPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.isCreative()) return;

            CraftorioMisc.setGeneralEffects(player, List.of());
            CraftorioMisc.setTagEffects(player, List.of());
            CraftorioMisc.setShopEffects(player, List.of());
        });
    }
}

package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

public record ForceContractRefreshPacket() implements CustomPacketPayload {

    public static final Type<ForceContractRefreshPacket> TYPE = new Type<>(Craftorio.prefix("force_contract_refresh_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForceContractRefreshPacket> STREAM_CODEC = StreamCodec.unit(new ForceContractRefreshPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ForceContractRefreshPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            CraftorioMisc.setContractRefreshTime(serverPlayer.level(), 0);
        });
    }
}

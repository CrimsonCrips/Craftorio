package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;

import java.util.List;

public record ForceCompleteContractPacket(int index) implements CustomPacketPayload {

    public static final Type<ForceCompleteContractPacket> TYPE = new Type<>(Craftorio.prefix("force_complete_contract_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForceCompleteContractPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ForceCompleteContractPacket::index,
            ForceCompleteContractPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ForceCompleteContractPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            List<CraftorioContract> contracts = CraftorioMisc.getCraftorioContracts(serverPlayer);
            if (message.index() < 0 || message.index() >= contracts.size()) return;

            CraftorioContract contract = contracts.get(message.index());
            if (contract.isAbandoned()) return;

            contract.forceComplete(serverPlayer);
        });
    }
}

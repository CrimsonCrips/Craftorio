package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;

import java.util.List;

public record AbandonContractPacket(int index) implements CustomPacketPayload {

    public static final Type<AbandonContractPacket> TYPE = new Type<>(Craftorio.prefix("abandon_contract_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbandonContractPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AbandonContractPacket::index,
            AbandonContractPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AbandonContractPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            List<CraftorioShipmentContract> contracts = CraftorioMisc.getCraftorioContracts(serverPlayer);
            if (message.index() < 0 || message.index() >= contracts.size()) return;

            CraftorioShipmentContract contract = contracts.get(message.index());
            if (contract.isAbandoned()) return;

            contract.abandon();
            CraftorioMisc.setCraftorioContracts(serverPlayer, contracts);
        });
    }
}

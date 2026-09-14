package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;

public record ClearContractCreatorGridPacket() implements CustomPacketPayload {

    public static final Type<ClearContractCreatorGridPacket> TYPE = new Type<>(Craftorio.prefix("clear_contract_creator_grid_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearContractCreatorGridPacket> STREAM_CODEC = StreamCodec.unit(new ClearContractCreatorGridPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearContractCreatorGridPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;
            if (!(serverPlayer.containerMenu instanceof ContractCreatorMenu menu)) return;

            menu.clearActiveGrid();
        });
    }
}

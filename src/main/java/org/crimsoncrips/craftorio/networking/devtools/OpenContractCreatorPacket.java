package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;

public record OpenContractCreatorPacket() implements CustomPacketPayload {

    public static final Type<OpenContractCreatorPacket> TYPE = new Type<>(Craftorio.prefix("open_contract_creator_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenContractCreatorPacket> STREAM_CODEC = StreamCodec.unit(new OpenContractCreatorPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenContractCreatorPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, player) -> ContractCreatorMenu.serverContractCreatorMenu(containerId, inventory),
                    Component.translatable("misc.craftorio.contract_creator_title")
            ));
        });
    }
}

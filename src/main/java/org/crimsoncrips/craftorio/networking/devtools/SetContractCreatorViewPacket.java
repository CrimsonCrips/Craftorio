package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;

public record SetContractCreatorViewPacket(boolean reward) implements CustomPacketPayload {

    public static final Type<SetContractCreatorViewPacket> TYPE = new Type<>(Craftorio.prefix("set_contract_creator_view_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetContractCreatorViewPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SetContractCreatorViewPacket::reward,
            SetContractCreatorViewPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetContractCreatorViewPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.containerMenu instanceof ContractCreatorMenu menu)) return;

            if (message.reward()) {
                menu.showRewardView();
            } else {
                menu.showBountyView();
            }
        });
    }
}

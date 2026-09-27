package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.structure.StructureWandActions;
import org.crimsoncrips.craftorio.item.structure.StructureWandItem;
import org.crimsoncrips.craftorio.item.structure.StructureWandSettings;

public record StructureWandActionPacket(InteractionHand hand, int action, StructureWandSettings settings) implements CustomPacketPayload {

    public static final Type<StructureWandActionPacket> TYPE = new Type<>(Craftorio.prefix("structure_wand_action_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StructureWandActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> InteractionHand.values()[index], InteractionHand::ordinal), StructureWandActionPacket::hand,
            ByteBufCodecs.VAR_INT, StructureWandActionPacket::action,
            StructureWandSettings.STREAM_CODEC, StructureWandActionPacket::settings,
            StructureWandActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StructureWandActionPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ItemStack stack = player.getItemInHand(message.hand());
            if (!(stack.getItem() instanceof StructureWandItem)) return;

            StructureWandActions.handle(player, stack, message.action(), message.settings());
        });
    }
}

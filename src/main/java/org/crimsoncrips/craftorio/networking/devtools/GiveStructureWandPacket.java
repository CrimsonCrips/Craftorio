package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.CraftorioItems;

public record GiveStructureWandPacket() implements CustomPacketPayload {

    public static final Type<GiveStructureWandPacket> TYPE = new Type<>(Craftorio.prefix("give_structure_wand_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GiveStructureWandPacket> STREAM_CODEC = StreamCodec.unit(new GiveStructureWandPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GiveStructureWandPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            serverPlayer.addItem(CraftorioItems.STRUCTURE_WAND.get().getDefaultInstance());
        });
    }
}

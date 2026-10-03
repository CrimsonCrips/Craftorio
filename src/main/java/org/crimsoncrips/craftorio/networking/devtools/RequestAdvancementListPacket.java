package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record RequestAdvancementListPacket() implements CustomPacketPayload {

    public static final Type<RequestAdvancementListPacket> TYPE = new Type<>(Craftorio.prefix("request_advancement_list_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestAdvancementListPacket> STREAM_CODEC = StreamCodec.unit(new RequestAdvancementListPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestAdvancementListPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            List<AdvancementListPacket.Entry> entries = new ArrayList<>();
            for (AdvancementHolder holder : serverPlayer.server.getAdvancements().getAllAdvancements()) {
                holder.value().display().ifPresent(display ->
                        entries.add(new AdvancementListPacket.Entry(holder.id(), display.getTitle(), display.getIcon())));
            }
            entries.sort(Comparator.comparing(entry -> entry.id().toString()));

            PacketDistributor.sendToPlayer(serverPlayer, new AdvancementListPacket(entries));
        });
    }
}

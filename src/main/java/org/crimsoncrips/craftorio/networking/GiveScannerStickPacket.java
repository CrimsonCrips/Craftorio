package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.CraftorioItems;

public record GiveScannerStickPacket() implements CustomPacketPayload {

    public static final Type<GiveScannerStickPacket> TYPE = new Type<>(Craftorio.prefix("give_scanner_stick_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GiveScannerStickPacket> STREAM_CODEC = StreamCodec.unit(new GiveScannerStickPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GiveScannerStickPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            serverPlayer.addItem(CraftorioItems.SCANNER_STICK.get().getDefaultInstance());
        });
    }
}

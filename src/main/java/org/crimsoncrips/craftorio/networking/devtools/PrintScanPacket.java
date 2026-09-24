package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.ScannerStickItem;

public record PrintScanPacket() implements CustomPacketPayload {

    public static final Type<PrintScanPacket> TYPE = new Type<>(Craftorio.prefix("print_scan_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PrintScanPacket> STREAM_CODEC = StreamCodec.unit(new PrintScanPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PrintScanPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            ItemStack mainHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHand = serverPlayer.getItemInHand(InteractionHand.OFF_HAND);

            if (mainHand.getItem() instanceof ScannerStickItem) {
                ScannerStickItem.scanAndWriteFile(serverPlayer, mainHand);
            } else if (offHand.getItem() instanceof ScannerStickItem) {
                ScannerStickItem.scanAndWriteFile(serverPlayer, offHand);
            }
        });
    }
}

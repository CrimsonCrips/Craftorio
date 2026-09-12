package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.AreaScannerMenu;

public record ScanAreaPacket() implements CustomPacketPayload {

    public static final Type<ScanAreaPacket> TYPE = new Type<>(Craftorio.prefix("scan_area_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanAreaPacket> STREAM_CODEC = StreamCodec.unit(new ScanAreaPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ScanAreaPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof AreaScannerMenu areaScannerMenu) {
                areaScannerMenu.scan(player);
            }
        });
    }
}

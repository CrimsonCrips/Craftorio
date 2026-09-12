package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.AreaScannerMenu;

public record SetAreaScannerRegionPacket(int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ) implements CustomPacketPayload {

    public static final Type<SetAreaScannerRegionPacket> TYPE = new Type<>(Craftorio.prefix("set_area_scanner_region_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetAreaScannerRegionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetAreaScannerRegionPacket::offsetX,
            ByteBufCodecs.VAR_INT, SetAreaScannerRegionPacket::offsetY,
            ByteBufCodecs.VAR_INT, SetAreaScannerRegionPacket::offsetZ,
            ByteBufCodecs.VAR_INT, SetAreaScannerRegionPacket::sizeX,
            ByteBufCodecs.VAR_INT, SetAreaScannerRegionPacket::sizeY,
            ByteBufCodecs.VAR_INT, SetAreaScannerRegionPacket::sizeZ,
            SetAreaScannerRegionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetAreaScannerRegionPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            AbstractContainerMenu menu = ctx.player().containerMenu;
            if (menu instanceof AreaScannerMenu areaScannerMenu) {
                areaScannerMenu.updateRegion(message.offsetX(), message.offsetY(), message.offsetZ(), message.sizeX(), message.sizeY(), message.sizeZ());
            }
        });
    }
}

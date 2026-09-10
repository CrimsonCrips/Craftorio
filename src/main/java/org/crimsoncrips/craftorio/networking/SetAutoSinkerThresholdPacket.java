package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.AutoSinkerMenu;

public record SetAutoSinkerThresholdPacket(int percent) implements CustomPacketPayload {

    public static final Type<SetAutoSinkerThresholdPacket> TYPE = new Type<>(Craftorio.prefix("set_auto_sinker_threshold_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetAutoSinkerThresholdPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetAutoSinkerThresholdPacket::percent,
            SetAutoSinkerThresholdPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetAutoSinkerThresholdPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            AbstractContainerMenu menu = ctx.player().containerMenu;
            if (menu instanceof AutoSinkerMenu autoSinkerMenu) {
                autoSinkerMenu.setThresholdPercent(message.percent());
            }
        });
    }
}

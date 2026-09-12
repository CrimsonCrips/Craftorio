package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.AutoSinkerMenu;

public record SetAutoSinkerOwnerPacket() implements CustomPacketPayload {

    public static final Type<SetAutoSinkerOwnerPacket> TYPE = new Type<>(Craftorio.prefix("set_auto_sinker_owner_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetAutoSinkerOwnerPacket> STREAM_CODEC = StreamCodec.unit(new SetAutoSinkerOwnerPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetAutoSinkerOwnerPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof AutoSinkerMenu autoSinkerMenu) {
                autoSinkerMenu.setOwnerToSelf(player);
            }
        });
    }
}

package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

public record UnlockUpgradeFailedPacket(String messageKey) implements CustomPacketPayload {

    public static final Type<UnlockUpgradeFailedPacket> TYPE = new Type<>(Craftorio.prefix("unlock_upgrade_failed_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockUpgradeFailedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UnlockUpgradeFailedPacket::messageKey,
            UnlockUpgradeFailedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockUpgradeFailedPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.handleUnlockUpgradeFailed(message));
    }
}

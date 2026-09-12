package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ClientUnlockedItemsState;

import java.util.List;

public record UnlockedItemsSyncPacket(List<ResourceLocation> unlockedItems) implements CustomPacketPayload {

    public static final Type<UnlockedItemsSyncPacket> TYPE = new Type<>(Craftorio.prefix("unlocked_items_sync_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockedItemsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), UnlockedItemsSyncPacket::unlockedItems,
            UnlockedItemsSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockedItemsSyncPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientUnlockedItemsState.set(message.unlockedItems()));
    }
}

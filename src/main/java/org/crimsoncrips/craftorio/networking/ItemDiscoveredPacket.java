package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.ClientUnlockedItemsState;
import org.crimsoncrips.craftorio.client.ItemDiscoveredPopup;

public record ItemDiscoveredPacket(ResourceLocation itemId) implements CustomPacketPayload {

    public static final Type<ItemDiscoveredPacket> TYPE = new Type<>(Craftorio.prefix("item_discovered_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemDiscoveredPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ItemDiscoveredPacket::itemId,
            ItemDiscoveredPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ItemDiscoveredPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientUnlockedItemsState.add(message.itemId());
            ItemDiscoveredPopup.spawn(message.itemId());
        });
    }
}

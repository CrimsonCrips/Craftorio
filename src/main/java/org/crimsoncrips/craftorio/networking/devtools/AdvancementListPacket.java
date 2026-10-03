package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

import java.util.List;

public record AdvancementListPacket(List<Entry> advancements) implements CustomPacketPayload {

    public record Entry(ResourceLocation id, Component title, ItemStack icon) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::id,
                ComponentSerialization.TRUSTED_STREAM_CODEC, Entry::title,
                ItemStack.OPTIONAL_STREAM_CODEC, Entry::icon,
                Entry::new
        );
    }

    public static final Type<AdvancementListPacket> TYPE = new Type<>(Craftorio.prefix("advancement_list_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancementListPacket> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), AdvancementListPacket::advancements,
            AdvancementListPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AdvancementListPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.handleAdvancementList(message));
    }
}

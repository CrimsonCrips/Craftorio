package org.crimsoncrips.craftorio.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.screen.ShopScreen;

import java.util.HashSet;
import java.util.List;

public record OpenShopScreenPacket(boolean allUnlocked, List<ResourceLocation> unlockedItems) implements CustomPacketPayload {

    public static final Type<OpenShopScreenPacket> TYPE = new Type<>(Craftorio.prefix("open_shop_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenShopScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.allUnlocked,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), p -> p.unlockedItems,
            OpenShopScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenShopScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(
                new ShopScreen(message.allUnlocked(), new HashSet<>(message.unlockedItems()))
        ));
    }
}

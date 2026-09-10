package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.CraftorioShop;
import org.crimsoncrips.craftorio.server.CraftorioShopMode;

import java.util.ArrayList;
import java.util.List;

public record RequestOpenShopPacket() implements CustomPacketPayload {

    public static final Type<RequestOpenShopPacket> TYPE = new Type<>(Craftorio.prefix("request_open_shop_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenShopPacket> STREAM_CODEC = StreamCodec.unit(new RequestOpenShopPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestOpenShopPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

            boolean shopEnabled = CraftorioShop.isEnabled();
            PacketDistributor.sendToPlayer(serverPlayer, new ShopStatusPacket(shopEnabled));
            if (!shopEnabled) return;

            boolean allUnlocked = Craftorio.SERVER_CONFIG.SHOP_MODE.get() == CraftorioShopMode.OPEN;
            List<ResourceLocation> unlocked = allUnlocked ? List.of() : new ArrayList<>(Craftorio.UNLOCKED_ITEMS.getUnlocked(serverPlayer));

            PacketDistributor.sendToPlayer(serverPlayer, new OpenShopScreenPacket(allUnlocked, unlocked));
        });
    }
}

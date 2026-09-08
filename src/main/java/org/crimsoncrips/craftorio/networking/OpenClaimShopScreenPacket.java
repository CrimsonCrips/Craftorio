package org.crimsoncrips.craftorio.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.screen.ClaimItemPurchaseScreen;

public record OpenClaimShopScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenClaimShopScreenPacket> TYPE = new Type<>(Craftorio.prefix("open_claim_shop_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenClaimShopScreenPacket> STREAM_CODEC = StreamCodec.unit(new OpenClaimShopScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenClaimShopScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new ClaimItemPurchaseScreen()));
    }
}

package org.crimsoncrips.craftorio.networking;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.CraftorioRebirth;
import org.crimsoncrips.craftorio.server.CraftorioRebirthConsent;

public record RequestRebirthPacket(int skipCount) implements CustomPacketPayload {

    public static final Type<RequestRebirthPacket> TYPE = new Type<>(Craftorio.prefix("request_rebirth_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestRebirthPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestRebirthPacket::skipCount,
            RequestRebirthPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestRebirthPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            if (CraftorioMisc.universalBased(player.level())) {
                CraftorioRebirthConsent.requestRebirth(player, message.skipCount());
                return;
            }

            boolean success = CraftorioRebirth.performRebirth(player, message.skipCount());
            if (success) {
                PacketDistributor.sendToPlayer(player, new OpenRebirthSkillTreeScreenPacket());
            } else {
                player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
            }
        });
    }
}

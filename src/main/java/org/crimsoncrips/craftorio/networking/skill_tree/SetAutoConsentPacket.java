package org.crimsoncrips.craftorio.networking.skill_tree;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.rebirth.CraftorioRebirthConsent;

public record SetAutoConsentPacket(boolean enabled) implements CustomPacketPayload {

    public static final Type<SetAutoConsentPacket> TYPE = new Type<>(Craftorio.prefix("set_auto_consent_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetAutoConsentPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SetAutoConsentPacket::enabled,
            SetAutoConsentPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetAutoConsentPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                player.setData(CraftorioDataAttachments.AUTO_CONSENT_REBIRTH, message.enabled());
                if (message.enabled()) {
                    CraftorioRebirthConsent.onAutoConsentEnabled(player);
                }
            }
        });
    }
}

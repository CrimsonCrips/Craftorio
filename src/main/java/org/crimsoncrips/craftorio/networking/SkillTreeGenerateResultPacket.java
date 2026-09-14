package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

public record SkillTreeGenerateResultPacket(boolean success, String messageKey, String arg) implements CustomPacketPayload {

    public static final Type<SkillTreeGenerateResultPacket> TYPE = new Type<>(Craftorio.prefix("skill_tree_generate_result_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeGenerateResultPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SkillTreeGenerateResultPacket::success,
            ByteBufCodecs.STRING_UTF8, SkillTreeGenerateResultPacket::messageKey,
            ByteBufCodecs.STRING_UTF8, SkillTreeGenerateResultPacket::arg,
            SkillTreeGenerateResultPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkillTreeGenerateResultPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.handleSkillTreeGenerateResult(message));
    }
}

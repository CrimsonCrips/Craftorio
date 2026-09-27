package org.crimsoncrips.craftorio.networking.sacrifice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;

public record SacrificeAnswerPacket(boolean accept, String seed, int difficulty) implements CustomPacketPayload {

    public static final int MAX_SEED_LENGTH = 32;
    public static final Type<SacrificeAnswerPacket> TYPE = new Type<>(Craftorio.prefix("sacrifice_answer_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SacrificeAnswerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SacrificeAnswerPacket::accept,
            ByteBufCodecs.stringUtf8(MAX_SEED_LENGTH), SacrificeAnswerPacket::seed,
            ByteBufCodecs.VAR_INT, SacrificeAnswerPacket::difficulty,
            SacrificeAnswerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SacrificeAnswerPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            if (message.accept()) {
                CraftorioSacrifice.accept(player, message.seed(), message.difficulty());
            } else {
                CraftorioSacrifice.refuse(player);
            }
        });
    }
}

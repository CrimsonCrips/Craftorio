package org.crimsoncrips.craftorio.networking.sacrifice;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.render.CraftorioPlayerDissolve;

public record PlayerDissolvePacket(int entityId, int stage) implements CustomPacketPayload {

    public static final int STAGE_START = 0;
    public static final int STAGE_SHATTER = 1;
    public static final int STAGE_ASSEMBLE = 2;


    public static final Type<PlayerDissolvePacket> TYPE = new Type<>(Craftorio.prefix("player_dissolve_packet"));
    public static final StreamCodec<ByteBuf, PlayerDissolvePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerDissolvePacket::entityId,
            ByteBufCodecs.VAR_INT, PlayerDissolvePacket::stage,
            PlayerDissolvePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerDissolvePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            switch (message.stage()) {
                case STAGE_SHATTER -> CraftorioPlayerDissolve.shatter(message.entityId());
                case STAGE_ASSEMBLE -> CraftorioPlayerDissolve.assemble(message.entityId());
                default -> CraftorioPlayerDissolve.start(message.entityId());
            }
        });
    }
}

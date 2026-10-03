package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.schematic.CraftorioChronosphere;

public record ChronospherePacket(double x, double y, double z, float radius, int removalTicks) implements CustomPacketPayload {

    public static final float MARGIN = 1.5F;
    public static final int APPEAR_TICKS = 30;
    public static final int HOLD_TICKS = 6;
    public static final int DISSIPATE_TICKS = 70;

    public static final Type<ChronospherePacket> TYPE = new Type<>(Craftorio.prefix("chronosphere_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChronospherePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ChronospherePacket::x,
            ByteBufCodecs.DOUBLE, ChronospherePacket::y,
            ByteBufCodecs.DOUBLE, ChronospherePacket::z,
            ByteBufCodecs.FLOAT, ChronospherePacket::radius,
            ByteBufCodecs.VAR_INT, ChronospherePacket::removalTicks,
            ChronospherePacket::new
    );

    public static ChronospherePacket around(AABB bounds, int removalTicks) {
        Vec3 center = bounds.getCenter();
        double radius = 0.5 * Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize());
        return new ChronospherePacket(center.x, center.y, center.z, (float) radius + MARGIN, removalTicks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChronospherePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> CraftorioChronosphere.add(message.x(), message.y(), message.z(), message.radius(), message.removalTicks()));
    }
}

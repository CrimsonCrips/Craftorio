package org.crimsoncrips.craftorio.networking.devtools;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.server.devtools.PointsDeterminerKind;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PointDataMapsPacket(PointsDeterminerKind kind, List<Source> sources) implements CustomPacketPayload {

    public record Source(String name, Map<String, String> values) {
        public static final StreamCodec<ByteBuf, Source> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Source::name,
                ByteBufCodecs.<ByteBuf, String, String, Map<String, String>>map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), Source::values,
                Source::new
        );
    }

    public static final Type<PointDataMapsPacket> TYPE = new Type<>(Craftorio.prefix("point_data_maps_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PointDataMapsPacket> STREAM_CODEC = StreamCodec.composite(
            PointsDeterminerKind.STREAM_CODEC, PointDataMapsPacket::kind,
            Source.STREAM_CODEC.apply(ByteBufCodecs.list()), PointDataMapsPacket::sources,
            PointDataMapsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PointDataMapsPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvents.handlePointDataMaps(message));
    }
}

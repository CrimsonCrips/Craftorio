package org.crimsoncrips.craftorio.networking.devtools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.devtools.PointsDeterminerKind;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RequestPointDataMapsPacket(PointsDeterminerKind kind) implements CustomPacketPayload {

    public static final Type<RequestPointDataMapsPacket> TYPE = new Type<>(Craftorio.prefix("request_point_data_maps_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPointDataMapsPacket> STREAM_CODEC = StreamCodec.composite(
            PointsDeterminerKind.STREAM_CODEC, RequestPointDataMapsPacket::kind,
            RequestPointDataMapsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestPointDataMapsPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            List<Resource> stack = serverPlayer.server.getResourceManager().getResourceStack(message.kind().dataMapFile());
            List<PointDataMapsPacket.Source> sources = new ArrayList<>();
            Map<String, String> merged = new LinkedHashMap<>();

            for (Resource resource : stack) {
                Map<String, String> values = new LinkedHashMap<>();
                boolean replace = false;
                try (Reader reader = resource.openAsReader()) {
                    JsonElement root = JsonParser.parseReader(reader);
                    if (!root.isJsonObject()) continue;
                    JsonObject object = root.getAsJsonObject();
                    replace = object.has("replace") && object.get("replace").isJsonPrimitive() && object.get("replace").getAsBoolean();
                    if (object.get("values") instanceof JsonObject entries) {
                        for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                            String value = readValue(entry.getValue());
                            if (value != null) {
                                values.put(entry.getKey(), value);
                            }
                        }
                    }
                } catch (IOException | RuntimeException e) {
                    Craftorio.LOGGER.warn("Failed to read data map {} from {}", message.kind().dataMapFile(), resource.sourcePackId(), e);
                    continue;
                }

                if (replace) {
                    merged.clear();
                }
                merged.putAll(values);
                sources.add(new PointDataMapsPacket.Source(resource.sourcePackId(), values));
            }

            if (sources.size() > 1) {
                sources.addFirst(new PointDataMapsPacket.Source("", merged));
            }

            PacketDistributor.sendToPlayer(serverPlayer, new PointDataMapsPacket(message.kind(), sources));
        });
    }

    private static String readValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (element instanceof JsonObject object && object.get("value") instanceof JsonElement value && value.isJsonPrimitive()) {
            return value.getAsString();
        }
        return null;
    }
}

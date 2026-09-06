package org.crimsoncrips.craftorio.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.crimsoncrips.craftorio.Craftorio;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class CraftorioAdvancementPoints extends SimpleJsonResourceReloadListener {

    private static Map<String, String> POINTS = Map.of();

    public CraftorioAdvancementPoints() {
        super(new Gson(), "data_maps/advancement");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, String> newPoints = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            if (!entry.getKey().getPath().equals("advancement_point_value")) continue;

            try {
                JsonObject root = entry.getValue().getAsJsonObject();
                JsonObject values = root.getAsJsonObject("values");
                for (Map.Entry<String, JsonElement> valueEntry : values.entrySet()) {
                    newPoints.put(valueEntry.getKey(), valueEntry.getValue().getAsString());
                }
            } catch (Exception e) {
                Craftorio.LOGGER.error("Failed to parse advancement point value file {}", entry.getKey(), e);
            }
        }

        POINTS = newPoints;
    }

    public static BigInteger getPoints(String advancementId) {
        String value = POINTS.get(advancementId);
        return value != null ? new BigDecimal(value).toBigInteger() : BigInteger.ZERO;
    }
}
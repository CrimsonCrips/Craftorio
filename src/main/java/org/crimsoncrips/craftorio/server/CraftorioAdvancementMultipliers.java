package org.crimsoncrips.craftorio.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.HashMap;
import java.util.Map;

public class CraftorioAdvancementMultipliers extends SimpleJsonResourceReloadListener {

    private static Map<String, String> MULTIPLIERS = Map.of();

    public CraftorioAdvancementMultipliers() {
        super(new Gson(), "data_maps/advancement");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, String> newMultipliers = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            if (!entry.getKey().getPath().equals("advancement_multiplier_value")) continue;

            try {
                JsonObject root = entry.getValue().getAsJsonObject();
                JsonObject values = root.getAsJsonObject("values");
                for (Map.Entry<String, JsonElement> valueEntry : values.entrySet()) {
                    newMultipliers.put(valueEntry.getKey(), valueEntry.getValue().getAsString());
                }
            } catch (Exception e) {
                Craftorio.LOGGER.error("Failed to parse advancement multiplier value file {}", entry.getKey(), e);
            }
        }

        MULTIPLIERS = newMultipliers;
    }

    public static double getMultiplier(String advancementId) {
        String value = MULTIPLIERS.get(advancementId);
        try {
            return value != null ? Double.parseDouble(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

package org.crimsoncrips.craftorio.server.loan;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftorioLoanEffects extends SimpleJsonResourceReloadListener {

    public static final String FOLDER = "loan_effects";

    private static Map<ResourceLocation, Integer> WEIGHTS = Map.of();

    public CraftorioLoanEffects() {
        super(new Gson(), FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Integer> weights = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> file : resourceList.entrySet()) {
            try {
                JsonObject effects = file.getValue().getAsJsonObject().getAsJsonObject("effects");
                for (Map.Entry<String, JsonElement> entry : effects.entrySet()) {
                    ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                    if (id == null) continue;

                    int weight = entry.getValue().getAsInt();
                    if (weight > 0) {
                        weights.put(id, weight);
                    } else {
                        weights.remove(id);
                    }
                }
            } catch (Exception e) {
                Craftorio.LOGGER.error("Failed to parse the loan effects file {}", file.getKey(), e);
            }
        }

        WEIGHTS = weights;
    }

    public static List<CraftorioEffects> pick(RegistryAccess registryAccess, RandomSource random, int count) {
        Registry<CraftorioEffects> registry = registryAccess.registryOrThrow(CraftorioEffects.REGISTRY_KEY);

        Map<ResourceLocation, Integer> pool = new HashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : WEIGHTS.entrySet()) {
            if (registry.containsKey(entry.getKey())) {
                pool.put(entry.getKey(), entry.getValue());
            }
        }

        List<CraftorioEffects> picked = new ArrayList<>();
        while (picked.size() < count && !pool.isEmpty()) {
            int total = pool.values().stream().mapToInt(Integer::intValue).sum();
            int roll = random.nextInt(total);
            ResourceLocation chosen = null;
            for (Map.Entry<ResourceLocation, Integer> entry : pool.entrySet()) {
                roll -= entry.getValue();
                if (roll < 0) {
                    chosen = entry.getKey();
                    break;
                }
            }
            if (chosen == null) break;

            pool.remove(chosen);
            CraftorioEffects effect = registry.get(chosen);
            if (effect != null) {
                picked.add(effect.copy());
            }
        }
        return picked;
    }
}

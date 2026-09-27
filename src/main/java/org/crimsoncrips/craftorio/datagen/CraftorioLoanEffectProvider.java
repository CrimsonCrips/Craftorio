package org.crimsoncrips.craftorio.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.loan.CraftorioLoanEffects;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CraftorioLoanEffectProvider implements DataProvider {

    private final PackOutput output;

    public CraftorioLoanEffectProvider(PackOutput output) {
        this.output = output;
    }

    private static Map<String, Integer> defaults() {
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("craftorio:inflated_valuables", 10);
        weights.put("craftorio:black_holdover", 4);
        weights.put("craftorio:monopolized", 8);
        weights.put("craftorio:oversupplied", 10);
        weights.put("craftorio:dense_traffic", 10);
        weights.put("craftorio:rugpulled", 8);
        weights.put("craftorio:strait_to_deficits", 10);
        weights.put("craftorio:shop/kingdom_tariff", 3);
        weights.put("craftorio:tag/copper_deficiency", 3);
        weights.put("craftorio:general/inquisitors_wrath", 3);
        return weights;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject effects = new JsonObject();
        defaults().forEach(effects::addProperty);

        JsonObject root = new JsonObject();
        root.add("effects", effects);

        Path file = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(Craftorio.MODID).resolve(CraftorioLoanEffects.FOLDER).resolve("default.json");
        return DataProvider.saveStable(cache, root, file);
    }

    @Override
    public String getName() {
        return "Craftorio Loan Effects";
    }
}

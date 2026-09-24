package org.crimsoncrips.craftorio.client.state;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientContractCreatorDraftState {

    private static final Map<String, String> VALUES = new HashMap<>();

    public static String get(String key, String fallback) {
        return VALUES.getOrDefault(key, fallback);
    }

    public static void set(String key, String value) {
        VALUES.put(key, value);
    }

    public static void clear() {
        VALUES.clear();
    }
}

package org.crimsoncrips.craftorio.server.config;

public class CraftorioWorldCreationOverrides {

    public record Pending(boolean universalProgression, boolean chunkBasedExpansion, boolean noBorders) {}

    private static Pending pending;

    private CraftorioWorldCreationOverrides() {}

    public static void set(boolean universalProgression, boolean chunkBasedExpansion, boolean noBorders) {
        pending = new Pending(universalProgression, chunkBasedExpansion, noBorders);
    }

    public static Pending consume() {
        Pending result = pending;
        pending = null;
        return result;
    }
}

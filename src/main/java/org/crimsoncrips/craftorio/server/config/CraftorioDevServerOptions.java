package org.crimsoncrips.craftorio.server.config;

import org.crimsoncrips.craftorio.Craftorio;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;

public final class CraftorioDevServerOptions {

    private static final String PREFIX = "craftorio.dev.";
    private static final String DEFAULT_LEVEL_NAME = "world";

    private CraftorioDevServerOptions() {}

    public static void deleteWorldIfRequested() {
        if (!Boolean.getBoolean(PREFIX + "deleteWorld")) return;

        Path world = Path.of(levelName());
        if (!Files.isDirectory(world)) return;

        try (Stream<Path> paths = Files.walk(world)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            Craftorio.LOGGER.info("Deleted world folder '{}' for a fresh dev server", world);
        } catch (IOException | UncheckedIOException e) {
            Craftorio.LOGGER.error("Could not delete world folder '{}'", world, e);
        }
    }

    public static CraftorioWorldCreationOverrides.Pending worldOverrides() {
        String universal = System.getProperty(PREFIX + "universal");
        String chunkBased = System.getProperty(PREFIX + "chunkBased");
        String noBorders = System.getProperty(PREFIX + "noBorders");
        if (universal == null && chunkBased == null && noBorders == null) return null;

        return new CraftorioWorldCreationOverrides.Pending(
                universal != null ? Boolean.parseBoolean(universal) : CraftorioWorldCreationOverrides.DEFAULTS.universalProgression(),
                chunkBased != null ? Boolean.parseBoolean(chunkBased) : CraftorioWorldCreationOverrides.DEFAULTS.chunkBasedExpansion(),
                noBorders != null ? Boolean.parseBoolean(noBorders) : CraftorioWorldCreationOverrides.DEFAULTS.noBorders());
    }

    private static String levelName() {
        Path properties = Path.of("server.properties");
        if (!Files.isRegularFile(properties)) return DEFAULT_LEVEL_NAME;

        Properties values = new Properties();
        try (InputStream input = Files.newInputStream(properties)) {
            values.load(input);
        } catch (IOException e) {
            return DEFAULT_LEVEL_NAME;
        }
        String name = values.getProperty("level-name", DEFAULT_LEVEL_NAME).trim();
        return name.isEmpty() ? DEFAULT_LEVEL_NAME : name;
    }
}

package org.crimsoncrips.craftorio.server.sacrifice;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.LevelResource;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public final class CraftorioWorldWipe {

    private static final String FOLDER = "craftorio";
    private static final String FILE_NAME = "wipe_cutoff.txt";
    private static final String PURGING = "purging";
    private static final String FINALIZE_FILE = "sacrifice_finalize.txt";
    private static final String AREAS_FILE = "area_wipes.txt";
    private static final String OVERWORLD_KEY = "minecraft:overworld";
    private static final String NETHER_KEY = "minecraft:the_nether";
    private static final String END_KEY = "minecraft:the_end";
    private static final String ATTACHMENTS_FILE = "neoforge_data_attachments.dat";
    private static final double DEFAULT_BORDER_SIZE = 5.9999968E7;
    private static final List<String> CHUNK_FOLDERS = List.of("region", "entities", "poi");
    private static final int REGION_HEADER_BYTES = 4096;
    private static final int REGION_CHUNKS = 1024;
    private static final String HAVEN_PATH = "/dimensions/" + CraftorioDimensions.HAVEN_ID.getNamespace() + "/" + CraftorioDimensions.HAVEN_ID.getPath() + "/";

    private static final class AreaWipe {
        volatile int cutoff;
        final List<ChunkRect> rects;
        final Map<String, List<ChunkRect>> byDimension = new HashMap<>();

        AreaWipe(int cutoff, List<ChunkRect> rects) {
            this.cutoff = cutoff;
            this.rects = rects;
            for (ChunkRect rect : rects) {
                byDimension.computeIfAbsent(rect.dimension(), key -> new ArrayList<>()).add(rect);
            }
        }

        boolean contains(String dimension, ChunkPos pos) {
            List<ChunkRect> candidates = byDimension.get(dimension);
            if (candidates == null) return false;
            for (ChunkRect rect : candidates) {
                if (rect.contains(dimension, pos.x, pos.z)) return true;
            }
            return false;
        }
    }

    private static volatile int cutoffSeconds = 0;
    private static final List<AreaWipe> areaWipes = new CopyOnWriteArrayList<>();
    private static final Map<Path, String> dimensionCache = new ConcurrentHashMap<>();
    private static AreaWipe currentArea = null;
    private static Integer savedSpawnRadius = null;
    private static Path worldRoot = null;
    private static volatile Path normalizedRoot = null;

    private CraftorioWorldWipe() {}

    public static int nowSeconds() {
        return (int) (Util.getEpochMillis() / 1000L);
    }

    public static int cutoff() {
        return cutoffSeconds;
    }

    public static boolean isStale(Path regionFilePath, IntBuffer timestamps, ChunkPos pos) {
        int cutoff = cutoffSeconds;
        boolean areas = !areaWipes.isEmpty();
        if (cutoff <= 0 && !areas) return false;
        if (isHaven(regionFilePath)) return false;

        int timestamp = timestamps.get(pos.getRegionLocalX() + pos.getRegionLocalZ() * 32);
        if (cutoff > 0 && timestamp < cutoff) return true;
        if (!areas) return false;

        String dimension = dimensionOf(regionFilePath);
        for (AreaWipe area : areaWipes) {
            if (timestamp < area.cutoff && area.contains(dimension, pos)) return true;
        }
        return false;
    }

    private static String dimensionOf(Path regionFile) {
        return dimensionCache.computeIfAbsent(regionFile, path -> {
            Path root = normalizedRoot;
            if (root == null) return "";

            Path dimensionRoot = path.toAbsolutePath().normalize().getParent().getParent();
            if (!dimensionRoot.startsWith(root)) return "";

            String relative = root.relativize(dimensionRoot).toString().replace('\\', '/');
            if (relative.isEmpty()) return OVERWORLD_KEY;
            if (relative.equals("DIM-1")) return NETHER_KEY;
            if (relative.equals("DIM1")) return END_KEY;
            if (relative.startsWith("dimensions/")) {
                String rest = relative.substring("dimensions/".length());
                int slash = rest.indexOf('/');
                if (slash > 0) return rest.substring(0, slash) + ":" + rest.substring(slash + 1);
            }
            return "";
        });
    }

    private static boolean isHaven(Path path) {
        return path.toString().replace('\\', '/').contains(HAVEN_PATH);
    }

    public static void beginPurge(MinecraftServer server, int spawnChunkRadius, List<ChunkRect> area) {
        savedSpawnRadius = spawnChunkRadius;
        if (area == null) {
            cutoffSeconds = Integer.MAX_VALUE;
            write(server, PURGING);
            return;
        }

        currentArea = new AreaWipe(Integer.MAX_VALUE, area);
        areaWipes.add(currentArea);
        write(server, String.valueOf(cutoffSeconds));
        writeAreas(server);
    }

    public static void finishPurge(MinecraftServer server, int cutoff) {
        if (currentArea != null) {
            currentArea.cutoff = cutoff;
            writeAreas(server);
            write(server, String.valueOf(cutoffSeconds));
            return;
        }

        cutoffSeconds = cutoff;
        write(server, String.valueOf(cutoff));
    }

    public static void spawnRadiusRestored(MinecraftServer server) {
        savedSpawnRadius = null;
        if (currentArea != null) {
            if (currentArea.cutoff == Integer.MAX_VALUE) {
                currentArea.cutoff = nowSeconds();
            }
            currentArea = null;
            writeAreas(server);
        }
        write(server, String.valueOf(cutoffSeconds == Integer.MAX_VALUE ? nowSeconds() : cutoffSeconds));
    }

    public static void onServerStarted(MinecraftServer server) {
        if (savedSpawnRadius == null) return;

        server.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(savedSpawnRadius, server);
        spawnRadiusRestored(server);
    }

    public static void onServerAboutToStart(MinecraftServer server) {
        worldRoot = server.getWorldPath(LevelResource.ROOT);
        normalizedRoot = worldRoot.toAbsolutePath().normalize();
        dimensionCache.clear();
        areaWipes.clear();
        currentArea = null;
        if (Files.exists(worldRoot.resolve(FOLDER).resolve(FINALIZE_FILE))) {
            finalizeOffline(worldRoot, false);
        }

        int cutoff = read(server);
        if (cutoff == Integer.MAX_VALUE) {
            cutoff = nowSeconds();
            write(server, String.valueOf(cutoff));
        }
        cutoffSeconds = cutoff;
        if (cutoff > 0) {
            deleteStaleRegionFiles(server.getWorldPath(LevelResource.ROOT), cutoff);
        }

        List<AreaWipe> leftover = readAreas(server);
        if (!leftover.isEmpty()) {
            applyAreaWipes(worldRoot, leftover);
        }
        deleteAreasFile(server.getWorldPath(LevelResource.ROOT));
    }

    public static void onServerStopped() {
        if (worldRoot != null && Files.exists(worldRoot.resolve(FOLDER).resolve(FINALIZE_FILE))) {
            finalizeOffline(worldRoot, true);
        }
        if (worldRoot != null && !areaWipes.isEmpty()) {
            applyAreaWipes(worldRoot, new ArrayList<>(areaWipes));
            deleteAreasFile(worldRoot);
        }
        cutoffSeconds = 0;
        savedSpawnRadius = null;
        areaWipes.clear();
        currentArea = null;
        dimensionCache.clear();
        normalizedRoot = null;
        worldRoot = null;
    }

    private static Path areasFile(Path root) {
        return root.resolve(FOLDER).resolve(AREAS_FILE);
    }

    private static void deleteAreasFile(Path root) {
        try {
            Files.deleteIfExists(areasFile(root));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to remove the sacrifice area wipe file", e);
        }
    }

    private static void writeAreas(MinecraftServer server) {
        Path file = areasFile(server.getWorldPath(LevelResource.ROOT));
        try {
            if (areaWipes.isEmpty()) {
                Files.deleteIfExists(file);
                return;
            }
            StringBuilder builder = new StringBuilder();
            for (AreaWipe area : areaWipes) {
                builder.append(area.cutoff == Integer.MAX_VALUE ? PURGING : String.valueOf(area.cutoff)).append('|');
                for (int i = 0; i < area.rects.size(); i++) {
                    if (i > 0) builder.append(';');
                    builder.append(area.rects.get(i).serialize());
                }
                builder.append('\n');
            }
            Files.createDirectories(file.getParent());
            Files.writeString(file, builder.toString());
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to persist the sacrifice area wipes", e);
        }
    }

    private static List<AreaWipe> readAreas(MinecraftServer server) {
        List<AreaWipe> result = new ArrayList<>();
        Path file = areasFile(server.getWorldPath(LevelResource.ROOT));
        if (!Files.exists(file)) return result;

        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split("\\|", 2);
                if (parts.length < 2 || parts[1].isBlank()) continue;

                int cutoff = parts[0].equals(PURGING) ? nowSeconds() : Integer.parseInt(parts[0]);
                List<ChunkRect> rects = new ArrayList<>();
                for (String rect : parts[1].split(";")) {
                    rects.add(ChunkRect.deserialize(rect));
                }
                result.add(new AreaWipe(cutoff, rects));
            }
        } catch (IOException | RuntimeException e) {
            Craftorio.LOGGER.error("Failed to read the sacrifice area wipes, they will be ignored", e);
        }
        return result;
    }

    private static void applyAreaWipes(Path root, List<AreaWipe> wipes) {
        int cleared = 0;
        for (AreaWipe wipe : wipes) {
            for (Map.Entry<String, List<ChunkRect>> entry : wipe.byDimension.entrySet()) {
                Path dimensionRoot = dimensionRoot(root, entry.getKey());
                if (dimensionRoot == null) continue;

                for (ChunkRect rect : entry.getValue()) {
                    for (int regionX = Math.floorDiv(rect.minX(), 32); regionX <= Math.floorDiv(rect.maxX(), 32); regionX++) {
                        for (int regionZ = Math.floorDiv(rect.minZ(), 32); regionZ <= Math.floorDiv(rect.maxZ(), 32); regionZ++) {
                            for (String folder : CHUNK_FOLDERS) {
                                Path file = dimensionRoot.resolve(folder).resolve("r." + regionX + "." + regionZ + ".mca");
                                if (Files.exists(file)) {
                                    cleared += clearRegionChunks(file, regionX, regionZ, entry.getKey(), wipe);
                                }
                            }
                        }
                    }
                }
            }
        }
        Craftorio.LOGGER.info("Sacrifice: cleared {} stale chunk entries from the reset areas", cleared);
    }

    private static Path dimensionRoot(Path root, String dimension) {
        return switch (dimension) {
            case OVERWORLD_KEY -> root;
            case NETHER_KEY -> root.resolve("DIM-1");
            case END_KEY -> root.resolve("DIM1");
            default -> {
                int colon = dimension.indexOf(':');
                yield colon <= 0 ? null : root.resolve("dimensions").resolve(dimension.substring(0, colon)).resolve(dimension.substring(colon + 1));
            }
        };
    }

    private static int clearRegionChunks(Path file, int regionX, int regionZ, String dimension, AreaWipe wipe) {
        int cleared = 0;
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            if (channel.size() < REGION_HEADER_BYTES * 2L) return 0;

            ByteBuffer header = ByteBuffer.allocate(REGION_HEADER_BYTES * 2);
            channel.read(header, 0);
            boolean changed = false;

            for (int index = 0; index < REGION_CHUNKS; index++) {
                int chunkX = regionX * 32 + index % 32;
                int chunkZ = regionZ * 32 + index / 32;
                int timestamp = header.getInt(REGION_HEADER_BYTES + index * 4);
                if (header.getInt(index * 4) == 0 && timestamp == 0) continue;
                if (timestamp >= wipe.cutoff || !wipe.contains(dimension, new ChunkPos(chunkX, chunkZ))) continue;

                header.putInt(index * 4, 0);
                header.putInt(REGION_HEADER_BYTES + index * 4, 0);
                Files.deleteIfExists(file.resolveSibling("c." + chunkX + "." + chunkZ + ".mcc"));
                changed = true;
                cleared++;
            }

            if (changed) {
                header.position(0);
                channel.write(header, 0);
            }
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to clear reset chunks from {}", file, e);
        }
        return cleared;
    }

    public static void writeFinalizeMarker(MinecraftServer server, Long seed) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve(FOLDER).resolve(FINALIZE_FILE);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, "seed=" + (seed == null ? "none" : String.valueOf(seed)));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write the sacrifice finalize marker, the world data will not be fully reset", e);
        }
    }

    private static void finalizeOffline(Path root, boolean editLevelData) {
        Path marker = root.resolve(FOLDER).resolve(FINALIZE_FILE);
        try {
            String content = Files.readString(marker).trim();
            Long seed = content.equals("seed=none") ? null : Long.parseLong(content.substring("seed=".length()));

            for (Path dimensionRoot : dimensionRoots(root)) {
                for (String folder : CHUNK_FOLDERS) {
                    deleteRecursively(dimensionRoot.resolve(folder));
                }
                deleteSavedData(dimensionRoot.resolve("data"));
            }

            if (editLevelData) {
                resetLevelData(root.resolve("level.dat"), seed);
            }
            Files.deleteIfExists(marker);
            Craftorio.LOGGER.info("Sacrifice finalized: chunk data, saved data and world data were reset{}", seed != null ? " with a new seed" : "");
        } catch (IOException | RuntimeException e) {
            Craftorio.LOGGER.error("Failed to finalize the sacrifice, it will be retried on the next start", e);
        }
    }

    private static List<Path> dimensionRoots(Path root) throws IOException {
        List<Path> roots = new ArrayList<>(List.of(root, root.resolve("DIM-1"), root.resolve("DIM1")));
        Path dimensions = root.resolve("dimensions");
        if (Files.isDirectory(dimensions)) {
            for (Path namespaceDir : listDirectories(dimensions)) {
                for (Path dimensionDir : listDirectories(namespaceDir)) {
                    if (!isHaven(dimensionDir.resolve("region"))) {
                        roots.add(dimensionDir);
                    }
                }
            }
        }
        return roots;
    }

    private static void deleteSavedData(Path dataFolder) throws IOException {
        if (!Files.isDirectory(dataFolder)) return;
        try (Stream<Path> stream = Files.list(dataFolder)) {
            for (Path entry : stream.filter(path -> !path.getFileName().toString().equals(ATTACHMENTS_FILE)).toList()) {
                deleteRecursively(entry);
            }
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (Stream<Path> stream = Files.walk(path)) {
            for (Path entry : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(entry);
            }
        }
    }

    private static void resetLevelData(Path levelDat, Long seed) throws IOException {
        if (!Files.exists(levelDat)) return;

        CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.unlimitedHeap());
        if (!root.contains("Data", Tag.TAG_COMPOUND)) return;
        CompoundTag data = root.getCompound("Data");

        if (seed != null && data.contains("WorldGenSettings", Tag.TAG_COMPOUND)) {
            data.getCompound("WorldGenSettings").putLong("seed", seed);
        }

        data.putBoolean("initialized", false);
        data.putLong("DayTime", 0L);
        data.putDouble("neoDayTimeFraction", 0.0);
        data.putBoolean("raining", false);
        data.putBoolean("thundering", false);
        data.putInt("rainTime", 0);
        data.putInt("thunderTime", 0);
        data.putInt("clearWeatherTime", 0);
        data.putInt("WanderingTraderSpawnDelay", 24000);
        data.putInt("WanderingTraderSpawnChance", 25);
        EndDragonFight.Data.CODEC.encodeStart(NbtOps.INSTANCE, EndDragonFight.Data.DEFAULT).result().ifPresentOrElse(tag -> data.put("DragonFight", tag), () -> data.remove("DragonFight"));
        data.put("CustomBossEvents", new CompoundTag());
        data.put("ScheduledEvents", new ListTag());
        data.putDouble("BorderCenterX", 0.0);
        data.putDouble("BorderCenterZ", 0.0);
        data.putDouble("BorderSize", DEFAULT_BORDER_SIZE);
        data.putDouble("BorderSizeLerpTarget", DEFAULT_BORDER_SIZE);
        data.putLong("BorderSizeLerpTime", 0L);
        data.putDouble("BorderSafeZone", 5.0);
        data.putDouble("BorderDamagePerBlock", 0.2);
        data.putDouble("BorderWarningBlocks", 5.0);
        data.putDouble("BorderWarningTime", 15.0);

        Files.copy(levelDat, levelDat.resolveSibling("level.dat_old"), StandardCopyOption.REPLACE_EXISTING);
        NbtIo.writeCompressed(root, levelDat);
    }

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve(FOLDER).resolve(FILE_NAME);
    }

    private static int read(MinecraftServer server) {
        Path file = file(server);
        if (!Files.exists(file)) return 0;
        try {
            String content = Files.readString(file).trim();
            String[] parts = content.split(";");
            if (parts.length > 1) {
                savedSpawnRadius = Integer.parseInt(parts[1]);
            }
            return parts[0].equals(PURGING) ? Integer.MAX_VALUE : Integer.parseInt(parts[0]);
        } catch (IOException | NumberFormatException e) {
            Craftorio.LOGGER.error("Failed to read the sacrifice wipe cutoff, treating the world as not wiped", e);
            return 0;
        }
    }

    private static void write(MinecraftServer server, String content) {
        Path file = file(server);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, savedSpawnRadius != null ? content + ";" + savedSpawnRadius : content);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to persist the sacrifice wipe cutoff", e);
        }
    }

    private static void deleteStaleRegionFiles(Path root, int cutoff) {
        try {
            deleteStaleIn(root, cutoff);
            deleteStaleIn(root.resolve("DIM-1"), cutoff);
            deleteStaleIn(root.resolve("DIM1"), cutoff);

            Path dimensions = root.resolve("dimensions");
            if (!Files.isDirectory(dimensions)) return;
            for (Path namespaceDir : listDirectories(dimensions)) {
                for (Path dimensionDir : listDirectories(namespaceDir)) {
                    if (!isHaven(dimensionDir.resolve("region"))) {
                        deleteStaleIn(dimensionDir, cutoff);
                    }
                }
            }
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to clean up stale region files after the sacrifice", e);
        }
    }

    private static void deleteStaleIn(Path dimensionRoot, int cutoff) throws IOException {
        for (String folder : CHUNK_FOLDERS) {
            Path directory = dimensionRoot.resolve(folder);
            if (!Files.isDirectory(directory)) continue;

            try (Stream<Path> stream = Files.list(directory)) {
                for (Path file : stream.filter(path -> path.getFileName().toString().endsWith(".mca")).toList()) {
                    if (isFullyStale(file, cutoff)) {
                        Files.deleteIfExists(file);
                    }
                }
            }
            deleteOrphanedExternalChunks(directory);
        }
    }

    private static boolean isFullyStale(Path regionFile, int cutoff) throws IOException {
        try (FileChannel channel = FileChannel.open(regionFile, StandardOpenOption.READ)) {
            if (channel.size() < REGION_HEADER_BYTES * 2L) return true;

            ByteBuffer header = ByteBuffer.allocate(REGION_HEADER_BYTES);
            channel.read(header, REGION_HEADER_BYTES);
            header.flip();

            for (int i = 0; i < REGION_CHUNKS; i++) {
                if (header.getInt() >= cutoff) return false;
            }
            return true;
        }
    }

    private static void deleteOrphanedExternalChunks(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            for (Path file : stream.filter(path -> path.getFileName().toString().endsWith(".mcc")).toList()) {
                String[] parts = file.getFileName().toString().split("\\.");
                if (parts.length != 4) continue;
                try {
                    int regionX = Math.floorDiv(Integer.parseInt(parts[1]), 32);
                    int regionZ = Math.floorDiv(Integer.parseInt(parts[2]), 32);
                    if (!Files.exists(directory.resolve("r." + regionX + "." + regionZ + ".mca"))) {
                        Files.deleteIfExists(file);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private static List<Path> listDirectories(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(Files::isDirectory).toList();
        }
    }
}

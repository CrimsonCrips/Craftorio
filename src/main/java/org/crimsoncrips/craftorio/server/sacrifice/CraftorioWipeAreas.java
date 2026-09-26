package org.crimsoncrips.craftorio.server.sacrifice;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.server.border.CraftorioBorder;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CraftorioWipeAreas {

    private static final int MAX_LOGGED_RECTS = 128;
    private static final int MAX_LOG_ENTRIES = 16;
    private static final String HAVEN = CraftorioDimensions.HAVEN_LEVEL_KEY.location().toString();

    private CraftorioWipeAreas() {}

    public static void recordOwnership(MinecraftServer server, String dimension, ChunkPos pos, String uuid, boolean owned) {
        if (server == null || HAVEN.equals(dimension)) return;

        ServerLevel overworld = server.overworld();
        Map<String, Map<String, Set<Long>>> current = overworld.getData(CraftorioDataAttachments.OWNED_CHUNK_INDEX);
        Set<Long> existing = current.getOrDefault(uuid, Map.of()).get(dimension);
        boolean present = existing != null && existing.contains(pos.toLong());
        if (present == owned) return;

        Map<String, Map<String, Set<Long>>> copy = copyIndex(current);
        Map<String, Set<Long>> byDimension = copy.computeIfAbsent(uuid, key -> new HashMap<>());
        if (owned) {
            byDimension.computeIfAbsent(dimension, key -> new HashSet<>()).add(pos.toLong());
        } else {
            Set<Long> chunks = byDimension.get(dimension);
            if (chunks != null) {
                chunks.remove(pos.toLong());
                if (chunks.isEmpty()) byDimension.remove(dimension);
            }
            if (byDimension.isEmpty()) copy.remove(uuid);
        }
        overworld.setData(CraftorioDataAttachments.OWNED_CHUNK_INDEX, copy);
    }

    public static void indexLoadedChunk(ServerLevel level, ChunkAccess chunk) {
        List<String> owners = chunk.getExistingDataOrNull(CraftorioDataAttachments.OWNED_BY);
        if (owners == null || owners.isEmpty()) return;

        String dimension = level.dimension().location().toString();
        for (String owner : owners) {
            recordOwnership(level.getServer(), dimension, chunk.getPos(), owner, true);
        }
    }

    public static void forgetChunks(MinecraftServer server, List<ChunkRect> rects, String uuid) {
        ServerLevel overworld = server.overworld();
        Map<String, Map<String, Set<Long>>> copy = copyIndex(overworld.getData(CraftorioDataAttachments.OWNED_CHUNK_INDEX));
        copy.remove(uuid);
        for (Map<String, Set<Long>> byDimension : copy.values()) {
            for (Map.Entry<String, Set<Long>> entry : byDimension.entrySet()) {
                entry.getValue().removeIf(packed -> contains(rects, entry.getKey(), ChunkPos.getX(packed), ChunkPos.getZ(packed)));
            }
        }
        overworld.setData(CraftorioDataAttachments.OWNED_CHUNK_INDEX, copy);
    }

    public static boolean contains(List<ChunkRect> rects, String dimension, int chunkX, int chunkZ) {
        for (ChunkRect rect : rects) {
            if (rect.contains(dimension, chunkX, chunkZ)) return true;
        }
        return false;
    }

    public static List<ChunkRect> forPlayer(MinecraftServer server, ServerPlayer player) {
        if (CraftorioMisc.chunkBased(server.overworld())) {
            return ownedChunkRects(server, player.getStringUUID());
        }
        return borderRects(player.getData(CraftorioDataAttachments.PLAYER_BORDERS));
    }

    private static List<ChunkRect> ownedChunkRects(MinecraftServer server, String uuid) {
        List<ChunkRect> rects = new ArrayList<>();
        Map<String, Set<Long>> byDimension = server.overworld().getData(CraftorioDataAttachments.OWNED_CHUNK_INDEX).get(uuid);
        if (byDimension == null) return rects;

        for (Map.Entry<String, Set<Long>> entry : byDimension.entrySet()) {
            if (HAVEN.equals(entry.getKey())) continue;

            Map<Integer, List<Integer>> rows = new HashMap<>();
            for (long packed : entry.getValue()) {
                rows.computeIfAbsent(ChunkPos.getZ(packed), key -> new ArrayList<>()).add(ChunkPos.getX(packed));
            }
            for (Map.Entry<Integer, List<Integer>> row : rows.entrySet()) {
                List<Integer> xs = row.getValue();
                xs.sort(Integer::compare);
                int start = xs.get(0);
                int previous = start;
                for (int i = 1; i < xs.size(); i++) {
                    int x = xs.get(i);
                    if (x != previous + 1) {
                        rects.add(new ChunkRect(entry.getKey(), start, row.getKey(), previous, row.getKey()));
                        start = x;
                    }
                    previous = x;
                }
                rects.add(new ChunkRect(entry.getKey(), start, row.getKey(), previous, row.getKey()));
            }
        }
        return rects;
    }

    private static List<ChunkRect> borderRects(List<CraftorioBorder> borders) {
        List<ChunkRect> rects = new ArrayList<>();
        for (CraftorioBorder border : borders) {
            String dimension = border.getDimension().location().toString();
            if (HAVEN.equals(dimension)) continue;

            double half = Math.max(border.getSize(), border.getLerpTarget()) / 2.0;
            int minX = (int) Math.floor((border.getCenterX() - half) / 16.0);
            int maxX = (int) Math.ceil((border.getCenterX() + half) / 16.0) - 1;
            int minZ = (int) Math.floor((border.getCenterZ() - half) / 16.0);
            int maxZ = (int) Math.ceil((border.getCenterZ() + half) / 16.0) - 1;
            rects.add(new ChunkRect(dimension, minX, minZ, Math.max(minX, maxX), Math.max(minZ, maxZ)));
        }
        return rects;
    }

    public static List<ChunkRect> compact(List<ChunkRect> rects) {
        if (rects.size() <= MAX_LOGGED_RECTS) return List.copyOf(rects);

        Map<String, ChunkRect> bounds = new HashMap<>();
        for (ChunkRect rect : rects) {
            bounds.merge(rect.dimension(), rect, (a, b) -> new ChunkRect(a.dimension(),
                    Math.min(a.minX(), b.minX()), Math.min(a.minZ(), b.minZ()), Math.max(a.maxX(), b.maxX()), Math.max(a.maxZ(), b.maxZ())));
        }
        return new ArrayList<>(bounds.values());
    }

    public static void log(MinecraftServer server, int index, List<ChunkRect> rects) {
        if (rects.isEmpty()) return;

        ServerLevel overworld = server.overworld();
        List<WipeLogEntry> log = new ArrayList<>(overworld.getData(CraftorioDataAttachments.SACRIFICE_WIPE_LOG));
        log.add(new WipeLogEntry(index, compact(rects)));
        while (log.size() > MAX_LOG_ENTRIES) {
            log.remove(0);
        }
        overworld.setData(CraftorioDataAttachments.SACRIFICE_WIPE_LOG, log);
    }

    public static boolean wipedSince(MinecraftServer server, int appliedIndex, String dimension, ChunkPos pos) {
        for (WipeLogEntry entry : server.overworld().getData(CraftorioDataAttachments.SACRIFICE_WIPE_LOG)) {
            if (entry.index() > appliedIndex && contains(entry.rects(), dimension, pos.x, pos.z)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Map<String, Set<Long>>> copyIndex(Map<String, Map<String, Set<Long>>> source) {
        Map<String, Map<String, Set<Long>>> copy = new HashMap<>();
        for (Map.Entry<String, Map<String, Set<Long>>> owner : source.entrySet()) {
            Map<String, Set<Long>> dimensions = new HashMap<>();
            for (Map.Entry<String, Set<Long>> dimension : owner.getValue().entrySet()) {
                dimensions.put(dimension.getKey(), new HashSet<>(dimension.getValue()));
            }
            copy.put(owner.getKey(), dimensions);
        }
        return copy;
    }
}

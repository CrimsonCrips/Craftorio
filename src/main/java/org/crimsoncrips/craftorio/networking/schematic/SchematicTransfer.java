package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SchematicTransfer {

    public static final int CHUNK_BYTES = 262_144;

    private static final Map<String, byte[][]> PENDING = new HashMap<>();

    private SchematicTransfer() {}

    public static List<byte[]> split(CompoundTag tag) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, output);
        byte[] bytes = output.toByteArray();

        List<byte[]> chunks = new ArrayList<>();
        for (int start = 0; start < bytes.length; start += CHUNK_BYTES) {
            chunks.add(Arrays.copyOfRange(bytes, start, Math.min(bytes.length, start + CHUNK_BYTES)));
        }
        if (chunks.isEmpty()) {
            chunks.add(new byte[0]);
        }
        return chunks;
    }

    public static synchronized CompoundTag accept(String key, int index, int total, byte[] part) throws IOException {
        if (total <= 0 || index < 0 || index >= total) {
            PENDING.remove(key);
            throw new IOException("Invalid schematic chunk " + index + " of " + total + " for " + key);
        }

        byte[][] parts = PENDING.get(key);
        if (parts == null || parts.length != total || index == 0) {
            parts = new byte[total][];
            PENDING.put(key, parts);
        }
        parts[index] = part;

        for (byte[] received : parts) {
            if (received == null) return null;
        }
        PENDING.remove(key);

        ByteArrayOutputStream joined = new ByteArrayOutputStream();
        for (byte[] received : parts) {
            joined.write(received);
        }
        return NbtIo.readCompressed(new ByteArrayInputStream(joined.toByteArray()), NbtAccounter.unlimitedHeap());
    }

    public static synchronized void clear() {
        PENDING.clear();
    }
}

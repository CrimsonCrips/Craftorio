package org.crimsoncrips.craftorio.server.sacrifice;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChunkRect(String dimension, int minX, int minZ, int maxX, int maxZ) {

    public static final Codec<ChunkRect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(ChunkRect::dimension),
            Codec.INT.fieldOf("minX").forGetter(ChunkRect::minX),
            Codec.INT.fieldOf("minZ").forGetter(ChunkRect::minZ),
            Codec.INT.fieldOf("maxX").forGetter(ChunkRect::maxX),
            Codec.INT.fieldOf("maxZ").forGetter(ChunkRect::maxZ)
    ).apply(instance, ChunkRect::new));

    public boolean contains(String otherDimension, int chunkX, int chunkZ) {
        return dimension.equals(otherDimension) && chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ;
    }

    public String serialize() {
        return dimension + "," + minX + "," + minZ + "," + maxX + "," + maxZ;
    }

    public static ChunkRect deserialize(String text) {
        String[] parts = text.split(",");
        return new ChunkRect(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
    }
}

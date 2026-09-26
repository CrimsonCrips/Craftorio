package org.crimsoncrips.craftorio.server.sacrifice;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record WipeLogEntry(int index, List<ChunkRect> rects) {

    public static final Codec<WipeLogEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(WipeLogEntry::index),
            ChunkRect.CODEC.listOf().fieldOf("rects").forGetter(WipeLogEntry::rects)
    ).apply(instance, WipeLogEntry::new));
}

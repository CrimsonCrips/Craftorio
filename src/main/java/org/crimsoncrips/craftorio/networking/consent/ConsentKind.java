package org.crimsoncrips.craftorio.networking.consent;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum ConsentKind {
    REBIRTH,
    SACRIFICE;

    public static final StreamCodec<ByteBuf, ConsentKind> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(id -> values()[id], ConsentKind::ordinal);
}

package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public final class StoredEffects {

    public static final Codec<StoredEffects> CODEC = Codec.list(CraftorioEffects.dispatchCodec()).xmap(StoredEffects::new, StoredEffects::effects);
    public static final StreamCodec<ByteBuf, StoredEffects> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private final List<CraftorioEffects> effects;
    private final Tag key;

    public StoredEffects(List<CraftorioEffects> effects) {
        this.effects = effects.stream().map(CraftorioEffects::copy).toList();
        this.key = Codec.list(CraftorioEffects.dispatchCodec()).encodeStart(NbtOps.INSTANCE, this.effects).result().orElse(null);
    }

    public List<CraftorioEffects> effects() {
        return effects;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof StoredEffects stored) || this.key == null || stored.key == null) return false;
        return this.key.equals(stored.key);
    }

    @Override
    public int hashCode() {
        return this.key == null ? System.identityHashCode(this) : this.key.hashCode();
    }
}

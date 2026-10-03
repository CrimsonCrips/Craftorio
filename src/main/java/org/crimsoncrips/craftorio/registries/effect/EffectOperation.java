package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum EffectOperation implements StringRepresentable {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE;

    public static final Codec<EffectOperation> CODEC = StringRepresentable.fromEnum(EffectOperation::values);
    public static final StreamCodec<ByteBuf, EffectOperation> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(id -> values()[id], EffectOperation::ordinal);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isAdditive() {
        return this == ADD || this == SUBTRACT;
    }

    public double additive(double value) {
        return switch (this) {
            case ADD -> value;
            case SUBTRACT -> -value;
            default -> 0;
        };
    }

    public double factor(double value) {
        return switch (this) {
            case MULTIPLY -> value;
            case DIVIDE -> value == 0 ? 1 : 1 / value;
            default -> 1;
        };
    }

    public boolean isNegative(double value) {
        return switch (this) {
            case ADD -> value < 0;
            case SUBTRACT -> value > 0;
            case MULTIPLY -> value < 1;
            case DIVIDE -> value > 1;
        };
    }

    public double quality(double value) {
        return isAdditive() ? additive(value) : factor(value) - 1;
    }
}

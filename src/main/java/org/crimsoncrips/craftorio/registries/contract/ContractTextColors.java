package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;
import java.util.Optional;

public record ContractTextColors(Optional<Integer> title, Optional<Integer> time, Optional<Integer> description, Optional<Integer> punishment) {

    public static final ContractTextColors EMPTY = new ContractTextColors(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<Integer> HEX_CODEC = Codec.STRING.comapFlatMap(ContractTextColors::parseHex, ContractTextColors::toHex);

    public static final Codec<ContractTextColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HEX_CODEC.optionalFieldOf("title").forGetter(ContractTextColors::title),
            HEX_CODEC.optionalFieldOf("time").forGetter(ContractTextColors::time),
            HEX_CODEC.optionalFieldOf("description").forGetter(ContractTextColors::description),
            HEX_CODEC.optionalFieldOf("punishment").forGetter(ContractTextColors::punishment)
    ).apply(instance, ContractTextColors::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ContractTextColors> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), ContractTextColors::title,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ContractTextColors::time,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ContractTextColors::description,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ContractTextColors::punishment,
            ContractTextColors::new
    );

    public boolean isEmpty() {
        return title.isEmpty() && time.isEmpty() && description.isEmpty() && punishment.isEmpty();
    }

    public int titleOr(int fallback) {
        return title.orElse(fallback);
    }

    public int timeOr(int fallback) {
        return time.orElse(fallback);
    }

    public int descriptionOr(int fallback) {
        return description.orElse(fallback);
    }

    public int punishmentOr(int fallback) {
        return punishment.orElse(fallback);
    }

    public static DataResult<Integer> parseHex(String value) {
        Optional<Integer> parsed = tryParse(value);
        return parsed.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Invalid color: " + value));
    }

    public static Optional<Integer> tryParse(String value) {
        if (value == null) return Optional.empty();
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) trimmed = trimmed.substring(1);
        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) trimmed = trimmed.substring(2);
        if (trimmed.length() != 6) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(trimmed, 16));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static String toHex(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }
}

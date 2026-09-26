package org.crimsoncrips.craftorio.server.sacrifice;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;

import java.util.List;
import java.util.Optional;

public record SavedRespawn(Optional<GlobalPos> pos, float angle, boolean forced) {

    public static final Codec<SavedRespawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("pos").forGetter(SavedRespawn::pos),
            Codec.FLOAT.fieldOf("angle").forGetter(SavedRespawn::angle),
            Codec.BOOL.fieldOf("forced").forGetter(SavedRespawn::forced)
    ).apply(instance, SavedRespawn::new));

    public static final Codec<Optional<SavedRespawn>> OPTIONAL_CODEC = CODEC.listOf().xmap(
            list -> list.isEmpty() ? Optional.<SavedRespawn>empty() : Optional.of(list.get(0)),
            optional -> optional.map(List::of).orElse(List.of())
    );
}

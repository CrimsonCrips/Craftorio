package org.crimsoncrips.craftorio.server.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;
import java.util.Optional;

public class CraftorioPointsTrigger extends SimpleCriterionTrigger<CraftorioPointsTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BigInteger points) {
        this.trigger(player, instance -> instance.matches(points));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, BigInteger threshold,
                                   boolean negative) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        CraftorioMisc.BIGINT_CODEC().fieldOf("threshold").forGetter(TriggerInstance::threshold),
                        Codec.BOOL.optionalFieldOf("negative", false).forGetter(TriggerInstance::negative)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(BigInteger points) {
            return negative ? points.compareTo(threshold) <= 0 : points.compareTo(threshold) >= 0;
        }
    }
}

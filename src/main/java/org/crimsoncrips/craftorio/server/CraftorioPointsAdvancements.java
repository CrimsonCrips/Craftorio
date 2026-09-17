package org.crimsoncrips.craftorio.server;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;
import java.util.List;

public final class CraftorioPointsAdvancements {

    public static final CraftorioPointsTrigger POINTS_TRIGGER = new CraftorioPointsTrigger();

    public static void registerTrigger(RegisterEvent event) {
        event.register(Registries.TRIGGER_TYPE, helper -> helper.register(Craftorio.prefix("points"), POINTS_TRIGGER));
    }

    public record Milestone(String path, BigInteger threshold, boolean negative) {}

    private static BigInteger illion(long coefficient, int exponent) {
        return BigInteger.valueOf(coefficient).multiply(BigInteger.TEN.pow(exponent));
    }

    public static final List<Milestone> POSITIVE_MILESTONES = List.of(
            new Milestone("millionaire", illion(1, 6), false),
            new Milestone("the_human_body", illion(7, 27), false),
            new Milestone("russias_lawsuit", illion(20, 33), false),
            new Milestone("universal_number", illion(1, 81), false),
            new Milestone("capture_of_the_true_overlord", illion(1, 102), false),
            new Milestone("the_miners_number", illion(1, 303), false),
            new Milestone("existential_infinity", CraftorioMisc.pointThreshold(), false)
    );

    public static final Milestone NEGATIVE_MILESTONE = new Milestone("existential_debt", CraftorioMisc.pointThreshold().negate(), true);

    private CraftorioPointsAdvancements() {}

    public static void checkAndGrant(ServerPlayer player, BigInteger points) {
        POINTS_TRIGGER.trigger(player, points);
    }
}

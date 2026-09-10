package org.crimsoncrips.craftorio.server;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;
import java.util.List;

public final class CraftorioPointsAdvancements {

    private static final String CRITERION = "impossible";

    private record Milestone(String path, BigInteger threshold) {}

    private static BigInteger illion(long coefficient, int exponent) {
        return BigInteger.valueOf(coefficient).multiply(BigInteger.TEN.pow(exponent));
    }

    private static final List<Milestone> POSITIVE_MILESTONES = List.of(
            new Milestone("millionaire", illion(1, 6)),
            new Milestone("the_human_body", illion(7, 27)),
            new Milestone("russias_lawsuit", illion(20, 33)),
            new Milestone("universal_number", illion(1, 81)),
            new Milestone("capture_of_the_true_overlord", illion(1, 102)),
            new Milestone("the_miners_number", illion(1, 303)),
            new Milestone("existential_infinity", CraftorioMisc.pointThreshold())
    );

    private static final Milestone NEGATIVE_MILESTONE = new Milestone("existential_debt", CraftorioMisc.pointThreshold().negate());

    private CraftorioPointsAdvancements() {}

    public static void checkAndGrant(ServerPlayer player, BigInteger points) {
        for (Milestone milestone : POSITIVE_MILESTONES) {
            if (points.compareTo(milestone.threshold()) >= 0) {
                grant(player, milestone.path());
            }
        }

        if (points.compareTo(NEGATIVE_MILESTONE.threshold()) <= 0) {
            grant(player, NEGATIVE_MILESTONE.path());
        }
    }

    private static void grant(ServerPlayer player, String path) {
        AdvancementHolder holder = player.server.getAdvancements().get(Craftorio.prefix(path));
        if (holder == null) return;

        if (CraftorioMisc.universalBased(player.level())) {
            for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
                other.getAdvancements().award(holder, CRITERION);
            }
        } else {
            player.getAdvancements().award(holder, CRITERION);
        }
    }
}

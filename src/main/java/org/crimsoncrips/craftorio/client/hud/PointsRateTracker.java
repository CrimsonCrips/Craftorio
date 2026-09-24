package org.crimsoncrips.craftorio.client.hud;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class PointsRateTracker {

    private static final long SAMPLE_INTERVAL_MS = 200L;
    private static final double HALF_LIFE_MS = 800.0;
    private static final long STALE_MS = 2000L;

    private static BigInteger lastPoints = null;
    private static long lastSampleMillis = -1L;
    private static long lastChangeMillis = -1L;
    private static double ratePerSecond = 0.0;

    private PointsRateTracker() {}

    public static void tick(BigInteger currentPoints) {
        long now = System.currentTimeMillis();

        if (lastPoints == null) {
            lastPoints = currentPoints;
            lastSampleMillis = now;
            lastChangeMillis = now;
            return;
        }

        long dtMillis = now - lastSampleMillis;
        if (dtMillis < SAMPLE_INTERVAL_MS) return;

        BigInteger delta = currentPoints.subtract(lastPoints);
        if (delta.signum() != 0) {
            lastChangeMillis = now;
        }

        if (now - lastChangeMillis >= STALE_MS) {
            ratePerSecond = 0.0;
        } else {
            double instantRatePerSecond = new BigDecimal(delta)
                    .multiply(BigDecimal.valueOf(1_000.0 / dtMillis))
                    .doubleValue();
            if (!Double.isFinite(instantRatePerSecond)) {
                instantRatePerSecond = Math.copySign(Double.MAX_VALUE, instantRatePerSecond);
            }

            double decay = Math.pow(0.5, dtMillis / HALF_LIFE_MS);
            ratePerSecond = ratePerSecond * decay + instantRatePerSecond * (1.0 - decay);
            if (!Double.isFinite(ratePerSecond)) {
                ratePerSecond = Math.copySign(Double.MAX_VALUE, ratePerSecond);
            }
        }

        lastPoints = currentPoints;
        lastSampleMillis = now;
    }

    public static BigInteger getPointsPerSecond() {
        return BigDecimal.valueOf(ratePerSecond).setScale(0, RoundingMode.HALF_UP).toBigInteger();
    }
}

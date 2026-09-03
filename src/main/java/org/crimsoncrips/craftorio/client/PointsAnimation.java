package org.crimsoncrips.craftorio.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public final class PointsAnimation {

    private static BigInteger animFrom = BigInteger.ZERO;
    private static BigInteger animTo = BigInteger.ZERO;
    private static BigInteger lastSeenTarget = null;
    private static long animStartMillis = 0L;
    private static long animDurationMillis = 500L;
    private static double lastJumpMagnitude = 0.0;
    private static double lastMagnitudeFactor = 0.0;

    private static boolean landed = true;
    private static long landedAtMillis = 0L;


    private static final long DURATION_BASE_MS = 400L;
    private static final double DURATION_PER_MAGNITUDE = 12.0;
    private static final long DURATION_MIN_MS = 350L;
    private static final long DURATION_MAX_MS = 6000L;

    private static final long SHAKE_DURATION_MIN_MS = 180L;
    private static final long SHAKE_DURATION_MAX_MS = 2200L;
    private static final float SHAKE_BASE_AMPLITUDE = 1.6f;
    private static final float SHAKE_MAX_AMPLITUDE = 14.0f;

    private static long currentShakeDuration = SHAKE_DURATION_MIN_MS;

    private static final float GROWTH_START_SCALE = 1.0f;
    private static final float GROWTH_PEAK_BASE = 1.35f;
    private static final float GROWTH_PEAK_MAX = GROWTH_PEAK_BASE * 2;
    private static final float POP_OVERSHOOT_BASE = 1.55f;
    private static final float POP_OVERSHOOT_MAX = 2.6f;
    private static final long POP_DURATION_MS = 2060L;


    private static final double MAGNITUDE_SATURATION_K = 25.0;

    private static final Random RNG = new Random();

    private PointsAnimation() {}

    private static boolean lastJumpWasIncrease = false; // NEW

    public static void tick(BigInteger actualPoints, BigInteger tempPoints) {
        if (lastSeenTarget == null) {
            lastSeenTarget = actualPoints;
            animFrom = actualPoints;
            animTo = actualPoints;
            landed = true;
            return;
        }

        if (!actualPoints.equals(lastSeenTarget)) {
            animFrom = tempPoints;
            animTo = actualPoints;
            animStartMillis = System.currentTimeMillis();
            animDurationMillis = computeDuration(animFrom, animTo);

            boolean numericIncrease = tempPoints.compareTo(actualPoints) < 0;
            boolean landsNonNegative = actualPoints.signum() >= 0; // NEW
            lastJumpWasIncrease = numericIncrease && landsNonNegative; // NEW: both must hold

            lastJumpMagnitude = Math.abs(signedLog10(animTo) - signedLog10(animFrom));
            lastMagnitudeFactor = 1.0 - Math.exp(-lastJumpMagnitude / MAGNITUDE_SATURATION_K);
            currentShakeDuration = computeShakeDuration(lastMagnitudeFactor);

            landed = false;
            lastSeenTarget = actualPoints;
        }
    }

    public static BigInteger getDisplayValue() {
        if (landed) return animTo;

        long now = System.currentTimeMillis();
        double t = (now - animStartMillis) / (double) animDurationMillis;

        if (t >= 1.0) {
            landed = true;
            landedAtMillis = now;
            return animTo;
        }

        double eased = easeInCubic(t);

        double signedLogFrom = signedLog10(animFrom);
        double signedLogTo = signedLog10(animTo);
        double interpolated = signedLogFrom + (signedLogTo - signedLogFrom) * eased;

        return fromSignedLog10(interpolated);
    }

    private static double signedLog10(BigInteger val) {
        double mag = log10(val.abs().add(BigInteger.ONE));
        return val.signum() < 0 ? -mag : mag;
    }

    private static BigInteger fromSignedLog10(double signedLogValue) {
        boolean negative = signedLogValue < 0;
        double mag = Math.abs(signedLogValue);
        return fromLog10(mag, negative);
    }

    public static float getScale() {
        if (!lastJumpWasIncrease) {
            return GROWTH_START_SCALE;
        }

        float growthPeak = GROWTH_PEAK_BASE + (GROWTH_PEAK_MAX - GROWTH_PEAK_BASE) * (float) lastMagnitudeFactor;
        float popOvershoot = POP_OVERSHOOT_BASE + (POP_OVERSHOOT_MAX - POP_OVERSHOOT_BASE) * (float) lastMagnitudeFactor;

        if (landed) {
            long sinceLand = System.currentTimeMillis() - landedAtMillis;
            if (sinceLand >= POP_DURATION_MS) {
                return GROWTH_START_SCALE;
            }
            double t = sinceLand / (double) POP_DURATION_MS;
            double decayed = easeOutCubic(1.0 - t);
            return (float) (GROWTH_START_SCALE + (popOvershoot - GROWTH_START_SCALE) * decayed);
        }

        long now = System.currentTimeMillis();
        double t = Math.min(1.0, (now - animStartMillis) / (double) animDurationMillis);
        double eased = easeInCubic(t);
        return (float) (GROWTH_START_SCALE + (growthPeak - GROWTH_START_SCALE) * eased);
    }

    public static float[] getShakeOffset() {
        if (!lastJumpWasIncrease) {
            return new float[]{0, 0}; // NEW: no shake on decrease
        }

        if (!landed) return new float[]{0, 0};
        long sinceLand = System.currentTimeMillis() - landedAtMillis;
        if (sinceLand >= currentShakeDuration) return new float[]{0, 0};

        double decay = 1.0 - (sinceLand / (double) currentShakeDuration);
        float amp = SHAKE_BASE_AMPLITUDE + (SHAKE_MAX_AMPLITUDE - SHAKE_BASE_AMPLITUDE) * (float) lastMagnitudeFactor;
        amp *= decay;

        return new float[]{
                (RNG.nextFloat() * 2 - 1) * amp,
                (RNG.nextFloat() * 2 - 1) * amp
        };
    }

    public static boolean isAnimating() {
        return !landed;
    }

    // ---- duration helpers ----

    private static long computeDuration(BigInteger from, BigInteger to) {
        double logDiff = Math.abs(log10(to) - log10(from));
        long duration = DURATION_BASE_MS + (long) (logDiff * DURATION_PER_MAGNITUDE);
        return Math.min(DURATION_MAX_MS, Math.max(DURATION_MIN_MS, duration));
    }

    private static long computeShakeDuration(double magnitudeFactor) {
        return SHAKE_DURATION_MIN_MS + (long) ((SHAKE_DURATION_MAX_MS - SHAKE_DURATION_MIN_MS) * magnitudeFactor);
    }

    // ---- easing helpers ----

    private static double easeInCubic(double t) {
        return t * t * t;
    }

    private static double easeOutCubic(double t) {
        double t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }


    private static double log10(BigInteger val) {
        BigInteger abs = val.abs(); // NEW: work on magnitude only
        if (abs.signum() == 0) return 0.0;
        String digits = abs.toString();
        int length = digits.length();
        String mantissaDigits = length <= 17 ? digits : digits.substring(0, 17);
        double mantissa = Double.parseDouble(mantissaDigits);
        return Math.log10(mantissa) + (length - mantissaDigits.length());
    }

    private static BigInteger fromLog10(double log10Value, boolean negative) {
        if (log10Value <= 0) return BigInteger.ZERO;
        long intPart = (long) Math.floor(log10Value);
        double fracPart = log10Value - intPart;
        double mantissa = Math.pow(10, fracPart);
        BigDecimal result = BigDecimal.valueOf(mantissa).multiply(BigDecimal.TEN.pow((int) intPart));
        BigInteger magnitude = result.toBigInteger();
        return negative ? magnitude.negate() : magnitude; // NEW
    }
}
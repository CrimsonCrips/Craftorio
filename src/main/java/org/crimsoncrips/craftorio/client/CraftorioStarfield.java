package org.crimsoncrips.craftorio.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class CraftorioStarfield {

    private record Star(float x, float y, int size, int peakFrame, double phase, double riseMs, double holdMs, double decayMs, double cooldownMs,
                         float depth, float r, float g, float b) {

        double cycleMs() {
            return riseMs + holdMs + decayMs + cooldownMs;
        }
    }

    private static final int[] PEAK_FRAMES = {1, 3, 5};
    private static final ResourceLocation[] GLITTER_FRAMES = new ResourceLocation[6];
    private static final int GLITTER_TEXTURE_SIZE = 8;
    private static final int FRAME_COUNT = 6;

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            GLITTER_FRAMES[i] = Craftorio.getGuiTexture("skill_tree_fx/glitter_" + i + ".png");
        }
    }

    private static final long SEED = 918273645L;
    private static final int STAR_COUNT = 130;
    private static final double RISE_MIN_MS = 400.0;
    private static final double RISE_MAX_MS = 900.0;
    private static final double HOLD_MIN_MS = 5000.0;
    private static final double HOLD_MAX_MS = 10000.0;
    private static final double DECAY_MIN_MS = 400.0;
    private static final double DECAY_MAX_MS = 900.0;
    private static final double COOLDOWN_MIN_MS = 7000.0;
    private static final double COOLDOWN_MAX_MS = 13000.0;
    private static final float PARALLAX_STRENGTH = 0.25f;
    private static final float HALO_SIZE_FACTOR = 1.6f;
    private static final float HALO_ALPHA_FACTOR = 0.5f;
    private static final float BASE_BLUR = 0.35f;

    private static final float BASE_HUE;
    private static final float BASE_SATURATION;
    private static final float BASE_BRIGHTNESS;
    private static final float HUE_JITTER = 0.05f;
    private static final float SATURATION_JITTER = 0.15f;
    private static final float BRIGHTNESS_JITTER = 0.15f;

    static {
        float[] hsb = Color.RGBtoHSB(0x30, 0xFF, 0x5D, null);
        BASE_HUE = hsb[0];
        BASE_SATURATION = hsb[1];
        BASE_BRIGHTNESS = hsb[2];
    }

    private static List<Star> stars = List.of();
    private static int cachedWidth = -1;
    private static int cachedHeight = -1;

    private CraftorioStarfield() {}

    public static void render(GuiGraphics graphics, int width, int height, double panX, double panY) {
        if (width != cachedWidth || height != cachedHeight) {
            regenerate(width, height);
        }

        long now = System.currentTimeMillis();
        for (Star star : stars) {
            double cyclePos = (now + star.phase()) % star.cycleMs();

            float wave;
            if (cyclePos < star.riseMs()) {
                wave = (float) (cyclePos / star.riseMs());
            } else if (cyclePos < star.riseMs() + star.holdMs()) {
                wave = 1f;
            } else if (cyclePos < star.riseMs() + star.holdMs() + star.decayMs()) {
                double decayT = (cyclePos - star.riseMs() - star.holdMs()) / star.decayMs();
                wave = (float) (1.0 - decayT);
            } else {
                continue;
            }

            int frame = Mth.clamp(Math.round(wave * star.peakFrame()), 0, FRAME_COUNT - 1);
            float alpha = Mth.clamp(0.1f + wave * 0.9f, 0.05f, 1f);

            float rawX = star.x() + (float) (panX * star.depth() * PARALLAX_STRENGTH);
            float rawY = star.y() + (float) (panY * star.depth() * PARALLAX_STRENGTH);
            int x = Math.round(wrap(rawX, width));
            int y = Math.round(wrap(rawY, height));
            int size = star.size();

            ResourceLocation frameTexture = GLITTER_FRAMES[frame];

            float blurAmount = BASE_BLUR + star.depth() * (1f - BASE_BLUR);
            int haloSize = Math.round(size * (1f + blurAmount * HALO_SIZE_FACTOR));
            float haloAlpha = alpha * blurAmount * HALO_ALPHA_FACTOR;
            int hx = x - (haloSize - size) / 2;
            int hy = y - (haloSize - size) / 2;

            graphics.setColor(star.r(), star.g(), star.b(), haloAlpha);
            graphics.blit(frameTexture, hx, hy, haloSize, haloSize, 0, 0, GLITTER_TEXTURE_SIZE, GLITTER_TEXTURE_SIZE, GLITTER_TEXTURE_SIZE, GLITTER_TEXTURE_SIZE);

            graphics.setColor(star.r(), star.g(), star.b(), alpha);
            graphics.blit(frameTexture, x, y, size, size, 0, 0, GLITTER_TEXTURE_SIZE, GLITTER_TEXTURE_SIZE, GLITTER_TEXTURE_SIZE, GLITTER_TEXTURE_SIZE);
        }
        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.bufferSource().endLastBatch();
    }

    private static float wrap(float value, int dimension) {
        float wrapped = value % dimension;
        if (wrapped < 0) wrapped += dimension;
        return wrapped;
    }

    private static void regenerate(int width, int height) {
        cachedWidth = width;
        cachedHeight = height;

        RandomSource random = RandomSource.create(SEED);
        List<Star> generated = new ArrayList<>(STAR_COUNT);
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * height;
            float brightness = 0.35f + random.nextFloat() * 0.65f;
            int size = brightness > 0.85f ? 4 : 3;
            int peakFrame = PEAK_FRAMES[random.nextInt(PEAK_FRAMES.length)];
            double phase = random.nextDouble() * 100000.0;
            double riseMs = RISE_MIN_MS + random.nextDouble() * (RISE_MAX_MS - RISE_MIN_MS);
            double holdMs = HOLD_MIN_MS + random.nextDouble() * (HOLD_MAX_MS - HOLD_MIN_MS);
            double decayMs = DECAY_MIN_MS + random.nextDouble() * (DECAY_MAX_MS - DECAY_MIN_MS);
            double cooldownMs = COOLDOWN_MIN_MS + random.nextDouble() * (COOLDOWN_MAX_MS - COOLDOWN_MIN_MS);
            float depth = random.nextFloat() * random.nextFloat();

            float hue = BASE_HUE + (random.nextFloat() * 2f - 1f) * HUE_JITTER;
            float saturation = Mth.clamp(BASE_SATURATION + (random.nextFloat() * 2f - 1f) * SATURATION_JITTER, 0f, 1f);
            float value = Mth.clamp(BASE_BRIGHTNESS + (random.nextFloat() * 2f - 1f) * BRIGHTNESS_JITTER, 0f, 1f);
            int rgb = Color.HSBtoRGB(hue, saturation, value);
            float r = ((rgb >> 16) & 0xFF) / 255f;
            float g = ((rgb >> 8) & 0xFF) / 255f;
            float b = (rgb & 0xFF) / 255f;

            generated.add(new Star(x, y, size, peakFrame, phase, riseMs, holdMs, decayMs, cooldownMs, depth, r, g, b));
        }
        stars = generated;
    }
}

package org.crimsoncrips.craftorio.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.joml.Matrix4f;

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

    private record Haze(float x, float y, int size, float alpha, float depth, double phase, double periodMs, float driftX, float driftY) {}

    private static final float ZOOM_BASE_SCALE = 1.12f;
    private static final float ZOOM_SCALE_STRENGTH = 0.06f;

    private static final float HAZE_FALLOFF = 2.2f;
    private static final int HAZE_SEGMENTS = 24;
    private static final int HAZE_RINGS = 5;
    private static final int HAZE_COUNT = 110;
    private static final float HAZE_MIN_ALPHA = 0.015f;
    private static final float HAZE_MAX_ALPHA = 0.032f;
    private static final float HAZE_SPREAD = 0.07f;
    private static final float HAZE_MIN_SIZE = 0.22f;
    private static final float HAZE_MAX_SIZE = 0.45f;
    private static final float HAZE_PARALLAX_STRENGTH = 0.08f;
    private static final double HAZE_MIN_PERIOD_MS = 14000.0;
    private static final double HAZE_MAX_PERIOD_MS = 32000.0;
    private static final float HAZE_TINT_R = 0.86f;
    private static final float HAZE_TINT_G = 0.91f;
    private static final float HAZE_TINT_B = 1.0f;

    private static final int[] PEAK_FRAMES = {1, 3, 5};
    private static final ResourceLocation[] GLITTER_FRAMES = new ResourceLocation[6];
    private static final int GLITTER_TEXTURE_SIZE = 8;
    private static final int FRAME_COUNT = 6;

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            GLITTER_FRAMES[i] = Craftorio.getGuiTexture("skill_tree/particle/glitter_" + i + ".png");
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

    private static final float HUE_JITTER = 0.05f;
    private static final float SATURATION_JITTER = 0.15f;
    private static final float BRIGHTNESS_JITTER = 0.15f;

    private static List<Star> stars = List.of();
    private static List<Haze> hazes = List.of();
    private static int cachedWidth = -1;
    private static int cachedHeight = -1;
    private static int cachedColor;

    private CraftorioStarfield() {}

    public static void render(GuiGraphics graphics, int width, int height, double panX, double panY, double zoom, int baseColor) {
        if (width != cachedWidth || height != cachedHeight || baseColor != cachedColor) {
            regenerate(width, height, baseColor);
        }

        long now = System.currentTimeMillis();
        float scale = ZOOM_BASE_SCALE * (1f + ZOOM_SCALE_STRENGTH * (float) Math.log(Math.max(zoom, 0.01)));

        graphics.pose().pushPose();
        graphics.pose().translate(width / 2f, height / 2f, 0f);
        graphics.pose().scale(scale, scale, 1f);
        graphics.pose().translate(-width / 2f, -height / 2f, 0f);

        renderHaze(graphics, panX, panY, now);

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
        graphics.pose().popPose();
    }

    private static void renderHaze(GuiGraphics graphics, double panX, double panY, long now) {
        graphics.flush();

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (Haze haze : hazes) {
            double cycle = ((now + haze.phase()) % haze.periodMs()) / haze.periodMs();
            float wave = (float) Math.sin(cycle * Math.PI * 2);
            float peakAlpha = haze.alpha() * (0.65f + 0.35f * wave);

            float cx = haze.x() + wave * haze.driftX() + (float) (panX * haze.depth() * HAZE_PARALLAX_STRENGTH);
            float cy = haze.y() + wave * haze.driftY() + (float) (panY * haze.depth() * HAZE_PARALLAX_STRENGTH);
            float radius = haze.size() / 2f;

            for (int ring = 0; ring < HAZE_RINGS; ring++) {
                float innerRadius = radius * ring / HAZE_RINGS;
                float outerRadius = radius * (ring + 1) / HAZE_RINGS;
                int innerAlpha = hazeAlpha(peakAlpha, (float) ring / HAZE_RINGS);
                int outerAlpha = hazeAlpha(peakAlpha, (float) (ring + 1) / HAZE_RINGS);

                for (int segment = 0; segment < HAZE_SEGMENTS; segment++) {
                    double angleA = Math.PI * 2 * segment / HAZE_SEGMENTS;
                    double angleB = Math.PI * 2 * (segment + 1) / HAZE_SEGMENTS;
                    float cosA = (float) Math.cos(angleA);
                    float sinA = (float) Math.sin(angleA);
                    float cosB = (float) Math.cos(angleB);
                    float sinB = (float) Math.sin(angleB);

                    hazeVertex(builder, matrix, cx + cosA * outerRadius, cy + sinA * outerRadius, outerAlpha);
                    hazeVertex(builder, matrix, cx + cosB * outerRadius, cy + sinB * outerRadius, outerAlpha);
                    hazeVertex(builder, matrix, cx + cosA * innerRadius, cy + sinA * innerRadius, innerAlpha);

                    if (ring > 0) {
                        hazeVertex(builder, matrix, cx + cosB * outerRadius, cy + sinB * outerRadius, outerAlpha);
                        hazeVertex(builder, matrix, cx + cosB * innerRadius, cy + sinB * innerRadius, innerAlpha);
                        hazeVertex(builder, matrix, cx + cosA * innerRadius, cy + sinA * innerRadius, innerAlpha);
                    }
                }
            }
        }

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static int hazeAlpha(float peakAlpha, float distance) {
        float falloff = (float) Math.pow(Math.max(0f, 1f - distance), HAZE_FALLOFF);
        return Mth.clamp(Math.round(255f * peakAlpha * falloff), 0, 255);
    }

    private static void hazeVertex(BufferBuilder builder, Matrix4f matrix, float x, float y, int alpha) {
        builder.addVertex(matrix, x, y, 0f)
                .setColor(Math.round(HAZE_TINT_R * 255f), Math.round(HAZE_TINT_G * 255f), Math.round(HAZE_TINT_B * 255f), alpha);
    }

    private static float wrap(float value, int dimension) {
        float wrapped = value % dimension;
        if (wrapped < 0) wrapped += dimension;
        return wrapped;
    }

    private static void regenerate(int width, int height, int baseColor) {
        cachedWidth = width;
        cachedHeight = height;
        cachedColor = baseColor;

        float[] baseHsb = Color.RGBtoHSB((baseColor >> 16) & 0xFF, (baseColor >> 8) & 0xFF, baseColor & 0xFF, null);
        float baseHue = baseHsb[0];
        float baseSaturation = baseHsb[1];
        float baseBrightness = baseHsb[2];

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

            float hue = baseHue + (random.nextFloat() * 2f - 1f) * HUE_JITTER;
            float saturation = Mth.clamp(baseSaturation + (random.nextFloat() * 2f - 1f) * SATURATION_JITTER, 0f, 1f);
            float value = Mth.clamp(baseBrightness + (random.nextFloat() * 2f - 1f) * BRIGHTNESS_JITTER, 0f, 1f);
            int rgb = Color.HSBtoRGB(hue, saturation, value);
            float r = ((rgb >> 16) & 0xFF) / 255f;
            float g = ((rgb >> 8) & 0xFF) / 255f;
            float b = (rgb & 0xFF) / 255f;

            generated.add(new Star(x, y, size, peakFrame, phase, riseMs, holdMs, decayMs, cooldownMs, depth, r, g, b));
        }
        stars = generated;
        hazes = generateHaze(width, height, random);
    }

    private static List<Haze> generateHaze(int width, int height, RandomSource random) {
        float startX = -0.1f * width;
        float startY = 0.72f * height;
        float endX = 1.1f * width;
        float endY = 0.38f * height;
        float dirX = endX - startX;
        float dirY = endY - startY;
        float length = Mth.sqrt(dirX * dirX + dirY * dirY);
        float normalX = -dirY / length;
        float normalY = dirX / length;

        List<Haze> generated = new ArrayList<>(HAZE_COUNT);
        for (int i = 0; i < HAZE_COUNT; i++) {
            float t = random.nextFloat();
            float offset = (float) random.nextGaussian() * HAZE_SPREAD * height;
            float x = startX + dirX * t + normalX * offset;
            float y = startY + dirY * t + normalY * offset;
            int size = Math.round(width * (HAZE_MIN_SIZE + random.nextFloat() * (HAZE_MAX_SIZE - HAZE_MIN_SIZE)));
            float alpha = HAZE_MIN_ALPHA + random.nextFloat() * (HAZE_MAX_ALPHA - HAZE_MIN_ALPHA);
            float depth = 0.3f + random.nextFloat() * 0.7f;
            double phase = random.nextDouble() * 100000.0;
            double period = HAZE_MIN_PERIOD_MS + random.nextDouble() * (HAZE_MAX_PERIOD_MS - HAZE_MIN_PERIOD_MS);
            float driftX = (random.nextFloat() * 2f - 1f) * 14f;
            float driftY = (random.nextFloat() * 2f - 1f) * 6f;
            generated.add(new Haze(x, y, size, alpha, depth, phase, period, driftX, driftY));
        }
        return generated;
    }
}

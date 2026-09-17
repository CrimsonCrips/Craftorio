package org.crimsoncrips.craftorio.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class CraftorioStarfield {

    private record Star(float x, float y, int size, float phase, float brightness, float depth) {}

    private static final ResourceLocation GLINT_TEXTURE = Craftorio.getGuiTexture("skill_tree_fx/glint.png");
    private static final int GLINT_TEXTURE_SIZE = 8;

    private static final long SEED = 918273645L;
    private static final int STAR_COUNT = 220;
    private static final double TWINKLE_PERIOD_MS = 900.0;
    private static final float BRIGHT_STAR_THRESHOLD = 0.85f;
    private static final float PARALLAX_STRENGTH = 0.25f;

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
            float twinkle = 0.55f + 0.45f * (float) Math.sin(now / TWINKLE_PERIOD_MS + star.phase());
            float alpha = Mth.clamp(star.brightness() * twinkle, 0.08f, 1f);

            float rawX = star.x() + (float) (panX * star.depth() * PARALLAX_STRENGTH);
            float rawY = star.y() + (float) (panY * star.depth() * PARALLAX_STRENGTH);
            int x = Math.round(wrap(rawX, width));
            int y = Math.round(wrap(rawY, height));
            int size = star.size();

            graphics.setColor(1f, 1f, 1f, alpha);
            graphics.blit(GLINT_TEXTURE, x, y, size, size, 0, 0, GLINT_TEXTURE_SIZE, GLINT_TEXTURE_SIZE, GLINT_TEXTURE_SIZE, GLINT_TEXTURE_SIZE);
        }
        graphics.setColor(1f, 1f, 1f, 1f);
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
            int size = brightness > BRIGHT_STAR_THRESHOLD ? 6 : 4;
            float phase = random.nextFloat() * (float) (Math.PI * 2);
            float depth = random.nextFloat() * random.nextFloat();
            generated.add(new Star(x, y, size, phase, brightness, depth));
        }
        stars = generated;
    }
}

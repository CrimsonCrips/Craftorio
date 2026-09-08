package org.crimsoncrips.craftorio.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class InfinityBurst {

    private static final long DURATION_MS = 1500L;
    private static final long SHRINK_START_MS = 900L;
    private static final int PARTICLE_COUNT = 14;
    private static final float MIN_SPEED = 50.0f;
    private static final float MAX_SPEED = 85.0f;

    private static final Random RNG = new Random();
    private static final List<Particle> particles = new ArrayList<>();

    private InfinityBurst() {}

    public static void spawn(int mode, float baseScale) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float angle = (float) (RNG.nextDouble() * Math.PI * 2);
            float speed = MIN_SPEED + RNG.nextFloat() * (MAX_SPEED - MIN_SPEED);
            particles.add(new Particle(mode, now, angle, speed, baseScale));
        }
    }

    public static void render(GuiGraphics graphics, Font font, float anchorX, float anchorY) {
        if (particles.isEmpty()) return;

        long now = System.currentTimeMillis();
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            long age = now - particle.startMillis;
            if (age >= DURATION_MS) {
                iterator.remove();
                continue;
            }

            float t = age / (float) DURATION_MS;
            float distance = particle.speed * easeOutCubic(t);
            float x = anchorX + (float) Math.cos(particle.angle) * distance;
            float y = anchorY + (float) Math.sin(particle.angle) * distance;

            float scale;
            if (age < SHRINK_START_MS) {
                scale = particle.baseScale();
            } else {
                float shrinkT = (age - SHRINK_START_MS) / (float) (DURATION_MS - SHRINK_START_MS);
                scale = particle.baseScale() * (1.0f - easeInCubic(shrinkT));
            }
            if (scale <= 0f) continue;

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(scale, scale, scale);
            CraftorioMisc.CraftorioTextEffects.drawFancy(graphics, font, "*", -font.width("*") / 2, -font.lineHeight / 2, true, particle.mode);
            graphics.pose().popPose();
        }
    }

    private static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    private static float easeInCubic(float t) {
        return t * t * t;
    }

    private record Particle(int mode, long startMillis, float angle, float speed, float baseScale) {}
}

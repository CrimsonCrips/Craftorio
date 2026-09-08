package org.crimsoncrips.craftorio.client;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class PointsPopup {

    private static final long DURATION_MS = 1800L;
    private static final long SHRINK_START_MS = 1100L;
    private static final float DRIFT_Y = 26.0f;
    private static final float DRIFT_X = 8.0f;
    private static final float MIN_TILT_DEGREES = 4.0f;
    private static final float MAX_TILT_DEGREES = 16.0f;

    private static final Random RNG = new Random();
    private static final List<Popup> popups = new ArrayList<>();

    private PointsPopup() {}

    public static void spawn(BigInteger delta) {
        if (delta.signum() == 0) return;

        String formatted = CraftorioMisc.bigIntFormat(delta, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        boolean positive = delta.signum() > 0;
        String text = positive ? "+" + formatted : formatted;
        float side = RNG.nextBoolean() ? 1.0f : -1.0f;
        float tilt = side * (MIN_TILT_DEGREES + RNG.nextFloat() * (MAX_TILT_DEGREES - MIN_TILT_DEGREES));

        popups.add(new Popup(text, positive, System.currentTimeMillis(), tilt, side));
    }

    public static void render(GuiGraphics graphics, Font font, float anchorX, float anchorY) {
        if (popups.isEmpty()) return;

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        long now = System.currentTimeMillis();
        Iterator<Popup> iterator = popups.iterator();
        while (iterator.hasNext()) {
            Popup popup = iterator.next();
            long age = now - popup.startMillis;
            if (age >= DURATION_MS) {
                iterator.remove();
                continue;
            }

            float t = age / (float) DURATION_MS;
            float offsetY = DRIFT_Y * easeOutCubic(t);
            float offsetX = popup.driftSign * DRIFT_X * easeOutCubic(t);

            float scale;
            if (age < SHRINK_START_MS) {
                scale = 1.0f;
            } else {
                float shrinkT = (age - SHRINK_START_MS) / (float) (DURATION_MS - SHRINK_START_MS);
                scale = 1.0f - easeInCubic(shrinkT);
            }
            if (scale <= 0f) continue;

            int color = popup.positive ? 0x55FF55 : 0xFF5555;
            int width = font.width(popup.text);
            int halfWidth = width / 2 + 2;
            int halfHeight = font.lineHeight / 2 + 2;

            float drawX = Mth.clamp(anchorX + offsetX, halfWidth, screenWidth - halfWidth);
            float drawY = Mth.clamp(anchorY + offsetY, halfHeight, screenHeight - halfHeight);

            graphics.pose().pushPose();
            graphics.pose().translate(drawX, drawY, 0);
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(popup.tiltDegrees));
            graphics.pose().scale(scale, scale, scale);
            graphics.drawString(font, popup.text, -width / 2, -font.lineHeight / 2, color, true);
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

    private record Popup(String text, boolean positive, long startMillis, float tiltDegrees, float driftSign) {}
}

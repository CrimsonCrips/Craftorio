package org.crimsoncrips.craftorio.client.render;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.crimsoncrips.craftorio.Craftorio;

@OnlyIn(Dist.CLIENT)
public final class CraftorioScreenFade {

    private static final long FADE_OUT_MS = 700L;
    private static final long HOLD_MS = 250L;
    private static final long FADE_IN_MS = 900L;

    private static boolean active = false;
    private static long startMillis = 0L;
    private static Runnable midpoint;

    private CraftorioScreenFade() {}

    public static void start(Runnable atBlack) {
        midpoint = atBlack;
        startMillis = Util.getMillis();
        active = true;
    }

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Craftorio.prefix("screen_fade"), (graphics, deltaTracker) -> {
            if (active && Minecraft.getInstance().screen == null) {
                render(graphics);
            }
        });
    }

    public static void renderOverScreen(ScreenEvent.Render.Post event) {
        if (active) {
            render(event.getGuiGraphics());
        }
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        active = false;
        midpoint = null;
    }

    private static void render(GuiGraphics graphics) {
        long elapsed = Util.getMillis() - startMillis;

        if (midpoint != null && elapsed >= FADE_OUT_MS) {
            Runnable atBlack = midpoint;
            midpoint = null;
            Minecraft.getInstance().execute(atBlack);
        }

        float alpha;
        if (elapsed < FADE_OUT_MS) {
            alpha = elapsed / (float) FADE_OUT_MS;
        } else if (elapsed < FADE_OUT_MS + HOLD_MS) {
            alpha = 1f;
        } else {
            alpha = 1f - (elapsed - FADE_OUT_MS - HOLD_MS) / (float) FADE_IN_MS;
        }

        if (elapsed >= FADE_OUT_MS + HOLD_MS + FADE_IN_MS) {
            active = false;
            return;
        }

        int alphaByte = Mth.clamp(Math.round(alpha * 255f), 0, 255);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alphaByte << 24);
        graphics.pose().popPose();
    }
}

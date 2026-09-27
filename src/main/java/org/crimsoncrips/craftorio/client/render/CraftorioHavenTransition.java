package org.crimsoncrips.craftorio.client.render;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;

@OnlyIn(Dist.CLIENT)
public final class CraftorioHavenTransition {

    public static final long FADE_IN_MS = 2000L;
    private static final long ARRIVAL_HOLD_MS = 400L;
    private static final long FADE_OUT_MS = 1500L;
    private static final long ARRIVAL_TIMEOUT_MS = 15000L;
    private static final int WHITE = 0xFFFFFF;

    private enum Phase { IDLE, FADING_IN, HOLDING, FADING_OUT }

    private static Phase phase = Phase.IDLE;
    private static long phaseStartMillis;
    private static float fadeInStartAlpha;
    private static long arrivedMillis = -1L;
    private static boolean towardsHaven = true;

    private CraftorioHavenTransition() {}

    public static void start(boolean instant, boolean entering) {
        towardsHaven = entering;
        float current = alpha();
        if (instant) {
            setPhase(Phase.HOLDING);
        } else if (phase != Phase.FADING_IN && phase != Phase.HOLDING) {
            fadeInStartAlpha = current;
            setPhase(Phase.FADING_IN);
        }
    }

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Craftorio.prefix("haven_transition"), (graphics, deltaTracker) -> {
            if (Minecraft.getInstance().screen == null) {
                render(graphics);
            }
        });
    }

    public static void hideLoadingScreen(ScreenEvent.Render.Pre event) {
        if (phase == Phase.IDLE || !(event.getScreen() instanceof ReceivingLevelScreen)) return;

        event.setCanceled(true);
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xFF000000 | WHITE);
    }

    public static void renderOverScreen(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof ReceivingLevelScreen) return;
        render(event.getGuiGraphics());
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        phase = Phase.IDLE;
        arrivedMillis = -1L;
    }

    private static void setPhase(Phase next) {
        phase = next;
        phaseStartMillis = Util.getMillis();
        arrivedMillis = -1L;
    }

    private static boolean arrived() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.player != null
                && minecraft.level.dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY) == towardsHaven
                && !(minecraft.screen instanceof ReceivingLevelScreen);
    }

    private static void update() {
        long now = Util.getMillis();
        long elapsed = now - phaseStartMillis;
        switch (phase) {
            case FADING_IN -> {
                if (elapsed >= FADE_IN_MS) {
                    setPhase(Phase.HOLDING);
                }
            }
            case HOLDING -> {
                if (arrived()) {
                    if (arrivedMillis < 0L) {
                        arrivedMillis = now;
                    } else if (now - arrivedMillis >= ARRIVAL_HOLD_MS) {
                        setPhase(Phase.FADING_OUT);
                    }
                } else {
                    arrivedMillis = -1L;
                    if (elapsed >= ARRIVAL_TIMEOUT_MS) {
                        setPhase(Phase.FADING_OUT);
                    }
                }
            }
            case FADING_OUT -> {
                if (elapsed >= FADE_OUT_MS) {
                    phase = Phase.IDLE;
                }
            }
            default -> {
            }
        }
    }

    private static float alpha() {
        long elapsed = Util.getMillis() - phaseStartMillis;
        return switch (phase) {
            case FADING_IN -> Mth.lerp(easeInCubic(Mth.clamp(elapsed / (float) FADE_IN_MS, 0f, 1f)), fadeInStartAlpha, 1f);
            case HOLDING -> 1f;
            case FADING_OUT -> 1f - Mth.clamp(elapsed / (float) FADE_OUT_MS, 0f, 1f);
            default -> 0f;
        };
    }

    private static float easeInCubic(float x) {
        return x * x * x;
    }

    private static void render(GuiGraphics graphics) {
        if (phase == Phase.IDLE) return;

        update();
        float alpha = alpha();
        if (alpha <= 0f) return;

        int alphaByte = Mth.clamp(Math.round(alpha * 255f), 0, 255);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alphaByte << 24 | WHITE);
        graphics.pose().popPose();
    }
}

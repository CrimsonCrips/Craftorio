package org.crimsoncrips.craftorio.client.render;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.input.CraftorioKeyMappings;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.server.schematic.BuildBlitz;

import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public final class CraftorioAbilityHud {

    private static final int MARGIN = 4;
    private static final int PADDING_X = 5;
    private static final int PADDING_Y = 3;
    private static final int LINE_GAP = 1;
    private static final int EMPTY_COLOR = 0xFFE8E4D8;
    private static final int TEXT_COLOR = 0xFF101010;
    private static final int BLITZ_FILL_COLOR = 0xFF4A90E2;
    private static final int READY_FILL_COLOR = 0xFF55D66B;

    private CraftorioAbilityHud() {}

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Craftorio.prefix("ability_hud"), (graphics, deltaTracker) -> render(graphics));
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.options.hideGui || minecraft.gui.getDebugOverlay().showDebugScreen()) return;
        if (minecraft.screen != null) return;
        if (CraftorioMisc.isInHavenDimension(minecraft.level) || !BuildBlitz.isUnlocked(player)) return;

        long remaining = BuildBlitz.ticksUntilReady(player);
        long total = Math.max(1L, BuildBlitz.cooldownTicks(player));
        float progress = remaining <= 0 ? 1f : Mth.clamp(1f - remaining / (float) total, 0f, 1f);

        Component name = Component.translatable("misc.craftorio.hud_build_blitz");
        Component detail = remaining <= 0
                ? Component.literal(CraftorioKeyMappings.BUILD_BLITZ.getTranslatedKeyMessage().getString().toUpperCase(Locale.ROOT))
                : Component.literal(formatTime(remaining));

        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int slide = Math.round(-ClientEvents.hudIntroOffset());
        drawCard(graphics, minecraft.font, MARGIN, screenHeight - MARGIN + slide, name, detail, progress,
                remaining <= 0 ? READY_FILL_COLOR : BLITZ_FILL_COLOR);
    }

    private static void drawCard(GuiGraphics graphics, Font font, int left, int bottom, Component name, Component detail, float progress, int fillColor) {
        int innerWidth = Math.max(font.width(name), font.width(detail));
        int width = innerWidth + PADDING_X * 2;
        int height = font.lineHeight * 2 + LINE_GAP + PADDING_Y * 2;
        int top = bottom - height;

        graphics.fill(left, top, left + width, bottom, EMPTY_COLOR);
        int filled = Math.round(width * progress);
        if (filled > 0) {
            graphics.fill(left, top, left + filled, bottom, fillColor);
        }

        int centerX = left + width / 2;
        int nameY = top + PADDING_Y;
        graphics.drawString(font, name, centerX - font.width(name) / 2, nameY, TEXT_COLOR, false);
        graphics.drawString(font, detail, centerX - font.width(detail) / 2, nameY + font.lineHeight + LINE_GAP, TEXT_COLOR, false);
    }

    private static String formatTime(long ticks) {
        long seconds = (ticks + CraftorioMisc.SECONDS_TO_TICKS - 1) / CraftorioMisc.SECONDS_TO_TICKS;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return hours > 0 ? String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs)
                : String.format(Locale.ROOT, "%02d:%02d", minutes, secs);
    }
}

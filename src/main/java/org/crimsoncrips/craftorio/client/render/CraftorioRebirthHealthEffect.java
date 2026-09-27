package org.crimsoncrips.craftorio.client.render;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class CraftorioRebirthHealthEffect {

    private record Shard(float startX, float startY, int u, int v, int size, float velocityX, float velocityY, float spin, float growth, long bornMs) {}

    private static final ResourceLocation CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container");
    private static final ResourceLocation FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");

    private static final int HEART_SIZE = 9;
    private static final int SHARD_SPLIT = 3;
    private static final int MAX_HEARTS = 30;
    private static final long FIRST_SHATTER_MS = 350L;
    private static final long MAX_STEP_MS = 130L;
    private static final long MAX_SEQUENCE_MS = 2200L;
    private static final long WARNING_MS = 260L;
    private static final long FLIGHT_MS = 1400L;
    private static final long RENEW_GAP_MS = 600L;
    private static final long RENEW_STEP_MS = 70L;
    private static final long POP_MS = 220L;
    private static final long HOLD_AFTER_RENEW_MS = 450L;
    private static final float GRAVITY = 1.4f;

    private static boolean active = false;
    private static long startMillis = 0L;
    private static int heartCount = 10;
    private static int heartsToBreak = 0;
    private static int heartsBroken = 0;
    private static long stepMs = MAX_STEP_MS;
    private static int renewedSounds = 0;
    private static List<Shard> shards = new ArrayList<>();
    private static Runnable onFinish;
    private static final RandomSource random = RandomSource.create();

    private CraftorioRebirthHealthEffect() {}

    public static void start(Runnable finished) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            finished.run();
            return;
        }

        heartCount = Mth.clamp(Mth.ceil(player.getMaxHealth() / 2f), 1, MAX_HEARTS);
        heartsToBreak = Mth.clamp(Mth.ceil(player.getHealth() / 2f), 0, heartCount);
        stepMs = heartsToBreak <= 1 ? MAX_STEP_MS : Math.min(MAX_STEP_MS, MAX_SEQUENCE_MS / heartsToBreak);
        heartsBroken = 0;
        renewedSounds = 0;
        shards = new ArrayList<>();
        onFinish = finished;
        startMillis = Util.getMillis();
        active = true;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.6f, 1.0f));
    }

    public static boolean isActive() {
        return active;
    }

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Craftorio.prefix("rebirth_health_effect"), (graphics, deltaTracker) -> {
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

    public static void hideVanillaHearts(RenderGuiLayerEvent.Pre event) {
        if (active && event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        active = false;
        onFinish = null;
        shards = List.of();
    }

    private static int rows() {
        return Mth.ceil(heartCount / 10f);
    }

    private static int rowHeight() {
        return Math.max(10 - (rows() - 2), 3);
    }

    private static int heartX(int width, int index) {
        return width / 2 - 91 + (index % 10) * 8;
    }

    private static int heartY(int height, int index) {
        return height - 39 - (index / 10) * rowHeight();
    }

    private static long shatterTime(int index) {
        return FIRST_SHATTER_MS + index * stepMs;
    }

    private static long renewStartMs() {
        return (heartsToBreak == 0 ? FIRST_SHATTER_MS : shatterTime(heartsToBreak - 1)) + RENEW_GAP_MS;
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        long elapsed = Util.getMillis() - startMillis;

        while (heartsBroken < heartsToBreak && elapsed >= shatterTime(heartsBroken)) {
            spawnShards(heartsBroken, width, height, elapsed);
            float pitch = 0.9f + random.nextFloat() * 0.5f;
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, pitch, 0.6f));
            heartsBroken++;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);

        for (int i = 0; i < heartCount; i++) {
            graphics.blitSprite(CONTAINER, heartX(width, i), heartY(height, i), HEART_SIZE, HEART_SIZE);
        }

        renderIntactHearts(graphics, width, height, elapsed);
        renderShards(graphics, elapsed);
        renderRenewing(graphics, width, height, elapsed, minecraft);

        graphics.pose().popPose();

        long renewEnd = renewStartMs() + heartCount * RENEW_STEP_MS + POP_MS + HOLD_AFTER_RENEW_MS;
        if (elapsed >= renewEnd) {
            active = false;
            Runnable finished = onFinish;
            onFinish = null;
            if (finished != null) {
                minecraft.execute(finished);
            }
        }
    }

    private static void renderIntactHearts(GuiGraphics graphics, int width, int height, long elapsed) {
        for (int i = heartsBroken; i < heartsToBreak; i++) {
            long untilBreak = shatterTime(i) - elapsed;
            float warning = untilBreak >= WARNING_MS ? 0f : 1f - Math.max(0L, untilBreak) / (float) WARNING_MS;
            float shake = 0.4f + 1.8f * warning;
            float offsetX = (random.nextFloat() - 0.5f) * 2f * shake;
            float offsetY = (random.nextFloat() - 0.5f) * 2f * shake;

            graphics.pose().pushPose();
            graphics.pose().translate(heartX(width, i) + offsetX, heartY(height, i) + offsetY, 0);
            graphics.blitSprite(FULL, 0, 0, HEART_SIZE, HEART_SIZE);
            if (warning > 0f && !Craftorio.CLIENT_CONFIG.PHOTOSENSITIVE_MODE.get()) {
                graphics.fill(1, 1, HEART_SIZE - 1, HEART_SIZE - 1, (Math.round(warning * 200f) << 24) | 0xFFFFFF);
            }
            graphics.pose().popPose();
        }
    }

    private static void spawnShards(int index, int width, int height, long bornMs) {
        int x = heartX(width, index);
        int y = heartY(height, index);
        int piece = Mth.ceil(HEART_SIZE / (float) SHARD_SPLIT);

        for (int row = 0; row < SHARD_SPLIT; row++) {
            for (int column = 0; column < SHARD_SPLIT; column++) {
                int u = column * piece;
                int v = row * piece;
                float velocityX = (random.nextFloat() - 0.5f) * width * 0.9f;
                float velocityY = -height * (0.3f + random.nextFloat() * 0.8f);
                float spin = (random.nextFloat() - 0.5f) * 12f;
                float growth = 1.5f + random.nextFloat() * 3.5f;
                shards.add(new Shard(x + u, y + v, u, v, piece, velocityX, velocityY, spin, growth, bornMs));
            }
        }
    }

    private static void renderShards(GuiGraphics graphics, long elapsed) {
        int height = graphics.guiHeight();

        for (Shard shard : shards) {
            long sinceShatter = elapsed - shard.bornMs();
            if (sinceShatter < 0 || sinceShatter >= FLIGHT_MS) continue;

            float seconds = sinceShatter / 1000f;
            float life = sinceShatter / (float) FLIGHT_MS;
            float alpha = 1f - Mth.clamp((life - 0.55f) / 0.45f, 0f, 1f);
            float x = shard.startX() + shard.velocityX() * seconds;
            float y = shard.startY() + shard.velocityY() * seconds + 0.5f * GRAVITY * height * seconds * seconds;
            float scale = 1f + shard.growth() * life;

            graphics.setColor(1f, 1f, 1f, alpha);
            graphics.pose().pushPose();
            graphics.pose().translate(x + shard.size() / 2f, y + shard.size() / 2f, 0);
            graphics.pose().mulPose(Axis.ZP.rotation(shard.spin() * seconds));
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-shard.size() / 2f, -shard.size() / 2f, 0);
            graphics.blitSprite(FULL, HEART_SIZE, HEART_SIZE, shard.u(), shard.v(), 0, 0,
                    Math.min(shard.size(), HEART_SIZE - shard.u()), Math.min(shard.size(), HEART_SIZE - shard.v()));
            graphics.pose().popPose();
        }
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    private static void renderRenewing(GuiGraphics graphics, int width, int height, long elapsed, Minecraft minecraft) {
        long sinceRenew = elapsed - renewStartMs();
        if (sinceRenew < 0) return;

        for (int i = 0; i < heartCount; i++) {
            long heartTime = sinceRenew - i * RENEW_STEP_MS;
            if (heartTime < 0) break;

            if (i >= renewedSounds) {
                renewedSounds = i + 1;
                float pitch = 0.8f + 1.2f * i / Math.max(1, heartCount - 1);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, pitch, 0.6f));
            }

            float popT = Mth.clamp(heartTime / (float) POP_MS, 0f, 1f);
            float scale = popT < 0.5f ? Mth.lerp(popT / 0.5f, 0.2f, 1.45f) : Mth.lerp((popT - 0.5f) / 0.5f, 1.45f, 1f);

            graphics.pose().pushPose();
            graphics.pose().translate(heartX(width, i) + HEART_SIZE / 2f, heartY(height, i) + HEART_SIZE / 2f, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-HEART_SIZE / 2f, -HEART_SIZE / 2f, 0);
            graphics.blitSprite(FULL, 0, 0, HEART_SIZE, HEART_SIZE);
            if (popT < 1f && !Craftorio.CLIENT_CONFIG.PHOTOSENSITIVE_MODE.get()) {
                int glow = Math.round((1f - popT) * 180f);
                graphics.fill(1, 1, HEART_SIZE - 1, HEART_SIZE - 1, (glow << 24) | 0xFFFFFF);
            }
            graphics.pose().popPose();
        }
    }
}

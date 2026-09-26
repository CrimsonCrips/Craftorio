package org.crimsoncrips.craftorio.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.screen.hub.CraftorioSacrificeDisconnectScreen;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@OnlyIn(Dist.CLIENT)
public final class CraftorioShatterEffect {

    private record Shard(float[] xs, float[] ys, float centerX, float centerY, float crackStart, float releaseTime, float velocityX, float velocityY, float spin) {}

    private static final long DURATION_MS = 6200L;
    private static final float DURATION_SECONDS = DURATION_MS / 1000f;
    private static final int COLUMNS = 16;
    private static final int NOISE_WIDTH = 192;
    private static final int NOISE_HEIGHT = 108;
    private static final float FIRST_STAGE_LIMIT = 0.12f;
    private static final float SECOND_STAGE_LIMIT = 0.40f;
    private static final float SHATTER_START = 0.68f;
    private static final float SHATTER_SPREAD = 0.07f;
    private static final float SHARD_LIFE_SECONDS = 1.5f;
    private static final float GRAVITY = 2.6f;
    private static final float CRACK_GROW_TIME = 0.05f;
    private static final float[] SOUND_TIMES = {0.02f, 0.08f, 0.28f, 0.34f, 0.52f, 0.58f, 0.68f, 0.72f, 0.76f};

    private static final ResourceLocation NOISE_LOCATION = Craftorio.prefix("shatter_noise");

    private static boolean active = false;
    private static boolean captured = false;
    private static boolean returnToTitle = false;
    private static long startMillis = 0L;
    private static int soundIndex = 0;
    private static Shard[] shards = new Shard[0];
    private static RenderTarget capture;
    private static DynamicTexture noiseTexture;
    private static final RandomSource random = RandomSource.create();

    private CraftorioShatterEffect() {}

    public static void start() {
        returnToTitle = true;
        active = true;
        captured = false;
        soundIndex = 0;
        startMillis = Util.getMillis();
    }

    public static boolean isActive() {
        return active;
    }

    public static void reset() {
        active = false;
        captured = false;
        soundIndex = 0;
    }

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Craftorio.prefix("shatter_effect"), (graphics, deltaTracker) -> {
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
        reset();
    }

    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        returnToTitle = false;
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (returnToTitle && event.getNewScreen() instanceof DisconnectedScreen) {
            returnToTitle = false;
            event.setNewScreen(new CraftorioSacrificeDisconnectScreen());
        }
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        graphics.flush();

        if (!captured) {
            capture(minecraft, width, height);
        }

        float progress = Mth.clamp((Util.getMillis() - startMillis) / (float) DURATION_MS, 0f, 1f);
        playSounds(minecraft, progress);
        ensureNoiseTexture();
        updateNoise(progress);

        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        drawNoise(matrix, width, height);
        drawEye(minecraft, progress);
        drawGlitchBars(matrix, width, height, progress);
        float shake = shakeAmount(progress);
        float shakeX = (random.nextFloat() - 0.5f) * 2f * shake;
        float shakeY = (random.nextFloat() - 0.5f) * 2f * shake;
        float zoomX = 1f + 2f * shake / width;
        float zoomY = 1f + 2f * shake / height;
        drawShards(matrix, width, height, progress, shakeX, shakeY, zoomX, zoomY);
        drawCracks(matrix, width, height, progress, shakeX, shakeY, zoomX, zoomY);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void capture(Minecraft minecraft, int width, int height) {
        RenderTarget main = minecraft.getMainRenderTarget();
        if (capture == null || capture.width != main.width || capture.height != main.height) {
            if (capture != null) {
                capture.destroyBuffers();
            }
            capture = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            capture.setFilterMode(GL11.GL_NEAREST);
        }

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, capture.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, capture.width, capture.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, main.frameBufferId);
        main.bindWrite(true);

        buildShards(width, height);
        captured = true;
    }

    private static void buildShards(int width, int height) {
        int columns = COLUMNS;
        int rows = Math.max(4, Math.round(columns * height / (float) width));
        float[][] px = new float[columns + 1][rows + 1];
        float[][] py = new float[columns + 1][rows + 1];
        float cellWidth = width / (float) columns;
        float cellHeight = height / (float) rows;

        for (int i = 0; i <= columns; i++) {
            for (int j = 0; j <= rows; j++) {
                float jitterX = i == 0 || i == columns ? 0f : (random.nextFloat() - 0.5f) * cellWidth * 0.7f;
                float jitterY = j == 0 || j == rows ? 0f : (random.nextFloat() - 0.5f) * cellHeight * 0.7f;
                px[i][j] = i * cellWidth + jitterX;
                py[i][j] = j * cellHeight + jitterY;
            }
        }

        Shard[] built = new Shard[columns * rows * 2];
        int index = 0;
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                boolean flip = random.nextBoolean();
                float[][] corners = {
                        {px[i][j], py[i][j]}, {px[i + 1][j], py[i + 1][j]}, {px[i + 1][j + 1], py[i + 1][j + 1]}, {px[i][j + 1], py[i][j + 1]}
                };
                int[][] triangles = flip ? new int[][]{{0, 1, 2}, {0, 2, 3}} : new int[][]{{0, 1, 3}, {1, 2, 3}};
                for (int[] triangle : triangles) {
                    float[] xs = new float[3];
                    float[] ys = new float[3];
                    for (int k = 0; k < 3; k++) {
                        xs[k] = corners[triangle[k]][0];
                        ys[k] = corners[triangle[k]][1];
                    }
                    float centerX = (xs[0] + xs[1] + xs[2]) / 3f;
                    float centerY = (ys[0] + ys[1] + ys[2]) / 3f;
                    built[index++] = createShard(xs, ys, centerX, centerY, width, height);
                }
            }
        }
        shards = built;
    }

    private static Shard createShard(float[] xs, float[] ys, float centerX, float centerY, int width, int height) {
        float depth = centerY / height;
        float crackStart;

        if (depth < FIRST_STAGE_LIMIT) {
            crackStart = 0.02f + depth / FIRST_STAGE_LIMIT * 0.06f;
        } else if (depth < SECOND_STAGE_LIMIT) {
            crackStart = 0.28f + (depth - FIRST_STAGE_LIMIT) / (SECOND_STAGE_LIMIT - FIRST_STAGE_LIMIT) * 0.10f;
        } else {
            crackStart = 0.52f + (depth - SECOND_STAGE_LIMIT) / (1f - SECOND_STAGE_LIMIT) * 0.08f;
        }

        float releaseTime = SHATTER_START + depth * SHATTER_SPREAD + random.nextFloat() * 0.015f;
        float velocityX = (random.nextFloat() - 0.5f) * width * 0.6f;
        float velocityY = -height * (0.15f + random.nextFloat() * 0.5f);
        float spin = (random.nextFloat() - 0.5f) * 7f;
        return new Shard(xs, ys, centerX, centerY, crackStart, releaseTime, velocityX, velocityY, spin);
    }

    private static void ensureNoiseTexture() {
        if (noiseTexture != null) return;

        noiseTexture = new DynamicTexture(new NativeImage(NativeImage.Format.RGBA, NOISE_WIDTH, NOISE_HEIGHT, false));
        noiseTexture.setFilter(false, false);
        Minecraft.getInstance().getTextureManager().register(NOISE_LOCATION, noiseTexture);
    }

    private static void updateNoise(float progress) {
        NativeImage image = noiseTexture.getPixels();
        if (image == null) return;

        float brightness = 0.55f + progress * 0.45f;
        for (int y = 0; y < NOISE_HEIGHT; y++) {
            for (int x = 0; x < NOISE_WIDTH; x++) {
                int gray = (int) (random.nextInt(256) * brightness);
                image.setPixelRGBA(x, y, 0xFF000000 | (gray << 16) | (gray << 8) | gray);
            }
        }

        int glitchRows = 2 + random.nextInt(5);
        for (int i = 0; i < glitchRows; i++) {
            int row = random.nextInt(NOISE_HEIGHT);
            int start = random.nextInt(NOISE_WIDTH / 2);
            int length = NOISE_WIDTH / 4 + random.nextInt(NOISE_WIDTH / 2);
            int color = 0xFF000000 | (random.nextBoolean() ? 0xFF0000 : 0) | (random.nextBoolean() ? 0x00FF00 : 0) | (random.nextBoolean() ? 0x0000FF : 0);
            for (int x = start; x < Math.min(NOISE_WIDTH, start + length); x++) {
                if (random.nextInt(3) != 0) {
                    image.setPixelRGBA(x, row, color);
                }
            }
        }
        noiseTexture.upload();
    }

    private static void drawNoise(Matrix4f matrix, int width, int height) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(matrix, 0f, height, 0f).setUv(0f, 1f);
        builder.addVertex(matrix, width, height, 0f).setUv(1f, 1f);
        builder.addVertex(matrix, width, 0f, 0f).setUv(1f, 0f);
        builder.addVertex(matrix, 0f, 0f, 0f).setUv(0f, 0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, NOISE_LOCATION);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    private static void drawEye(Minecraft minecraft, float progress) {
        ShaderInstance shader = CraftorioShaders.shatterEye();
        float strength = Mth.clamp((progress - SHATTER_START + 0.02f) / 0.14f, 0f, 1f);
        if (shader == null || strength <= 0f) return;

        RenderTarget main = minecraft.getMainRenderTarget();
        shader.safeGetUniform("iResolution").set((float) main.width, (float) main.height);
        shader.safeGetUniform("iTime").set((Util.getMillis() - startMillis) / 1000f);
        shader.safeGetUniform("Strength").set(strength);

        BufferBuilder quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        quad.addVertex(-1f, -1f, 0f);
        quad.addVertex(1f, -1f, 0f);
        quad.addVertex(1f, 1f, 0f);
        quad.addVertex(-1f, 1f, 0f);
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, NOISE_LOCATION);
        BufferUploader.drawWithShader(quad.buildOrThrow());
    }

    private static void drawGlitchBars(Matrix4f matrix, int width, int height, float progress) {
        int bars = 1 + random.nextInt(4) + (int) (progress * 4);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < bars; i++) {
            float y = random.nextFloat() * height;
            float barHeight = 1f + random.nextFloat() * height * 0.04f;
            float x = random.nextFloat() * width * 0.4f;
            float barWidth = width * (0.2f + random.nextFloat() * 0.8f);
            int alpha = 60 + random.nextInt(150);
            int shade = random.nextBoolean() ? 255 : 0;
            builder.addVertex(matrix, x, y + barHeight, 0f).setColor(shade, shade, shade, alpha);
            builder.addVertex(matrix, x + barWidth, y + barHeight, 0f).setColor(shade, shade, shade, alpha);
            builder.addVertex(matrix, x + barWidth, y, 0f).setColor(shade, shade, shade, alpha);
            builder.addVertex(matrix, x, y, 0f).setColor(shade, shade, shade, alpha);
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    private static float shakeAmount(float progress) {
        if (progress >= 0.02f && progress < 0.24f) {
            return 2.5f * (float) Math.sin((progress - 0.02f) / 0.22f * Math.PI);
        }
        if (progress >= 0.28f && progress < 0.50f) {
            return 9f * (float) Math.sin((progress - 0.28f) / 0.22f * Math.PI) + 2f;
        }
        if (progress >= 0.52f && progress < SHATTER_START) {
            return 3f + 9f * (progress - 0.52f) / (SHATTER_START - 0.52f);
        }
        if (progress >= SHATTER_START && progress < 0.92f) {
            return 8f * (1f - (progress - SHATTER_START) / (0.92f - SHATTER_START));
        }
        return 0f;
    }

    private static void drawShards(Matrix4f matrix, int width, int height, float progress, float shakeX, float shakeY, float zoomX, float zoomY) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean any = false;

        for (Shard shard : shards) {
            float local = (progress - shard.releaseTime()) * DURATION_SECONDS;
            float alpha = 1f;
            float offsetX = shakeX;
            float offsetY = shakeY;
            float angle = 0f;
            float scale = 1f;

            if (local > 0f) {
                if (local >= SHARD_LIFE_SECONDS) continue;
                offsetX += shard.velocityX() * local;
                offsetY += shard.velocityY() * local + 0.5f * GRAVITY * height * local * local;
                angle = shard.spin() * local;
                scale = 1f - 0.2f * (local / SHARD_LIFE_SECONDS);
                alpha = 1f - Mth.clamp((local / SHARD_LIFE_SECONDS - 0.6f) / 0.4f, 0f, 1f);
            }

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            int alphaByte = Math.round(alpha * 255f);
            for (int k = 0; k < 3; k++) {
                float relX = (shard.xs()[k] - shard.centerX()) * scale;
                float relY = (shard.ys()[k] - shard.centerY()) * scale;
                float x = shard.centerX() + relX * cos - relY * sin + offsetX;
                float y = shard.centerY() + relX * sin + relY * cos + offsetY;
                builder.addVertex(matrix, (x - width / 2f) * zoomX + width / 2f, (y - height / 2f) * zoomY + height / 2f, 0f)
                        .setUv(shard.xs()[k] / width, 1f - shard.ys()[k] / height)
                        .setColor(255, 255, 255, alphaByte);
            }
            any = true;
        }

        if (!any) return;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, capture.getColorTextureId());
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    private static void drawCracks(Matrix4f matrix, int width, int height, float progress, float shakeX, float shakeY, float zoomX, float zoomY) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        boolean any = false;

        for (Shard shard : shards) {
            if (progress < shard.crackStart() || progress >= shard.releaseTime()) continue;

            float grow = Mth.clamp((progress - shard.crackStart()) / CRACK_GROW_TIME, 0f, 1f);
            for (int k = 0; k < 3; k++) {
                int next = (k + 1) % 3;
                float startX = shard.xs()[k] + shakeX;
                float startY = shard.ys()[k] + shakeY;
                float endX = startX + (shard.xs()[next] + shakeX - startX) * grow;
                float endY = startY + (shard.ys()[next] + shakeY - startY) * grow;
                float x1 = (startX - width / 2f) * zoomX + width / 2f;
                float y1 = (startY - height / 2f) * zoomY + height / 2f;
                float x2 = (endX - width / 2f) * zoomX + width / 2f;
                float y2 = (endY - height / 2f) * zoomY + height / 2f;
                addLine(builder, matrix, x1, y1, x2, y2, 2.2f, 0, 0, 0, 200);
                addLine(builder, matrix, x1, y1, x2, y2, 0.9f, 255, 255, 255, 220);
            }
            any = true;
        }

        if (!any) return;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    private static void addLine(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float thickness, int red, int green, int blue, int alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001f) return;

        float nx = -dy / length * thickness * 0.5f;
        float ny = dx / length * thickness * 0.5f;
        builder.addVertex(matrix, x1 + nx, y1 + ny, 0f).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, x2 + nx, y2 + ny, 0f).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, x2 - nx, y2 - ny, 0f).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, x1 - nx, y1 - ny, 0f).setColor(red, green, blue, alpha);
    }

    private static void playSounds(Minecraft minecraft, float progress) {
        while (soundIndex < SOUND_TIMES.length && progress >= SOUND_TIMES[soundIndex]) {
            float pitch = 0.6f + random.nextFloat() * 0.5f;
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, pitch, 1.0f));
            soundIndex++;
        }
    }
}

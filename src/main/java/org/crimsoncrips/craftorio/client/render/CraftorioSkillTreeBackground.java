package org.crimsoncrips.craftorio.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@OnlyIn(Dist.CLIENT)
public final class CraftorioSkillTreeBackground {

    private static final float RESOLUTION_SCALE = 1.0f;
    private static final float INTENSITY = 1.0f;
    private static final long TIME_WRAP_MS = 3_600_000L;
    private static final int NOISE_SIZE = 256;
    private static final long NOISE_SEED = 918273645L;

    private static RenderTarget target;
    private static int noiseTextureId = -1;
    private static long epochMillis = -1L;

    private CraftorioSkillTreeBackground() {}

    public static void render(GuiGraphics graphics, int width, int height, double panX, double panY, double zoom) {
        ShaderInstance shader = CraftorioShaders.skillTreeStarfield();
        if (shader == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        int targetWidth = Math.max(1, Math.round(minecraft.getWindow().getWidth() * RESOLUTION_SCALE));
        int targetHeight = Math.max(1, Math.round(minecraft.getWindow().getHeight() * RESOLUTION_SCALE));

        graphics.flush();

        if (target == null) {
            target = new TextureTarget(targetWidth, targetHeight, false, Minecraft.ON_OSX);
            target.setFilterMode(GL11.GL_LINEAR);
        } else if (target.width != targetWidth || target.height != targetHeight) {
            target.resize(targetWidth, targetHeight, Minecraft.ON_OSX);
        }

        long now = Util.getMillis();
        if (epochMillis < 0L) {
            epochMillis = now;
        }
        float time = ((now - epochMillis) % TIME_WRAP_MS) / 1000f;

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        target.bindWrite(true);
        shader.safeGetUniform("iResolution").set((float) targetWidth, (float) targetHeight);
        shader.safeGetUniform("iTime").set(time);
        shader.safeGetUniform("Pan").set((float) (panX / width), (float) (panY / width));
        shader.safeGetUniform("Zoom").set((float) zoom);
        shader.safeGetUniform("Intensity").set(INTENSITY);

        BufferBuilder quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        quad.addVertex(-1f, -1f, 0f);
        quad.addVertex(1f, -1f, 0f);
        quad.addVertex(1f, 1f, 0f);
        quad.addVertex(-1f, 1f, 0f);
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, noiseTexture());
        BufferUploader.drawWithShader(quad.buildOrThrow());

        minecraft.getMainRenderTarget().bindWrite(true);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder screen = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        screen.addVertex(matrix, 0f, height, 0f).setUv(0f, 0f);
        screen.addVertex(matrix, width, height, 0f).setUv(1f, 0f);
        screen.addVertex(matrix, width, 0f, 0f).setUv(1f, 1f);
        screen.addVertex(matrix, 0f, 0f, 0f).setUv(0f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());
        BufferUploader.drawWithShader(screen.buildOrThrow());

        RenderSystem.enableDepthTest();
    }

    private static int noiseTexture() {
        if (noiseTextureId != -1) return noiseTextureId;

        RandomSource random = RandomSource.create(NOISE_SEED);
        ByteBuffer pixels = MemoryUtil.memAlloc(NOISE_SIZE * NOISE_SIZE * 4);
        try {
            for (int i = 0; i < NOISE_SIZE * NOISE_SIZE; i++) {
                byte value = (byte) random.nextInt(256);
                pixels.put(value).put(value).put(value).put((byte) 255);
            }
            pixels.flip();

            int id = TextureUtil.generateTextureId();
            GlStateManager._bindTexture(id);
            GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, 4);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, NOISE_SIZE, NOISE_SIZE, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            noiseTextureId = id;
        } finally {
            MemoryUtil.memFree(pixels);
        }
        return noiseTextureId;
    }
}

package org.crimsoncrips.craftorio.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public final class CraftorioSkillTreeBackground {

    private static final float RESOLUTION_SCALE = 0.5f;
    private static final float INTENSITY = 0.2f;

    private static RenderTarget target;
    private static ShaderInstance renderedWith;
    private static float renderedPanX = Float.NaN;
    private static float renderedPanY = Float.NaN;
    private static float renderedZoom = Float.NaN;

    private CraftorioSkillTreeBackground() {}

    public static void render(GuiGraphics graphics, int width, int height, double panX, double panY, double zoom) {
        ShaderInstance shader = CraftorioShaders.skillTreeStarNest();
        if (shader == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        int targetWidth = Math.max(1, Math.round(minecraft.getWindow().getWidth() * RESOLUTION_SCALE));
        int targetHeight = Math.max(1, Math.round(minecraft.getWindow().getHeight() * RESOLUTION_SCALE));
        float normalizedPanX = (float) (panX / width);
        float normalizedPanY = (float) (panY / width);

        float zoomValue = (float) zoom;

        graphics.flush();

        boolean needsRender = false;
        if (target == null) {
            target = new TextureTarget(targetWidth, targetHeight, false, Minecraft.ON_OSX);
            target.setFilterMode(GL11.GL_LINEAR);
            needsRender = true;
        } else if (target.width != targetWidth || target.height != targetHeight) {
            target.resize(targetWidth, targetHeight, Minecraft.ON_OSX);
            needsRender = true;
        }
        if (renderedWith != shader || renderedPanX != normalizedPanX || renderedPanY != normalizedPanY || renderedZoom != zoomValue) {
            needsRender = true;
        }

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        if (needsRender) {
            renderedWith = shader;
            renderedPanX = normalizedPanX;
            renderedPanY = normalizedPanY;
            renderedZoom = zoomValue;

            target.bindWrite(true);
            shader.safeGetUniform("iResolution").set((float) targetWidth, (float) targetHeight);
            shader.safeGetUniform("Pan").set(normalizedPanX, normalizedPanY);
            shader.safeGetUniform("Zoom").set(zoomValue);
            shader.safeGetUniform("Intensity").set(INTENSITY);

            BufferBuilder quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            quad.addVertex(-1f, -1f, 0f);
            quad.addVertex(1f, -1f, 0f);
            quad.addVertex(1f, 1f, 0f);
            quad.addVertex(-1f, 1f, 0f);
            RenderSystem.setShader(() -> shader);
            BufferUploader.drawWithShader(quad.buildOrThrow());

            minecraft.getMainRenderTarget().bindWrite(true);
        }

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
}

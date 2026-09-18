package org.crimsoncrips.craftorio.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CraftorioHavenSkyEffects extends DimensionSpecialEffects {

    private static final ResourceLocation HAVEN_SKY_LOCATION = Craftorio.prefix("textures/environment/haven_sky.png");

    public CraftorioHavenSkyEffects() {
        super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, true, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        if (isFoggy) {
            return true;
        }

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, HAVEN_SKY_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);

        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            if (i == 1) poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (i == 2) poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            if (i == 3) poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            if (i == 4) poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            if (i == 5) poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));

            Matrix4f matrix = poseStack.last().pose();
            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex(matrix, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(-1);
            bufferBuilder.addVertex(matrix, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(-1);
            bufferBuilder.addVertex(matrix, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(-1);
            bufferBuilder.addVertex(matrix, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(-1);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        return true;
    }
}

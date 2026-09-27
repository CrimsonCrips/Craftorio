package org.crimsoncrips.craftorio.client.screen.widget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.client.render.CraftorioShaders;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class AssemblingButton extends Button {

    private static final long FORM_DURATION_MS = 550L;
    private static final float MIN_TRANSITION_FRACTION = 0.25f;
    private static final long SHAKE_DURATION_MS = 900L;
    private static final float SHAKE_AMPLITUDE = 4f;
    private static final float CELL_SIZE = 16f / (float) Math.sqrt(0.4);
    private static final float ORBIT_MIN = 0.35f * 1.4f;
    private static final float ORBIT_MAX = 1.1f * 1.4f;
    private static final float ORBIT_SPEED_MIN = 0.0006f;
    private static final float ORBIT_SPEED_MAX = 0.0012f;
    private static final float SPIN_SPEED_RANGE = 0.003f;
    private static final double TWO_PI = Math.PI * 2.0;

    private static final float MOTION_SPEED = 0.3f;
    private static final float SWING_DEGREES = 12f;
    private static final float SWING_PERIOD_MS = 3400f;
    private static final float SCALE_AMPLITUDE = 0.12f;
    private static final float SCALE_PERIOD_MS = 2600f;
    private static final float DRIFT_RADIUS = 1.6f;
    private static final float DRIFT_PERIOD_X_MS = 5200f;
    private static final float DRIFT_PERIOD_Y_MS = 4100f;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final float OUTLINE_WIDTH = 0.5f;
    private static final int TEXT_HEIGHT = 8;
    private static final float GLOW_VIRTUAL_HEIGHT = 420f;
    private static final float GLOW_STRENGTH = 2.5f;
    private static final float GLOW_OPACITY = 0.6f;
    private static final float GLOW_FADE_MS = 150f;
    private static final float AURA_PATTERN_UNIT = 11f;
    private static final float ELECTRIC_OPACITY = 1.0f;
    private static final float ELECTRIC_MARGIN = 8f;
    private static final float ELECTRIC_DISSIPATION = 1.7f;
    private static final float PULSE_OPACITY = 0.9f;
    private static final float PULSE_EXTRA_REACH = 10f;
    private static final float PULSE_RING_WIDTH = 2f;

    private record Shard(float[] xs, float[] ys, float centerX, float centerY,
                         float orbitRadiusX, float orbitRadiusY, float orbitPhase, float orbitSpeed,
                         float spinPhase, float spinSpeed, float revealOrder) {}

    private final RandomSource random = RandomSource.create();
    private final float textScale;

    private boolean forming;
    private float eased;
    private float fromEased;
    private float progress = 1f;
    private float transitionMs = FORM_DURATION_MS;
    private long shakeStartMillis;
    private float glowAmount;
    private float lastShakeX;
    private float lastShakeY;
    private long lastElapsed;
    private long startMillis;
    private long lastFrameMillis;
    private TextureTarget buttonCapture;
    private TextureTarget textCapture;
    private double captureGuiScale;
    private float orbitCenterX;
    private float orbitCenterY;
    private Shard[] shards = new Shard[0];
    private float revealFraction = 1f;

    public AssemblingButton(Button.Builder builder, float textScale) {
        super(builder);
        this.textScale = textScale;
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.buttonCapture == null) {
            captureSelf(guiGraphics, mouseX, mouseY, partialTick);
            buildShards();
            this.startMillis = Util.getMillis();
            this.lastFrameMillis = this.startMillis;
        }

        long now = Util.getMillis();
        float dt = Math.max(0L, now - this.lastFrameMillis);
        this.lastFrameMillis = now;

        boolean hovered = this.isHovered() && isFullyRevealed();
        if (hovered != this.forming) {
            this.forming = hovered;
            this.fromEased = this.eased;
            this.progress = 0f;
            float distance = this.forming ? 1f - this.eased : Math.abs(this.eased);
            this.transitionMs = FORM_DURATION_MS * Math.max(MIN_TRANSITION_FRACTION, Math.min(1f, distance));
        }

        if (this.progress < 1f) {
            this.progress = Math.min(1f, this.progress + dt / this.transitionMs);
            if (this.forming) {
                this.eased = this.fromEased + (1f - this.fromEased) * easeInExpo(this.progress);
                if (this.progress >= 1f) {
                    this.shakeStartMillis = now;
                }
            } else {
                this.eased = this.fromEased * (1f - easeOutBack(this.progress));
            }
        }

        boolean formed = this.forming && this.progress >= 1f;
        float glowStep = dt / GLOW_FADE_MS;
        this.glowAmount = formed ? Math.min(1f, this.glowAmount + glowStep) : Math.max(0f, this.glowAmount - glowStep);

        long elapsed = now - this.startMillis;
        this.lastElapsed = elapsed;
        this.lastShakeX = 0f;
        this.lastShakeY = 0f;
        PoseStack pose = guiGraphics.pose();
        if (formed) {
            float shakeProgress = (now - this.shakeStartMillis) / (float) SHAKE_DURATION_MS;
            float shake = shakeProgress < 1f ? SHAKE_AMPLITUDE * (1f - shakeProgress) * (1f - shakeProgress) : 0f;
            float shakeX = (this.random.nextFloat() - 0.5f) * 2f * shake;
            float shakeY = (this.random.nextFloat() - 0.5f) * 2f * shake;
            this.lastShakeX = shakeX;
            this.lastShakeY = shakeY;
            pose.pushPose();
            pose.translate(shakeX, shakeY, 0f);
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            pose.popPose();
            drawGlow(guiGraphics, elapsed, this.glowAmount);
            pose.pushPose();
            pose.translate(shakeX, shakeY, 0f);
            pose.pushPose();
            applyTextMotion(pose, now, 1f);
            drawText(guiGraphics);
            pose.popPose();
            pose.popPose();
        } else {
            float decay = 1f - this.eased;
            float textBlend = Mth.clamp(this.eased, 0f, 1f);
            drawShards(guiGraphics, this.buttonCapture, decay, elapsed);
            drawGlow(guiGraphics, elapsed, this.glowAmount);
            pose.pushPose();
            applyTextMotion(pose, now, textBlend);
            drawShards(guiGraphics, this.textCapture, decay, elapsed);
            pose.popPose();
        }
    }

    public void setRevealFraction(float fraction) {
        this.revealFraction = Mth.clamp(fraction, 0f, 1f);
        this.active = isFullyRevealed();
    }

    public boolean isFullyRevealed() {
        return this.revealFraction >= 1f;
    }

    public void release() {
        if (this.buttonCapture != null) {
            this.buttonCapture.destroyBuffers();
            this.buttonCapture = null;
        }
        if (this.textCapture != null) {
            this.textCapture.destroyBuffers();
            this.textCapture = null;
        }
    }

    private static float easeInExpo(float x) {
        return x <= 0f ? 0f : (float) Math.pow(2.0, 10.0 * x - 10.0);
    }

    private static float easeOutBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float shifted = x - 1f;
        return 1f + c3 * shifted * shifted * shifted + c1 * shifted * shifted;
    }

    private void applyTextMotion(PoseStack pose, long now, float blend) {
        float phase = now * MOTION_SPEED;
        float swing = (float) Math.sin(phase / SWING_PERIOD_MS * TWO_PI) * SWING_DEGREES * blend;
        float scale = 1f + (float) Math.sin(phase / SCALE_PERIOD_MS * TWO_PI) * SCALE_AMPLITUDE * blend;
        float driftX = (float) Math.sin(phase / DRIFT_PERIOD_X_MS * TWO_PI) * DRIFT_RADIUS * blend;
        float driftY = (float) Math.cos(phase / DRIFT_PERIOD_Y_MS * TWO_PI) * DRIFT_RADIUS * blend;

        float pivotX = getX() + getWidth() / 2f;
        float pivotY = getY() + getHeight() / 2f;

        pose.translate(pivotX + driftX, pivotY + driftY, 0f);
        pose.mulPose(Axis.ZP.rotationDegrees(swing));
        pose.scale(scale, scale, 1f);
        pose.translate(-pivotX, -pivotY, 0f);
    }

    private float textHalfWidth() {
        return Minecraft.getInstance().font.width(getMessage().getString()) * this.textScale / 2f;
    }

    private float textHalfHeight() {
        return TEXT_HEIGHT * this.textScale / 2f;
    }

    private void drawText(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        String text = getMessage().getString();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0f);
        pose.scale(this.textScale, this.textScale, 1f);
        int x = -font.width(text) / 2;
        int y = -TEXT_HEIGHT / 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    pose.pushPose();
                    pose.translate(dx * OUTLINE_WIDTH, dy * OUTLINE_WIDTH, 0f);
                    guiGraphics.drawString(font, text, x, y, OUTLINE_COLOR, false);
                    pose.popPose();
                }
            }
        }
        guiGraphics.drawString(font, text, x, y, TEXT_COLOR, false);
        pose.popPose();
    }

    private void drawGlow(GuiGraphics guiGraphics, long elapsed, float strength) {
        ShaderInstance shader = CraftorioShaders.sacrificeGlow();
        if (shader == null || strength <= 0.001f) return;

        guiGraphics.flush();

        float left = getX();
        float right = getX() + getWidth();
        float top = getY();
        float bottom = getY() + getHeight();
        Matrix4f matrix = guiGraphics.pose().last().pose();

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(matrix, left, bottom, 0f).setUv(0f, 0f);
        builder.addVertex(matrix, right, bottom, 0f).setUv(1f, 0f);
        builder.addVertex(matrix, right, top, 0f).setUv(1f, 1f);
        builder.addVertex(matrix, left, top, 0f).setUv(0f, 1f);

        shader.safeGetUniform("iTime").set(elapsed / 1000f);
        shader.safeGetUniform("VirtualSize").set(GLOW_VIRTUAL_HEIGHT * (right - left) / (bottom - top), GLOW_VIRTUAL_HEIGHT);
        shader.safeGetUniform("Strength").set(GLOW_STRENGTH);
        shader.safeGetUniform("Opacity").set(GLOW_OPACITY * strength);

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(() -> shader);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    private void captureSelf(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        this.captureGuiScale = minecraft.getWindow().getGuiScale();

        guiGraphics.flush();

        this.buttonCapture = createCaptureTarget(main);
        this.buttonCapture.bindWrite(true);
        boolean wasActive = this.active;
        this.active = true;
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.active = wasActive;
        guiGraphics.flush();

        this.textCapture = createCaptureTarget(main);
        this.textCapture.bindWrite(true);
        drawText(guiGraphics);
        guiGraphics.flush();

        main.bindWrite(true);
    }

    private static TextureTarget createCaptureTarget(RenderTarget main) {
        TextureTarget target = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        target.setFilterMode(GL11.GL_NEAREST);
        target.setClearColor(0f, 0f, 0f, 0f);
        target.clear(Minecraft.ON_OSX);
        return target;
    }

    private void buildShards() {
        this.orbitCenterX = getX() + getWidth() / 2f;
        this.orbitCenterY = getY() + getHeight() / 2f;

        float left = Math.min(getX(), this.orbitCenterX - textHalfWidth()) - 2f;
        float top = Math.min(getY(), this.orbitCenterY - textHalfHeight()) - 2f;
        float right = Math.max(getX() + getWidth(), this.orbitCenterX + textHalfWidth()) + 2f;
        float bottom = Math.max(getY() + getHeight(), this.orbitCenterY + textHalfHeight()) + 2f;
        float width = right - left;
        float height = bottom - top;
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        int columns = Math.max(2, Math.round(width / CELL_SIZE));
        int rows = Math.max(2, Math.round(height / CELL_SIZE));
        float[][] px = new float[columns + 1][rows + 1];
        float[][] py = new float[columns + 1][rows + 1];
        float cellWidth = width / columns;
        float cellHeight = height / rows;

        for (int i = 0; i <= columns; i++) {
            for (int j = 0; j <= rows; j++) {
                float jitterX = i == 0 || i == columns ? 0f : (this.random.nextFloat() - 0.5f) * cellWidth * 0.6f;
                float jitterY = j == 0 || j == rows ? 0f : (this.random.nextFloat() - 0.5f) * cellHeight * 0.6f;
                px[i][j] = left + i * cellWidth + jitterX;
                py[i][j] = top + j * cellHeight + jitterY;
            }
        }

        Shard[] built = new Shard[columns * rows * 2];
        int index = 0;
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                boolean flip = this.random.nextBoolean();
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
                    float shardX = (xs[0] + xs[1] + xs[2]) / 3f;
                    float shardY = (ys[0] + ys[1] + ys[2]) / 3f;
                    float orbitScale = ORBIT_MIN + this.random.nextFloat() * (ORBIT_MAX - ORBIT_MIN);
                    built[index++] = new Shard(xs, ys, shardX, shardY,
                            halfWidth * orbitScale, halfHeight * orbitScale,
                            this.random.nextFloat() * (float) TWO_PI,
                            ORBIT_SPEED_MIN + this.random.nextFloat() * (ORBIT_SPEED_MAX - ORBIT_SPEED_MIN),
                            this.random.nextFloat() * (float) TWO_PI,
                            (this.random.nextFloat() - 0.5f) * 2f * SPIN_SPEED_RANGE,
                            this.random.nextFloat());
                }
            }
        }
        this.shards = built;
    }

    public void renderAura(GuiGraphics guiGraphics) {
        float strength = this.glowAmount;
        if (strength <= 0.001f) return;

        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(this.lastShakeX, this.lastShakeY, 0f);

        ShaderInstance pulse = CraftorioShaders.sacrificePulse();
        if (pulse != null) {
            float reach = ELECTRIC_MARGIN + PULSE_EXTRA_REACH;
            pulse.safeGetUniform("Reach").set(reach * scale);
            pulse.safeGetUniform("RingWidth").set(PULSE_RING_WIDTH * scale);
            drawAuraLayer(guiGraphics, pulse, getX() - reach, getY() - reach,
                    getX() + getWidth() + reach, getY() + getHeight() + reach, scale, PULSE_OPACITY * strength);
        }

        ShaderInstance electric = CraftorioShaders.sacrificeElectric();
        if (electric != null) {
            electric.safeGetUniform("Margin").set(ELECTRIC_MARGIN * scale);
            electric.safeGetUniform("Dissipation").set(ELECTRIC_DISSIPATION * scale);
            electric.safeGetUniform("PatternUnit").set(AURA_PATTERN_UNIT * scale);
            drawAuraLayer(guiGraphics, electric, getX() - ELECTRIC_MARGIN, getY() - ELECTRIC_MARGIN,
                    getX() + getWidth() + ELECTRIC_MARGIN, getY() + getHeight() + ELECTRIC_MARGIN, scale, ELECTRIC_OPACITY * strength);
        }

        pose.popPose();
    }

    private void drawAuraLayer(GuiGraphics guiGraphics, ShaderInstance shader, float left, float top, float right, float bottom, float scale, float opacity) {
        guiGraphics.flush();

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(matrix, left, bottom, 0f).setUv(0f, 0f);
        builder.addVertex(matrix, right, bottom, 0f).setUv(1f, 0f);
        builder.addVertex(matrix, right, top, 0f).setUv(1f, 1f);
        builder.addVertex(matrix, left, top, 0f).setUv(0f, 1f);

        shader.safeGetUniform("iTime").set(this.lastElapsed / 1000f);
        shader.safeGetUniform("RegionSize").set((right - left) * scale, (bottom - top) * scale);
        shader.safeGetUniform("BoxSize").set(getWidth() * scale, getHeight() * scale);
        shader.safeGetUniform("Opacity").set(opacity);

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    private void drawShards(GuiGraphics guiGraphics, TextureTarget texture, float decay, long elapsed) {
        if (texture == null) return;

        guiGraphics.flush();

        float textureWidth = texture.width;
        float textureHeight = texture.height;
        float scale = (float) this.captureGuiScale;
        float eased = 1f - decay;
        Matrix4f matrix = guiGraphics.pose().last().pose();

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        for (Shard shard : this.shards) {
            if (shard.revealOrder() >= this.revealFraction) continue;

            double orbitAngle = shard.orbitPhase() + elapsed * (double) shard.orbitSpeed();
            float orbitX = this.orbitCenterX + (float) Math.cos(orbitAngle) * shard.orbitRadiusX();
            float orbitY = this.orbitCenterY + (float) Math.sin(orbitAngle) * shard.orbitRadiusY();
            float positionX = orbitX + (shard.centerX() - orbitX) * eased;
            float positionY = orbitY + (shard.centerY() - orbitY) * eased;
            float rotation = (float) Math.IEEEremainder(shard.spinPhase() + elapsed * (double) shard.spinSpeed(), TWO_PI) * decay;

            float cos = (float) Math.cos(rotation);
            float sin = (float) Math.sin(rotation);

            for (int k = 0; k < 3; k++) {
                float relX = shard.xs()[k] - shard.centerX();
                float relY = shard.ys()[k] - shard.centerY();
                float x = positionX + relX * cos - relY * sin;
                float y = positionY + relX * sin + relY * cos;
                builder.addVertex(matrix, x, y, 0f)
                        .setUv(shard.xs()[k] * scale / textureWidth, 1f - shard.ys()[k] * scale / textureHeight);
            }
        }

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture.getColorTextureId());
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }
}

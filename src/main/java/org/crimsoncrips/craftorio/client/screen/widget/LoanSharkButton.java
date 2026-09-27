package org.crimsoncrips.craftorio.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.render.CraftorioShaders;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.joml.Matrix4f;

import java.math.BigInteger;

@OnlyIn(Dist.CLIENT)
public class LoanSharkButton extends Button {

    private static final float MOTION_SPEED = 0.3f;

    private static final float SWING_DEGREES = 12f;
    private static final float SWING_PERIOD_MS = 3400f;
    private static final float SCALE_AMPLITUDE = 0.12f;
    private static final float SCALE_PERIOD_MS = 2600f;
    private static final float DRIFT_RADIUS = 1.6f;
    private static final float DRIFT_PERIOD_X_MS = 5200f;
    private static final float DRIFT_PERIOD_Y_MS = 4100f;

    private static final int AURA_MARGIN = 16;
    private static final int REFERENCE_DEBT_DIGITS = 9;

    public LoanSharkButton(Button.Builder builder) {
        super(builder);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        float phase = now * MOTION_SPEED;

        float swing = (float) Math.sin(phase / SWING_PERIOD_MS * (Math.PI * 2)) * SWING_DEGREES;
        float scale = 1f + (float) Math.sin(phase / SCALE_PERIOD_MS * (Math.PI * 2)) * SCALE_AMPLITUDE;
        float driftX = (float) Math.sin(phase / DRIFT_PERIOD_X_MS * (Math.PI * 2)) * DRIFT_RADIUS;
        float driftY = (float) Math.cos(phase / DRIFT_PERIOD_Y_MS * (Math.PI * 2)) * DRIFT_RADIUS;

        float pivotX = getX() + getWidth() / 2f;
        float pivotY = getY() + getHeight() / 2f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(pivotX + driftX, pivotY + driftY, 0f);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(swing));
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate(-pivotX, -pivotY, 0f);

        drawAura(guiGraphics, now);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().popPose();
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        int size = ClientEvents.STATUS_ICON_SIZE;
        int iconX = getX() + (getWidth() - size) / 2;
        int iconY = getY() + (getHeight() - size) / 2;
        guiGraphics.blit(ClientEvents.STATUS_ICONS, iconX, iconY, ClientEvents.LOAN_SHARK_ICON_U, ClientEvents.LOAN_SHARK_ICON_V, size, size,
                ClientEvents.STATUS_ICON_SHEET_WIDTH, ClientEvents.STATUS_ICON_SHEET_HEIGHT);
    }

    private void drawAura(GuiGraphics guiGraphics, long now) {
        ShaderInstance shader = CraftorioShaders.loanSharkAura();
        if (shader == null) return;

        float intensity = debtIntensity();
        if (intensity <= 0.002f) return;

        float left = getX() - AURA_MARGIN;
        float top = getY() - AURA_MARGIN;
        float right = getX() + getWidth() + AURA_MARGIN;
        float bottom = getY() + getHeight() + AURA_MARGIN;
        Matrix4f matrix = guiGraphics.pose().last().pose();

        BufferBuilder builder;
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(matrix, left, bottom, 0f).setUv(0f, 1f);
            builder.addVertex(matrix, right, bottom, 0f).setUv(1f, 1f);
            builder.addVertex(matrix, right, top, 0f).setUv(1f, 0f);
            builder.addVertex(matrix, left, top, 0f).setUv(0f, 0f);

            MeshData mesh = builder.buildOrThrow();
            shader.safeGetUniform("AuraParams").set(now / 1000f, intensity);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> shader);
            BufferUploader.drawWithShader(mesh);
        }
    }

    private static float debtIntensity() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0f;

        BigInteger owed = CraftorioMisc.getLoanOwed(player);
        if (owed.signum() <= 0) return 0f;

        int digits = owed.toString().length();
        return Mth.clamp((digits - 1) / (float) (REFERENCE_DEBT_DIGITS - 1), 0f, 1f);
    }
}

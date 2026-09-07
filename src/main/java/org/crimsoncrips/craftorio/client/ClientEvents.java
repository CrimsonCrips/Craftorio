package org.crimsoncrips.craftorio.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.SinkScreen;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.List;

public class ClientEvents {

	@SubscribeEvent
	public void registerScreens(RegisterMenuScreensEvent event) {
		event.register(CraftorioMenuTypes.SINKER.get(), SinkScreen::new);
	}

	private static final ResourceLocation FORCEFIELD_TEXTURE = Craftorio.prefix("textures/forcefield.png");



	@SubscribeEvent
	public static void renderBorders(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		List<CraftorioBorder> borders = CraftorioMisc.getCraftorioBorders(mc.player);
		if (borders.isEmpty()) return;

		PoseStack poseStack = event.getPoseStack();
		Camera camPos = event.getCamera();

		for (CraftorioBorder border : borders) {
			if (border.getDimension().equals(mc.player.level().dimension())) {
				renderBorderWall(mc, poseStack, camPos, border);
			}
		}
		
	}


	private static void renderBorderWall(Minecraft minecraft, PoseStack poseStack, Camera camera, CraftorioBorder border) {
		double d0 = (double)(minecraft.options.getEffectiveRenderDistance() * 16);
		if (!(camera.getPosition().x < border.getMaxX() - d0) || !(camera.getPosition().x > border.getMinX() + d0) || !(camera.getPosition().z < border.getMaxZ() - d0) || !(camera.getPosition().z > border.getMinZ() + d0)) {
			double d1 = (double)1.0F - border.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d0;
			d1 = Math.pow(d1, (double)4.0F);
			d1 = Mth.clamp(d1, (double)0.0F, (double)1.0F);
			double d2 = camera.getPosition().x;
			double d3 = camera.getPosition().z;
			double d4 = minecraft.gameRenderer.getDepthFar();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderTexture(0, FORCEFIELD_TEXTURE);
			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			int i = border.getStatus().getColor();
			float f = (float)(i >> 16 & 255) / 255.0F;
			float f1 = (float)(i >> 8 & 255) / 255.0F;
			float f2 = (float)(i & 255) / 255.0F;
			RenderSystem.setShaderColor(f, f1, f2, (float)d1);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.polygonOffset(-3.0F, -3.0F);
			RenderSystem.enablePolygonOffset();
			RenderSystem.disableCull();
			float f3 = (float)(Util.getMillis() % 3000L) / 3000.0F;
			float f4 = (float)(-Mth.frac(camera.getPosition().y * (double)0.5F));
			float f5 = f4 + (float)d4;
			BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			double d5 = Math.max((double)Mth.floor(d3 - d0), border.getMinZ());
			double d6 = Math.min((double)Mth.ceil(d3 + d0), border.getMaxZ());
			float f6 = (float)(Mth.floor(d5) & 1) * 0.5F;
			if (d2 > border.getMaxX() - d0) {
				float f7 = f6;

				for(double d7 = d5; d7 < d6; f7 += 0.5F) {
					double d8 = Math.min((double)1.0F, d6 - d7);
					float f8 = (float)d8 * 0.5F;
					bufferbuilder.addVertex((float)(border.getMaxX() - d2), (float)(-d4), (float)(d7 - d3)).setUv(f3 - f7, f3 + f5);
					bufferbuilder.addVertex((float)(border.getMaxX() - d2), (float)(-d4), (float)(d7 + d8 - d3)).setUv(f3 - (f8 + f7), f3 + f5);
					bufferbuilder.addVertex((float)(border.getMaxX() - d2), (float)d4, (float)(d7 + d8 - d3)).setUv(f3 - (f8 + f7), f3 + f4);
					bufferbuilder.addVertex((float)(border.getMaxX() - d2), (float)d4, (float)(d7 - d3)).setUv(f3 - f7, f3 + f4);
					++d7;
				}
			}

			if (d2 < border.getMinX() + d0) {
				float f9 = f6;

				for(double d9 = d5; d9 < d6; f9 += 0.5F) {
					double d12 = Math.min((double)1.0F, d6 - d9);
					float f12 = (float)d12 * 0.5F;
					bufferbuilder.addVertex((float)(border.getMinX() - d2), (float)(-d4), (float)(d9 - d3)).setUv(f3 + f9, f3 + f5);
					bufferbuilder.addVertex((float)(border.getMinX() - d2), (float)(-d4), (float)(d9 + d12 - d3)).setUv(f3 + f12 + f9, f3 + f5);
					bufferbuilder.addVertex((float)(border.getMinX() - d2), (float)d4, (float)(d9 + d12 - d3)).setUv(f3 + f12 + f9, f3 + f4);
					bufferbuilder.addVertex((float)(border.getMinX() - d2), (float)d4, (float)(d9 - d3)).setUv(f3 + f9, f3 + f4);
					++d9;
				}
			}

			d5 = Math.max((double)Mth.floor(d2 - d0), border.getMinX());
			d6 = Math.min((double)Mth.ceil(d2 + d0), border.getMaxX());
			f6 = (float)(Mth.floor(d5) & 1) * 0.5F;
			if (d3 > border.getMaxZ() - d0) {
				float f10 = f6;

				for(double d10 = d5; d10 < d6; f10 += 0.5F) {
					double d13 = Math.min((double)1.0F, d6 - d10);
					float f13 = (float)d13 * 0.5F;
					bufferbuilder.addVertex((float)(d10 - d2), (float)(-d4), (float)(border.getMaxZ() - d3)).setUv(f3 + f10, f3 + f5);
					bufferbuilder.addVertex((float)(d10 + d13 - d2), (float)(-d4), (float)(border.getMaxZ() - d3)).setUv(f3 + f13 + f10, f3 + f5);
					bufferbuilder.addVertex((float)(d10 + d13 - d2), (float)d4, (float)(border.getMaxZ() - d3)).setUv(f3 + f13 + f10, f3 + f4);
					bufferbuilder.addVertex((float)(d10 - d2), (float)d4, (float)(border.getMaxZ() - d3)).setUv(f3 + f10, f3 + f4);
					++d10;
				}
			}

			if (d3 < border.getMinZ() + d0) {
				float f11 = f6;

				for(double d11 = d5; d11 < d6; f11 += 0.5F) {
					double d14 = Math.min((double)1.0F, d6 - d11);
					float f14 = (float)d14 * 0.5F;
					bufferbuilder.addVertex((float)(d11 - d2), (float)(-d4), (float)(border.getMinZ() - d3)).setUv(f3 - f11, f3 + f5);
					bufferbuilder.addVertex((float)(d11 + d14 - d2), (float)(-d4), (float)(border.getMinZ() - d3)).setUv(f3 - (f14 + f11), f3 + f5);
					bufferbuilder.addVertex((float)(d11 + d14 - d2), (float)d4, (float)(border.getMinZ() - d3)).setUv(f3 - (f14 + f11), f3 + f4);
					bufferbuilder.addVertex((float)(d11 - d2), (float)d4, (float)(border.getMinZ() - d3)).setUv(f3 - f11, f3 + f4);
					++d11;
				}
			}

			MeshData meshdata = bufferbuilder.build();
			if (meshdata != null) {
				BufferUploader.drawWithShader(meshdata);
			}

			RenderSystem.enableCull();
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.depthMask(true);
		}
	}


	//From Improved Mobs
	private static final ResourceLocation pointBar = Craftorio.getGuiTexture("textures/gui/points_bar.png");

	public static void displayPoints(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.gui.getDebugOverlay().showDebugScreen())
			return;
		if (minecraft.player == null)
			return;


		Font font = minecraft.font;
		BigInteger actualPoints = CraftorioMisc.getPoints(minecraft.player);
		BigInteger tempPoints = CraftorioMisc.getTempPoints(minecraft.player);

		PointsAnimation.tick(actualPoints, tempPoints);

		BigInteger displayValue = PointsAnimation.getDisplayValue();
		String pointsString = CraftorioMisc.bigIntFormat(displayValue, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
		String INFINITY_TEXT = CraftorioMisc.bigIntFormat(CraftorioMisc.pointThreshold(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
		String NEG_INFINITY_TEXT = "-" + CraftorioMisc.bigIntFormat(CraftorioMisc.pointThreshold(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());

		int width = font.width(pointsString);
		int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int edgeMargin = 12;
		int y = 5;

		int x;
		float pivotX;

		if (!Craftorio.CLIENT_CONFIG.POINT_BAR_LOCATION.getAsBoolean()) {
			x = edgeMargin;
			pivotX = edgeMargin;
		} else {
			int rightEdge = screenWidth - edgeMargin;
			x = rightEdge - width;
			pivotX = rightEdge;
		}

		float scale = PointsAnimation.getScale();
		float[] shake = PointsAnimation.getShakeOffset();

		float pivotY = y + 4 + 5;

		graphics.pose().pushPose();
		graphics.pose().translate(pivotX + shake[0], pivotY + shake[1], 0);
		graphics.pose().scale(scale, scale, scale);
		graphics.pose().translate(-pivotX, -pivotY, 0);

		if (pointsString.equals(INFINITY_TEXT)) {
			CraftorioMisc.CraftorioTextEffects.drawFancy(graphics, font, pointsString, x + 4, y + 5, true, 0);
		} else if (pointsString.equals(NEG_INFINITY_TEXT)) {
			CraftorioMisc.CraftorioTextEffects.drawFancy(graphics, font, pointsString, x + 4, y + 5, true, 1);
		} else {
			graphics.drawString(font, pointsString, x + 4, y + 5, 16759552, true);
		}

		graphics.pose().popPose();
	}


	public static void showPoints(RegisterGuiLayersEvent e) {
		e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, pointBar,
				(graphics, partialTicks) -> ClientEvents.displayPoints(graphics));
	}
}

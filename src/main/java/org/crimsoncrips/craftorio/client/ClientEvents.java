package org.crimsoncrips.craftorio.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.SinkScreen;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.ArrayList;
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


	private static final double CLAIM_BORDER_FADE_DISTANCE = 16.0;
	private static final int CLAIM_BORDER_COLOR = 0x30D5C8;

	private record ClaimBorderSegment(boolean alongX, double planeCoord, double tangentStart, double tangentEnd, double distance) {}

	@SubscribeEvent
	public static void renderClaimedChunkBorders(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;
		if (!CraftorioMisc.chunkBased(mc.level)) return;

		Camera camera = event.getCamera();
		Vec3 camPos = camera.getPosition();

		List<ClaimBorderSegment> segments = collectNearbyClaimBorders(mc, camPos);
		if (segments.isEmpty()) return;

		double depthFar = mc.gameRenderer.getDepthFar();
		float scroll = (float) (Util.getMillis() % 3000L) / 3000.0F;
		float vBase = scroll - (float) Mth.frac(camPos.y * 0.5F);
		float vTopUV = vBase;
		float vBottomUV = vBase + (float) depthFar;

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderTexture(0, FORCEFIELD_TEXTURE);
		RenderSystem.depthMask(Minecraft.useShaderTransparency());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.polygonOffset(-3.0F, -3.0F);
		RenderSystem.enablePolygonOffset();
		RenderSystem.disableCull();

		float r = (float) (CLAIM_BORDER_COLOR >> 16 & 255) / 255.0F;
		float g = (float) (CLAIM_BORDER_COLOR >> 8 & 255) / 255.0F;
		float b = (float) (CLAIM_BORDER_COLOR & 255) / 255.0F;

		for (ClaimBorderSegment segment : segments) {
			float alpha = claimBorderAlpha(segment.distance());
			if (alpha <= 0.0F) continue;

			RenderSystem.setShaderColor(r, g, b, alpha);

			BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			addClaimWallQuad(bufferbuilder, segment, camPos, depthFar, scroll, vTopUV, vBottomUV);
			MeshData meshdata = bufferbuilder.build();
			if (meshdata != null) {
				BufferUploader.drawWithShader(meshdata);
			}
		}

		RenderSystem.enableCull();
		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
	}

	private static float claimBorderAlpha(double distance) {
		double t = Mth.clamp(1.0 - distance / CLAIM_BORDER_FADE_DISTANCE, 0.0, 1.0);
		return (float) (t * t);
	}

	private static List<ClaimBorderSegment> collectNearbyClaimBorders(Minecraft mc, Vec3 camPos) {
		List<ClaimBorderSegment> segments = new ArrayList<>();
		int scanRadius = Mth.ceil(CLAIM_BORDER_FADE_DISTANCE / 16.0) + 1;
		ChunkPos center = new ChunkPos(BlockPos.containing(camPos.x, camPos.y, camPos.z));

		for (int cx = center.x - scanRadius; cx <= center.x + scanRadius; cx++) {
			for (int cz = center.z - scanRadius; cz <= center.z + scanRadius; cz++) {
				ChunkAccess chunk = mc.level.getChunk(cx, cz);
				if (!CraftorioMisc.isOwnedBy(chunk, mc.player)) continue;

				addClaimSegmentIfBorder(mc, camPos, segments, cx + 1, cz, true, (cx + 1) * 16.0, cz * 16.0, (cz + 1) * 16.0);
				addClaimSegmentIfBorder(mc, camPos, segments, cx - 1, cz, true, cx * 16.0, cz * 16.0, (cz + 1) * 16.0);
				addClaimSegmentIfBorder(mc, camPos, segments, cx, cz + 1, false, (cz + 1) * 16.0, cx * 16.0, (cx + 1) * 16.0);
				addClaimSegmentIfBorder(mc, camPos, segments, cx, cz - 1, false, cz * 16.0, cx * 16.0, (cx + 1) * 16.0);
			}
		}
		return segments;
	}

	private static void addClaimSegmentIfBorder(Minecraft mc, Vec3 camPos, List<ClaimBorderSegment> segments,
												  int neighborCx, int neighborCz, boolean alongX,
												  double planeCoord, double tangentStart, double tangentEnd) {
		ChunkAccess neighbor = mc.level.getChunk(neighborCx, neighborCz);
		if (CraftorioMisc.isOwnedBy(neighbor, mc.player)) return;

		double distance = alongX
				? distanceToSegment2D(camPos.x, camPos.z, planeCoord, tangentStart, planeCoord, tangentEnd)
				: distanceToSegment2D(camPos.x, camPos.z, tangentStart, planeCoord, tangentEnd, planeCoord);
		if (distance > CLAIM_BORDER_FADE_DISTANCE) return;

		segments.add(new ClaimBorderSegment(alongX, planeCoord, tangentStart, tangentEnd, distance));
	}

	private static double distanceToSegment2D(double px, double pz, double x1, double z1, double x2, double z2) {
		double dx = x2 - x1;
		double dz = z2 - z1;
		double lengthSq = dx * dx + dz * dz;
		double t = lengthSq == 0.0 ? 0.0 : Mth.clamp(((px - x1) * dx + (pz - z1) * dz) / lengthSq, 0.0, 1.0);
		double closestX = x1 + t * dx;
		double closestZ = z1 + t * dz;
		return Math.hypot(px - closestX, pz - closestZ);
	}

	private static void addClaimWallQuad(BufferBuilder buffer, ClaimBorderSegment segment, Vec3 camPos, double depthFar,
										  float uScroll, float vTopUV, float vBottomUV) {
		float u0 = uScroll + (float) (segment.tangentStart() * 0.5);
		float u1 = uScroll + (float) (segment.tangentEnd() * 0.5);

		if (segment.alongX()) {
			float x = (float) (segment.planeCoord() - camPos.x);
			float z0 = (float) (segment.tangentStart() - camPos.z);
			float z1 = (float) (segment.tangentEnd() - camPos.z);
			buffer.addVertex(x, (float) -depthFar, z0).setUv(u0, vBottomUV);
			buffer.addVertex(x, (float) -depthFar, z1).setUv(u1, vBottomUV);
			buffer.addVertex(x, (float) depthFar, z1).setUv(u1, vTopUV);
			buffer.addVertex(x, (float) depthFar, z0).setUv(u0, vTopUV);
		} else {
			float z = (float) (segment.planeCoord() - camPos.z);
			float x0 = (float) (segment.tangentStart() - camPos.x);
			float x1 = (float) (segment.tangentEnd() - camPos.x);
			buffer.addVertex(x0, (float) -depthFar, z).setUv(u0, vBottomUV);
			buffer.addVertex(x1, (float) -depthFar, z).setUv(u1, vBottomUV);
			buffer.addVertex(x1, (float) depthFar, z).setUv(u1, vTopUV);
			buffer.addVertex(x0, (float) depthFar, z).setUv(u0, vTopUV);
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
		int y = 5;

		int centerX = screenWidth / 2;
		int x = centerX - width / 2;

		boolean rawFormat = Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt() == 0;
		float fitScale = 1.0f;
		if (rawFormat) {
			int maxRawWidth = (int) (screenWidth * 0.6);
			if (width > maxRawWidth) {
				fitScale = Math.max(0.5f, maxRawWidth / (float) width);
			}
		}

		float scale = PointsAnimation.getScale() * fitScale;
		float[] shake = PointsAnimation.getShakeOffset();


		float pivotX = centerX;
		float pivotY = y + 4 + 5;

		graphics.pose().pushPose();
		graphics.pose().translate(pivotX + shake[0], pivotY + shake[1], 0);
		graphics.pose().scale(scale, scale, scale);
		graphics.pose().translate(-pivotX, -pivotY, 0);

		if (pointsString.equals(INFINITY_TEXT)) {
			CraftorioMisc.CraftorioTextEffects.drawFancy(graphics, font, pointsString, x, y + 5, true, 0);
		} else if (pointsString.equals(NEG_INFINITY_TEXT)) {
			CraftorioMisc.CraftorioTextEffects.drawFancy(graphics, font, pointsString, x, y + 5, true, 1);
		} else {
			graphics.drawString(font, pointsString, x, y + 5, 16759552, true);
		}

		graphics.pose().popPose();

		PointsPopup.render(graphics, font, pivotX, pivotY);
		InfinityBurst.render(graphics, font, pivotX, pivotY);
	}


	public static void showPoints(RegisterGuiLayersEvent e) {
		e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, pointBar,
				(graphics, partialTicks) -> ClientEvents.displayPoints(graphics));
	}

	private static final int POINTS_BAR_HEIGHT = 24;

	public static Rect2i getPointsBarScreenRect() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return null;

		Font font = minecraft.font;
		String pointsString = CraftorioMisc.bigIntFormat(PointsAnimation.getDisplayValue(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());

		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int padding = 8;
		int rectWidth = font.width(pointsString) + padding * 2;
		int x = screenWidth / 2 - rectWidth / 2;

		return new Rect2i(x, 0, rectWidth, POINTS_BAR_HEIGHT);
	}


	private static final ResourceLocation activeEffectsLayer = Craftorio.prefix("active_effects");

	public static void displayActiveEffects(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.gui.getDebugOverlay().showDebugScreen())
			return;
		if (minecraft.player == null)
			return;

		List<CraftorioEffects> effects = CraftorioMisc.getCraftorioEffects(minecraft.player);
		if (effects.isEmpty())
			return;

		Font font = minecraft.font;
		int leftEdge = 4;
		int y = 5;

		for (CraftorioEffects effect : effects) {
			String line = effect.getActualName() + " (" + CraftorioMisc.ticksToTimeString(effect.getTime()) + ")";
			int color = activeEffectColor(effect);

			graphics.drawString(font, line, leftEdge, y, color, true);
			y += font.lineHeight + 2;
		}
	}

	private static int activeEffectColor(CraftorioEffects effect) {
		boolean negative = (effect instanceof TagMultiplierEffect tagEffect && tagEffect.getMultiplier() < 0)
				|| (effect instanceof GeneralMultiplierEffect generalEffect && generalEffect.getMultiplier() < 0);
		Integer color = (negative ? ChatFormatting.RED : ChatFormatting.BLUE).getColor();
		return color != null ? color : 0xFFFFFF;
	}

	public static void showActiveEffects(RegisterGuiLayersEvent e) {
		e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, activeEffectsLayer,
				(graphics, partialTicks) -> ClientEvents.displayActiveEffects(graphics));
	}


	private static final ResourceLocation effectTimerLayer = Craftorio.prefix("effect_timer");
	private static boolean effectTimerEnabled = false;
	private static int effectTimerTicksRemaining = 0;

	public static void setEffectTimerDisplay(boolean enabled, int ticksRemaining) {
		effectTimerEnabled = enabled;
		effectTimerTicksRemaining = ticksRemaining;
	}

	public static void displayEffectTimer(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();

		if (!effectTimerEnabled)
			return;
		if (minecraft.gui.getDebugOverlay().showDebugScreen())
			return;
		if (minecraft.player == null)
			return;

		Font font = minecraft.font;
		String next_effect_in = Component.translatable("misc.craftorio.next_effect_in").getString();
		String text = next_effect_in + CraftorioMisc.ticksToTimeString(effectTimerTicksRemaining);
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int rightEdge = screenWidth - 4;

		graphics.drawString(font, text, rightEdge - font.width(text), 5, 0xFFFFFF, true);
	}

	public static void showEffectTimer(RegisterGuiLayersEvent e) {
		e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, effectTimerLayer,
				(graphics, partialTicks) -> ClientEvents.displayEffectTimer(graphics));
	}
}

package org.crimsoncrips.craftorio.events;

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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.ClientUnlockedItemsState;
import org.crimsoncrips.craftorio.client.InfinityBurst;
import org.crimsoncrips.craftorio.client.ItemDiscoveredPopup;
import org.crimsoncrips.craftorio.client.PointsAnimation;
import org.crimsoncrips.craftorio.client.PointsPopup;
import org.crimsoncrips.craftorio.client.AreaScannerBlockEntityRenderer;
import org.crimsoncrips.craftorio.client.screen.AreaScannerScreen;
import org.crimsoncrips.craftorio.client.screen.AutoSinkerScreen;
import org.crimsoncrips.craftorio.client.screen.AutoValueCondenserScreen;
import org.crimsoncrips.craftorio.client.screen.ContractRevealScreen;
import org.crimsoncrips.craftorio.client.screen.CraftorioConfigScreen;
import org.crimsoncrips.craftorio.client.screen.CraftorioSinkStatsScreen;
import org.crimsoncrips.craftorio.client.screen.ShopScreen;
import org.crimsoncrips.craftorio.client.screen.SinkScreen;
import org.crimsoncrips.craftorio.client.screen.ValueBrowserScreen;
import org.crimsoncrips.craftorio.client.screen.ValueCondenserScreen;
import org.crimsoncrips.craftorio.networking.OpenContractOfferScreenPacket;
import org.crimsoncrips.craftorio.networking.OpenShopScreenPacket;
import org.crimsoncrips.craftorio.networking.OpenValueBrowserScreenPacket;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public void registerScreens(RegisterMenuScreensEvent event) {
		event.register(CraftorioMenuTypes.SINKER.get(), SinkScreen::new);
		event.register(CraftorioMenuTypes.AUTO_SINKER.get(), AutoSinkerScreen::new);
		event.register(CraftorioMenuTypes.VALUE_CONDENSER.get(), ValueCondenserScreen::new);
		event.register(CraftorioMenuTypes.AUTO_VALUE_CONDENSER.get(), AutoValueCondenserScreen::new);
		event.register(CraftorioMenuTypes.AREA_SCANNER.get(), AreaScannerScreen::new);
	}

	@SubscribeEvent
	public void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(org.crimsoncrips.craftorio.block.entity.CraftorioBlockEntityTypes.AREA_SCANNER.get(), AreaScannerBlockEntityRenderer::new);
	}

	public static void registerConfigScreen(ModContainer modContainer) {
		modContainer.registerExtensionPoint(IConfigScreenFactory.class,
				(IConfigScreenFactory) (container, modListScreen) -> new CraftorioConfigScreen(modListScreen));
	}

	public static void openShopScreen(OpenShopScreenPacket message) {
		Minecraft.getInstance().setScreen(new ShopScreen(message.allUnlocked(), new HashSet<>(message.unlockedItems())));
	}

	public static void openValueBrowserScreen(OpenValueBrowserScreenPacket message) {
		Minecraft.getInstance().setScreen(new ValueBrowserScreen(message.allUnlocked(), new HashSet<>(message.unlockedItems())));
	}

	private static final ResourceLocation UNDISCOVERED_LOCK_TEXTURE = Craftorio.getGuiTexture("locked.png");

	public static void renderUndiscoveredItemLocks(ContainerScreenEvent.Render.Foreground event) {
		if (!ClientUnlockedItemsState.isAvailable()) return;

		AbstractContainerScreen<?> screen = event.getContainerScreen();
		GuiGraphics graphics = event.getGuiGraphics();
		int left = screen.getGuiLeft();
		int top = screen.getGuiTop();
		int lockSize = 8;

		for (Slot slot : screen.getMenu().slots) {
			ItemStack stack = slot.getItem();
			if (stack.isEmpty()) continue;

			ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
			if (ClientUnlockedItemsState.isUnlocked(id)) continue;

			int x = left + slot.x;
			int y = top + slot.y;

			graphics.pose().pushPose();
			graphics.pose().translate(0.0F, 0.0F, 200.0F);
			graphics.blit(UNDISCOVERED_LOCK_TEXTURE, x, y, 0.0F, 0.0F, lockSize, lockSize, lockSize, lockSize);
			graphics.pose().popPose();
		}
	}

	public static void openContractOfferScreen(OpenContractOfferScreenPacket message) {
		Screen current = Minecraft.getInstance().screen;
		if (current instanceof ContractRevealScreen revealScreen) {
			revealScreen.updateOffer(message.contractIds(), message.ticksUntilRefresh());
		} else {
			Minecraft.getInstance().setScreen(new ContractRevealScreen(message.contractIds(), message.ticksUntilRefresh()));
		}
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
	private static final int OTHER_CLAIM_BORDER_COLOR = 0xFF3030;
	private static final int OTHER_CLAIM_BORDER_COLOR_SHARED = 0x808080;

	private record ClaimBorderSegment(boolean alongX, double planeCoord, double tangentStart, double tangentEnd, double distance, int color) {}
	private record ClaimWallKey(boolean alongX, long plane, long tangentStart) {}

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

		for (ClaimBorderSegment segment : segments) {
			float alpha = claimBorderAlpha(segment.distance());
			if (alpha <= 0.0F) continue;

			float r = (float) (segment.color() >> 16 & 255) / 255.0F;
			float g = (float) (segment.color() >> 8 & 255) / 255.0F;
			float b = (float) (segment.color() & 255) / 255.0F;
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
		Set<ClaimWallKey> seenWalls = new HashSet<>();
		int scanRadius = Mth.ceil(CLAIM_BORDER_FADE_DISTANCE / 16.0) + 1;
		ChunkPos center = new ChunkPos(BlockPos.containing(camPos.x, camPos.y, camPos.z));

		for (int cx = center.x - scanRadius; cx <= center.x + scanRadius; cx++) {
			for (int cz = center.z - scanRadius; cz <= center.z + scanRadius; cz++) {
				ChunkAccess chunk = mc.level.getChunk(cx, cz);
				if (!CraftorioMisc.isClaimed(chunk)) continue;

				addClaimSegmentIfBorder(mc, camPos, segments, seenWalls, chunk, cx + 1, cz, true, (cx + 1) * 16.0, cz * 16.0, (cz + 1) * 16.0);
				addClaimSegmentIfBorder(mc, camPos, segments, seenWalls, chunk, cx - 1, cz, true, cx * 16.0, cz * 16.0, (cz + 1) * 16.0);
				addClaimSegmentIfBorder(mc, camPos, segments, seenWalls, chunk, cx, cz + 1, false, (cz + 1) * 16.0, cx * 16.0, (cx + 1) * 16.0);
				addClaimSegmentIfBorder(mc, camPos, segments, seenWalls, chunk, cx, cz - 1, false, cz * 16.0, cx * 16.0, (cx + 1) * 16.0);
			}
		}
		return segments;
	}

	private static int claimRank(ChunkAccess chunk, Minecraft mc) {
		if (chunk == null) return 0;
		if (CraftorioMisc.isOwnedBy(chunk, mc.player)) return 2;
		if (CraftorioMisc.isOwnedByAnother(chunk, mc.player)) return 1;
		return 0;
	}

	private static void addClaimSegmentIfBorder(Minecraft mc, Vec3 camPos, List<ClaimBorderSegment> segments, Set<ClaimWallKey> seenWalls,
												  ChunkAccess sourceChunk, int neighborCx, int neighborCz, boolean alongX,
												  double planeCoord, double tangentStart, double tangentEnd) {
		ChunkAccess neighbor = mc.level.getChunk(neighborCx, neighborCz);

		int sourceRank = claimRank(sourceChunk, mc);
		int neighborRank = claimRank(neighbor, mc);
		if (sourceRank == neighborRank) return;

		ClaimWallKey key = new ClaimWallKey(alongX, (long) planeCoord, (long) tangentStart);
		if (!seenWalls.add(key)) return;

		double distance = alongX
				? distanceToSegment2D(camPos.x, camPos.z, planeCoord, tangentStart, planeCoord, tangentEnd)
				: distanceToSegment2D(camPos.x, camPos.z, tangentStart, planeCoord, tangentEnd, planeCoord);
		if (distance > CLAIM_BORDER_FADE_DISTANCE) return;

		int winningRank = Math.max(sourceRank, neighborRank);
		int color = winningRank == 2 ? CLAIM_BORDER_COLOR
				: (CraftorioMisc.isNoBorders(mc.level) ? OTHER_CLAIM_BORDER_COLOR_SHARED : OTHER_CLAIM_BORDER_COLOR);

		segments.add(new ClaimBorderSegment(alongX, planeCoord, tangentStart, tangentEnd, distance, color));
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

		float craftorioMultiplier = CraftorioMisc.getCraftorioMultiplier(minecraft.player);
		String multiplierString = "x" + String.format("%.2f", 1.0 + craftorioMultiplier);
		graphics.drawString(font, multiplierString, x + width + 6, y + 5, 0x55FF55, true);

		PointsPopup.render(graphics, font, pivotX, pivotY);
		InfinityBurst.render(graphics, font, pivotX, pivotY);
		ItemDiscoveredPopup.render(graphics, pivotX, pivotY);
	}

	public static void drawMaxPointsAtMeterPosition(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;

		Font font = minecraft.font;
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int centerX = screenWidth / 2;
		int y = 5;

		BigInteger maxPoints = CraftorioMisc.getHighestPoints(minecraft.player);
		String prefix = Component.translatable("misc.craftorio.highest_points_label").getString();
		CraftorioMisc.CraftorioTextEffects.drawCenteredLine(graphics, font, centerX, y + 5, true, 0xFFFF55, prefix, maxPoints);
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


	@SubscribeEvent
	public static void tickUniversalProgressDisplay(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) return;
		if (!CraftorioMisc.universalBased(minecraft.level)) return;

		for (CraftorioEffects effect : CraftorioMisc.getCraftorioEffects(minecraft.player)) {
			if (effect.getTime() > 0) {
				effect.setTime(effect.getTime() - 1);
			}
		}

		for (CraftorioShipmentContract contract : CraftorioMisc.getCraftorioContracts(minecraft.player)) {
			if (contract.getTime() > 0) {
				contract.setTime(contract.getTime() - 1);
			}
		}
	}

	public static int activeEffectColor(CraftorioEffects effect) {
		boolean negative = (effect instanceof TagMultiplierEffect tagEffect && tagEffect.getMultiplier() < 0)
				|| (effect instanceof GeneralMultiplierEffect generalEffect && generalEffect.getMultiplier() < 0);
		Integer color = (negative ? ChatFormatting.RED : ChatFormatting.BLUE).getColor();
		return color != null ? color : 0xFFFFFF;
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

	private static final ResourceLocation toastLayer = Craftorio.prefix("craftorio_toasts");

	public static void showToasts(RegisterGuiLayersEvent e) {
		e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, toastLayer,
				(graphics, partialTicks) -> org.crimsoncrips.craftorio.client.CraftorioToastManager.render(graphics));
	}

	public static final ResourceLocation STATUS_ICONS = Craftorio.getGuiTexture("status_icons.png");
	public static final int STATUS_ICON_SIZE = 13;
	public static final int STATUS_ICON_SHEET_WIDTH = 26;
	public static final int STATUS_ICON_SHEET_HEIGHT = 39;
	public static final int BORDER_MODE_INDICATOR_SIZE = STATUS_ICON_SIZE;
	public static final int BORDER_MODE_INDICATOR_GAP = 4;

	private static final int CHUNK_BASED_ROW = 0;
	private static final int UNIVERSAL_BASED_ROW = 1;
	private static final int NO_BORDERS_ROW = 2;

	public static void drawBorderModeIndicators(GuiGraphics graphics, Font font, int x, int y, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;

		net.minecraft.world.level.Level level = minecraft.level;

		drawIndicator(graphics, font, x, y, mouseX, mouseY, CHUNK_BASED_ROW, CraftorioMisc.chunkBased(level),
				Component.translatable("misc.craftorio.chunk_based_label", CraftorioMisc.chunkBased(level)));
		y += BORDER_MODE_INDICATOR_SIZE + BORDER_MODE_INDICATOR_GAP;

		drawIndicator(graphics, font, x, y, mouseX, mouseY, UNIVERSAL_BASED_ROW, CraftorioMisc.universalBased(level),
				Component.translatable("misc.craftorio.universal_based_label", CraftorioMisc.universalBased(level)));
		y += BORDER_MODE_INDICATOR_SIZE + BORDER_MODE_INDICATOR_GAP;

		drawIndicator(graphics, font, x, y, mouseX, mouseY, NO_BORDERS_ROW, CraftorioMisc.isNoBorders(level),
				Component.translatable("misc.craftorio.no_borders_based_label", CraftorioMisc.isNoBorders(level)));
	}

	private static void drawIndicator(GuiGraphics graphics, Font font, int x, int y, int mouseX, int mouseY, int row, boolean value, Component tooltip) {
		float u = value ? 0.0F : STATUS_ICON_SIZE;
		float v = row * STATUS_ICON_SIZE;
		graphics.blit(STATUS_ICONS, x, y, u, v, STATUS_ICON_SIZE, STATUS_ICON_SIZE, STATUS_ICON_SHEET_WIDTH, STATUS_ICON_SHEET_HEIGHT);

		if (mouseX >= x && mouseX < x + BORDER_MODE_INDICATOR_SIZE && mouseY >= y && mouseY < y + BORDER_MODE_INDICATOR_SIZE) {
			graphics.renderTooltip(font, tooltip, mouseX, mouseY);
		}
	}

	public static void renderPauseMenuIndicators(ScreenEvent.Render.Post event) {
		if (!(event.getScreen() instanceof PauseScreen)) return;

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;

		int x = minecraft.getWindow().getGuiScaledWidth() - BORDER_MODE_INDICATOR_SIZE - 8;
		drawBorderModeIndicators(event.getGuiGraphics(), minecraft.font, x, 8, event.getMouseX(), event.getMouseY());
	}

	public static void addCraftorioStatsButton(ScreenEvent.Init.Post event) {
		if (!(event.getScreen() instanceof StatsScreen statsScreen)) return;

		int width = 140;
		int height = 20;
		int x = statsScreen.width - width - 8;
		int y = 8;

		event.addListener(Button.builder(Component.translatable("misc.craftorio.sink_stats_button"),
						b -> Minecraft.getInstance().setScreen(new CraftorioSinkStatsScreen()))
				.bounds(x, y, width, height)
				.build());
	}
}

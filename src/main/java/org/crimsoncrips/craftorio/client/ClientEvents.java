package org.crimsoncrips.craftorio.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigInteger;

public class ClientEvents {

	@SubscribeEvent
	public void registerScreens(RegisterMenuScreensEvent event) {
		event.register(CraftorioMenuTypes.SINKER.get(), SinkScreen::new);
	}


	//From Improved Mobs
	private static final ResourceLocation pointBar = Craftorio.getGuiTexture("textures/gui/points_bar.png");

	public static void displayPoints(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.gui.getDebugOverlay().showDebugScreen())
			return;
		if (minecraft.level == null)
			return;


		Font font = minecraft.font;
		BigInteger actualPoints = CraftorioMisc.getPoints(minecraft.level,minecraft.player);
		BigInteger tempPoints = CraftorioMisc.getTempPoints(minecraft.level,minecraft.player);

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

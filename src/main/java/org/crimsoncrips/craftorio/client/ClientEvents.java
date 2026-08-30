package org.crimsoncrips.craftorio.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;

import java.awt.*;
import java.math.BigInteger;

public class ClientEvents {

	@SubscribeEvent
	public void registerScreens(RegisterMenuScreensEvent event) {
		event.register(CraftorioMenuTypes.SINKER.get(), SinkScreen::new);
	}


	//From Improved Mobs
	private static final ResourceLocation pointBar = Craftorio.getGuiTexture("textures/gui/points_bar.png");

	public static void displayPoints(GuiGraphics graphics) {
		if (Minecraft.getInstance().gui.getDebugOverlay().showDebugScreen())
			return;
		if (Minecraft.getInstance().level == null)
			return;

		graphics.pose().pushPose();
		Font font = Minecraft.getInstance().font;
		BigInteger points = CraftorioMisc.getPoints(Minecraft.getInstance().level);
		String pointsString = CraftorioMisc.bigIntFormat(points, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());

		String SPECIAL_TEXT = CraftorioMisc.bigIntFormat(CraftorioMisc.pointThreshold(),Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
		float scale = 1;
		int width = font.width(pointsString);
		int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 7 - width - 5;
		int y = 5;

		graphics.pose().scale(scale, scale, scale);
		if (pointsString.equals(SPECIAL_TEXT)) {
			CraftorioMisc.CraftorioTextEffects.drawFancy(graphics, font, pointsString, x + 4, y + 5,true);
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

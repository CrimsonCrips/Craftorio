package org.crimsoncrips.craftorio.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;

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
		graphics.pose().pushPose();
		Font font = Minecraft.getInstance().font;
		long points = Minecraft.getInstance().level.getData(CraftorioDataAttachments.POINTS);
		MutableComponent txt = Component.translatable(String.valueOf(points)).withStyle();
		float scale = 1;
		graphics.pose().scale(scale, scale, scale);
		int width = font.width(txt);
		int x;
		int y = 5;
		x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 7 - width - 5;
		graphics.drawString(font, Component.translatable(String.valueOf(points)), x + 4, y + 5, 16759552, true);
		graphics.pose().popPose();
	}

	public static void showPoints(RegisterGuiLayersEvent e) {
		e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, pointBar,
				(graphics, partialTicks) -> ClientEvents.displayPoints(graphics));
	}
}

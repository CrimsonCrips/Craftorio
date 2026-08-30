package org.crimsoncrips.craftorio.client;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;
import org.crimsoncrips.craftorio.networking.SinkItemsPacket;

import java.math.BigInteger;
import java.util.List;

public class SinkScreen extends AbstractContainerScreen<SinkerMenu>{
	private static final ResourceLocation SINK_SCREEN = Craftorio.getGuiTexture("sinker_screen.png");

	private final int containerRows;

	public SinkScreen(SinkerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		this.containerRows = menu.getRowCount();
		this.imageHeight = 124 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}


	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(SINK_SCREEN, i, j + this.containerRows * 9 + 13, 0, 86, this.imageWidth, 136);
		guiGraphics.blit(SINK_SCREEN, i, j - 36, 0, 0, this.imageWidth, this.containerRows * 28 + 17);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY - 10, 4210752, false);
	}

	private final List<SinkButton> sinkButtons = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
    class SinkButton extends AbstractButton  {
		public SinkButton(int x, int y) {
			super(x, y, 16, 16, CommonComponents.EMPTY);
			String string = Component.translatable("misc.craftorio.sinker_button").getString();
			Tooltip tooltip = Tooltip.create(Component.literal(string));
			this.setTooltip(tooltip);
		}

		public void onPress() {
			PacketDistributor.sendToServer(new SinkItemsPacket(true));
			minecraft.player.closeContainer();

		}

		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}
	}

	private void addButton(SinkButton beaconButton) {
		this.addRenderableWidget(beaconButton);
		this.sinkButtons.add(beaconButton);
	}

	protected void init() {
		super.init();
		this.sinkButtons.clear();
		int i = (this.width) / 2;
		int j = (this.height) / 2 - 40;
		this.addButton(new SinkButton(i - 8, j - 58));
	}


}

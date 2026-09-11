package org.crimsoncrips.craftorio.client.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.ValueCondenserMenu;
import org.crimsoncrips.craftorio.networking.CondenseValuePacket;

import java.math.BigInteger;
import java.util.List;

public class ValueCondenserScreen extends AbstractContainerScreen<ValueCondenserMenu> {
	private static final ResourceLocation CONDENSER_SCREEN = Craftorio.getGuiTexture("value_condenser_screen.png");

	public ValueCondenserScreen(ValueCondenserMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int j = (this.height - this.imageHeight) / 2;
		int maxPointsWidth = (int) (this.width * 0.7);
		String pointsSuffix = Component.translatable("misc.craftorio.points_suffix").getString();
		CraftorioMisc.CraftorioTextEffects.drawCenteredLineFit(guiGraphics, this.font, this.width / 2, j - 46, true, 0xFFAA00, maxPointsWidth, computeCondenserValue(), pointsSuffix);

		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	private BigInteger computeCondenserValue() {
		BigInteger total = BigInteger.ZERO;
		Container container = this.menu.getContainer();
		for (int slot = 0; slot < this.menu.getMainSlotCount(); slot++) {
			ItemStack stack = container.getItem(slot);
			if (!stack.isEmpty()) {
				total = total.add(CraftorioMisc.checkValue(stack, this.minecraft.player, false));
			}
		}
		return total;
	}

	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(CONDENSER_SCREEN, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY - 10, 4210752, false);
	}

	private final List<CondenseButton> condenseButtons = Lists.newArrayList();

	@OnlyIn(Dist.CLIENT)
	class CondenseButton extends AbstractButton {
		public CondenseButton(int x, int y) {
			super(x, y, 16, 16, CommonComponents.EMPTY);
			Tooltip tooltip = Tooltip.create(Component.translatable("misc.craftorio.condense_button"));
			this.setTooltip(tooltip);
		}

		public void onPress() {
			PacketDistributor.sendToServer(new CondenseValuePacket(true));
		}

		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}
	}

	private void addButton(CondenseButton button) {
		this.addRenderableWidget(button);
		this.condenseButtons.add(button);
	}

	protected void init() {
		super.init();
		this.condenseButtons.clear();
		int i = (this.width) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.addButton(new CondenseButton(i - 8, j - 18));
	}
}

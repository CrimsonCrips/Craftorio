package org.crimsoncrips.craftorio.client;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.BorderExpandPacket;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BorderExpandScreen extends Screen {
	private static final ResourceLocation EXPAND_SCREEN = Craftorio.getGuiTexture("expand_screen.png");

	long amountClaiming = 0;
	BorderExpandScreen borderExpandScreen;

	public BorderExpandScreen() {
		super(Component.literal("Test"));
		this.borderExpandScreen = this;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int i = (this.width) / 2;
		int j = (this.height) / 2 - 10;
		long landAmount = CraftorioMisc.getLandAmount(Minecraft.getInstance().level);
		String pointsToExpand = CraftorioMisc.bigIntFormat(CraftorioMisc.pointsToExpand(amountClaiming,landAmount),Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
		int centralizer = pointsToExpand.length() * 2;
		int centralizer1 = String.valueOf(amountClaiming).length() * 2;
		guiGraphics.drawString(this.font, Component.literal(String.valueOf(amountClaiming)), i + 7 + (-centralizer1), j + 5, 4210752, false);
		guiGraphics.drawString(this.font, Component.literal("Points Required"), i - 25, j - 40, 4210752, false);

		guiGraphics.drawString(this.font, Component.literal(pointsToExpand), i + 9 + (-centralizer), j - 30, 4210752, false);


	}



	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected void init() {
		super.init();
		this.borderButtons.clear();

		int i = (this.width) / 2;
		int j = (this.height) / 2 - 10;

		this.addButton(new BorderExpandAmount(i + 30, j, 22, 22,false,true));
		this.addButton(new BorderExpandAmount(i - 30, j, 22, 22,false,false));
		this.addButton(new BorderExpandAmount(i + 60, j, 22, 22,true, true));
		this.addButton(new BorderExpandAmount(i - 60, j, 22, 22,true,false));
		this.addButton(new ExpandBorder(i, j - 100, 22, 22));
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		int i = (this.width) / 2 ;
		int j = (this.height) / 2 - 40;
		guiGraphics.blit(EXPAND_SCREEN, i - 75, j - 80, 0, 0, 176, 140);
	}

	private final List<ExpansionButtons> borderButtons = Lists.newArrayList();

	@OnlyIn(Dist.CLIENT)
	class ExpansionButtons extends AbstractButton  {
		public ExpansionButtons(int x, int y, int width, int height) {
			super(x, y, width, height, CommonComponents.EMPTY);
		}

		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);

		}

		@Override
		public void onPress() {
		}
	}

	class ExpandBorder extends ExpansionButtons {
		public ExpandBorder(int x, int y, int width, int height){
			super(x,y,width,height);

			Tooltip tooltip = Tooltip.create(Component.literal("Expand Border"));
			this.setTooltip(tooltip);
		}

		@Override
		public void onPress() {
			PacketDistributor.sendToServer(new BorderExpandPacket(amountClaiming,true));
			if (minecraft == null)return;
			minecraft.setScreen(null);
		}
	}

	class BorderExpandAmount extends ExpansionButtons {
		boolean maxer;
		boolean positive;

		public BorderExpandAmount(int x, int y, int width, int height, boolean maxer, boolean positive){
			super(x,y,width,height);
			this.maxer = maxer;
			this.positive = positive;
			String text;
			if (maxer){
				text = positive ? "Max+" : "Max-";
			} else {
				text = positive ? "+1" : "-1";
			}
			Tooltip tooltip = Tooltip.create(Component.literal(text));
			this.setTooltip(tooltip);
		}

		@Override
		public @Nullable Tooltip getTooltip() {
			return super.getTooltip();
		}

		@Override
		public void onPress() {
			Level level = minecraft.level;
			BigInteger points = CraftorioMisc.getPoints(level);
			long land = CraftorioMisc.getLandAmount(level);
			if (maxer){
				if (positive){
					amountClaiming = CraftorioMisc.expandCapabilityWithPoints(points,land);
				} else amountClaiming = 0;
			} else {
				amountClaiming = positive ? (amountClaiming + 1) : (amountClaiming > 0 ? amountClaiming - 1 : 0);
			}
		}
	}



	private void addButton(ExpansionButtons beaconButton) {
		this.addRenderableWidget(beaconButton);
		this.borderButtons.add(beaconButton);
	}
}

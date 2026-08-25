package org.crimsoncrips.craftorio.client;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.AMOUNT_OF_LAND;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.POINTS;

@OnlyIn(Dist.CLIENT)
public class ChunkExpansionScreen extends Screen {
	private static final ResourceLocation SINK_SCREEN = Craftorio.getGuiTexture("sinker_screen.png");

	int amountClaiming = 0;
	ChunkExpansionScreen chunkExpansionScreen;

	public ChunkExpansionScreen() {
		super(Component.literal("Test"));
		this.chunkExpansionScreen = this;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int i = (this.width) / 2;
		int j = (this.height) / 2 - 40;


		guiGraphics.drawString(this.font, Component.literal(String.valueOf(amountClaiming)), i, j, 4210752, false);
		guiGraphics.drawString(this.font, Component.literal("Points Required"), i - 25, j - 40, 4210752, false);
		guiGraphics.drawString(this.font, Component.literal(String.valueOf(CraftorioMisc.pointsToClaimLand(amountClaiming,0))), i, j - 30, 4210752, false);


	}



	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected void init() {
		super.init();
		this.chunkButtons.clear();

		int i = (this.width) / 2;
		int j = (this.height) / 2 - 40;

		this.addButton(new ChunkAmountDeterminer(i + 30, j, 22, 22,false,true));
		this.addButton(new ChunkAmountDeterminer(i - 30, j, 22, 22,false,false));
		this.addButton(new ChunkAmountDeterminer(i + 60, j, 22, 22,true, true));
		this.addButton(new ChunkAmountDeterminer(i - 60, j, 22, 22,true,false));
		this.addButton(new ClaimChunk(i, j - 100, 22, 22));
	}


	private final List<ChunkExpansionButton> chunkButtons = Lists.newArrayList();

	@OnlyIn(Dist.CLIENT)
	class ChunkExpansionButton extends AbstractButton  {
		public ChunkExpansionButton(int x, int y,int width, int height) {
			super(x, y, width, height, CommonComponents.EMPTY);
		}

		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);

		}

		@Override
		public void onPress() {
		}
	}

	class ClaimChunk extends ChunkExpansionButton{
		public ClaimChunk(int x,int y,int width, int height){
			super(x,y,width,height);
		}

		@Override
		public void onPress() {
			if (minecraft == null)return;
			minecraft.setScreen(null);
		}
	}

	class ChunkAmountDeterminer extends ChunkExpansionButton{
		boolean maxer;
		boolean positive;

		public ChunkAmountDeterminer(int x,int y,int width, int height,boolean maxer,boolean positive){
			super(x,y,width,height);
			this.maxer = maxer;
			this.positive = positive;
		}

		@Override
		public @Nullable Tooltip getTooltip() {
			return super.getTooltip();
		}

		@Override
		public void onPress() {
			Level level = minecraft.level;
			long points = CraftorioMisc.getPoints(level);
			int land = CraftorioMisc.getLandAmount(level);
			if (maxer){
				if (positive){
					amountClaiming = CraftorioMisc.landClaimableWithPoints(points,land);
				} else amountClaiming = 0;
			} else {
				amountClaiming = positive ? (amountClaiming + 1) : (amountClaiming - 1);
			}
		}
	}



	private void addButton(ChunkExpansionButton beaconButton) {
		this.addRenderableWidget(beaconButton);
		this.chunkButtons.add(beaconButton);
	}
}

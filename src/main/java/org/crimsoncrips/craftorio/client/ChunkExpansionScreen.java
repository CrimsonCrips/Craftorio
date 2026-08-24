package org.crimsoncrips.craftorio.client;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;

import java.util.List;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.AMOUNT_OF_LAND;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.POINTS;

@OnlyIn(Dist.CLIENT)
public class ChunkExpansionScreen extends Screen {
	private static final ResourceLocation SINK_SCREEN = Craftorio.getGuiTexture("sinker_screen.png");

	int amountToClaim = 0;
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

		this.addButton(new ChunkExpansionButton(i + 30, j, 22, 22));
		this.addButton(new ChunkExpansionButton(i - 30, j, 22, 22));
		this.addButton(new ChunkExpansionButton(i + 60, j, 22, 22));
		this.addButton(new ChunkExpansionButton(i - 60, j, 22, 22));
		this.addButton(new ClaimChunk(i - 60, j, 22, 22));

		guiGraphics.drawString(this.font, Component.literal(String.valueOf(amountToClaim)), i, j, 4210752, false);


	}

	protected void init() {
		super.init();
		this.chunkButtons.clear();
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
			long points = Minecraft.getInstance().level.getData(POINTS);
			int currentLand = Minecraft.getInstance().level.getData(AMOUNT_OF_LAND);

			amountToClaim = Math.toIntExact(points / (currentLand * 10L));
			minecraft.setScreen(chunkExpansionScreen);
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
		public void onPress() {
			if (maxer){

			} else {

			}
		}
	}



	private void addButton(ChunkExpansionButton beaconButton) {
		this.addRenderableWidget(beaconButton);
		this.chunkButtons.add(beaconButton);
	}
}

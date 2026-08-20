package org.crimsoncrips.craftorio.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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
		guiGraphics.blit(SINK_SCREEN, i, j - 36, 0, 0, this.imageWidth, this.containerRows * 28 + 17);
		guiGraphics.blit(SINK_SCREEN, i, j + this.containerRows * 9 + 13, 0, 86, this.imageWidth, 136);
	}



}

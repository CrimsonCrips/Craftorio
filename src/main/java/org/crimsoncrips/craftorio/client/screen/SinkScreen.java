package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;
import org.crimsoncrips.craftorio.networking.CashOutDoubleOrNothingPacket;
import org.crimsoncrips.craftorio.networking.DoubleOrNothingPacket;
import org.crimsoncrips.craftorio.networking.SinkItemsPacket;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SinkScreen extends AbstractContainerScreen<SinkerMenu>{

	private static final ResourceLocation GOLD_INGOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/gold_ingot.png");

	private static final long FLIP_MIN_DURATION_MS = 1200L;
	private static final long FLIP_TIMEOUT_MS = 5000L;
	private static final long FLIP_TEXT_INTERVAL_MS = 150L;
	private static final int BASE_INGOT_COUNT = 25;
	private static final int MAX_INGOT_SPAWN_PER_WIN = 400;
	private static final int MAX_TOTAL_INGOTS = 800;
	private static final int INGOT_SIZE = 16;
	private static final float MAX_VANISH_HEIGHT_FRACTION = 0.75f;
	private static final float MIN_VANISH_HEIGHT_FRACTION = 0.4f;
	private static final float SHRINK_DISTANCE = 60f;
	private static final int BASE_IMAGE_WIDTH = 176;
	private static final int SIDE_PANEL_WIDTH = 130;
	private static final float MAX_ROTATION_SPEED = 240f;
	private static final float SHOWERING_ITEM_ALPHA = 0.65f;

	private final int containerRows;
	private final Random rng = new Random();

	private enum GambleState { IDLE, FLIPPING, WON, LOST }
	private GambleState state = GambleState.IDLE;

	private long flipStartMillis = 0L;
	private Boolean pendingHeads = null;
	private BigInteger pendingEscrow = BigInteger.ZERO;
	private int pendingWinStreak = 0;
	private BigInteger pendingRefund = BigInteger.ZERO;

	private BigInteger displayedEscrow = BigInteger.ZERO;
	private int winStreak = 0;

	private final List<ShoweringItem> fallingIngots = new ArrayList<>();
	private List<ResourceLocation> showeringItemTextures = List.of(GOLD_INGOT_TEXTURE);

	private Button doubleOrNothingButton;
	private Button sinkButton;
	private Button doneButton;
	private Button forceHeadsButton;

	private int sideCenterX;
	private int groupY;

	public SinkScreen(SinkerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		this.containerRows = menu.getRowCount();
		this.imageWidth = BASE_IMAGE_WIDTH + SIDE_PANEL_WIDTH;
		this.imageHeight = 124 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}


	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		tryResolveFlip();

		renderGoldRain(guiGraphics);

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int j = (this.height - this.imageHeight) / 2;
		int centerX = this.width / 2;
		int maxPointsWidth = (int) (this.width * 0.7);
		String pointsSuffix = Component.translatable("misc.craftorio.points_suffix").getString();

		if (this.state == GambleState.FLIPPING) {
			long elapsed = System.currentTimeMillis() - this.flipStartMillis;
			boolean showHeads = (elapsed / FLIP_TEXT_INTERVAL_MS) % 2 == 0;
			Component flipText = Component.translatable(showHeads ? "misc.craftorio.coin_flip_heads" : "misc.craftorio.coin_flip_tails");
			guiGraphics.drawCenteredString(this.font, flipText, this.sideCenterX, this.groupY - this.font.lineHeight - 6, 0xFFFFFF);
		} else {
			BigInteger valueShown = this.state == GambleState.WON || this.state == GambleState.LOST
					? this.displayedEscrow
					: computeSinkerValue();
			CraftorioMisc.CraftorioTextEffects.drawCenteredLineFit(guiGraphics, this.font, centerX, j - 46, true, 0xFFAA00, maxPointsWidth, valueShown, pointsSuffix);
		}

		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (this.doubleOrNothingButton != null) {
			boolean canBet = this.state != GambleState.IDLE || computeSinkerValue().signum() != 0;
			this.doubleOrNothingButton.active = canBet;
			this.forceHeadsButton.active = canBet;
		}
	}

	private BigInteger computeSinkerValue() {
		BigInteger total = BigInteger.ZERO;
		Container container = this.menu.getContainer();
		for (int slot = 0; slot < this.menu.getRowCount() * 9; slot++) {
			ItemStack stack = container.getItem(slot);
			if (!stack.isEmpty()) {
				total = total.add(CraftorioMisc.checkValue(stack, this.minecraft.player, true));
			}
		}
		return total;
	}

	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;

		guiGraphics.fill(i, j, i + this.imageWidth, j + this.imageHeight, 0xE0202020);
		guiGraphics.renderOutline(i, j, this.imageWidth, this.imageHeight, 0xFF808080);

		for (Slot slot : this.menu.slots) {
			int slotX = i + slot.x - 1;
			int slotY = j + slot.y - 1;
			guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF404040);
			guiGraphics.renderOutline(slotX, slotY, 18, 18, 0xFF808080);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY - 10, 4210752, false);
	}

	protected void init() {
		super.init();

		this.showeringItemTextures = resolveShoweringItemTextures();

		int panelLeft = (this.width - this.imageWidth) / 2;
		int panelTop = (this.height - this.imageHeight) / 2;
		this.sideCenterX = panelLeft + BASE_IMAGE_WIDTH + SIDE_PANEL_WIDTH / 2;
		this.groupY = panelTop + (int) (this.imageHeight * 0.62f);

		this.doubleOrNothingButton = Button.builder(Component.translatable("misc.craftorio.double_or_nothing_button"), b -> startFlip(false))
				.bounds(sideCenterX - 55, groupY, 110, 20).build();
		this.addRenderableWidget(this.doubleOrNothingButton);

		this.sinkButton = Button.builder(Component.translatable("misc.craftorio.sink_button_label"), b -> onSinkPressed())
				.bounds(sideCenterX - 40, groupY + 26, 80, 16).build();
		this.addRenderableWidget(this.sinkButton);

		this.doneButton = Button.builder(Component.translatable("misc.craftorio.done"), b -> onDonePressed())
				.bounds(sideCenterX - 45, groupY, 90, 20).build();
		this.addRenderableWidget(this.doneButton);

		this.forceHeadsButton = Button.builder(Component.translatable("misc.craftorio.force_heads_button"), b -> startFlip(true))
				.bounds(sideCenterX - 55, groupY + 48, 110, 16).build();
		this.addRenderableWidget(this.forceHeadsButton);

		updateButtonVisibility();
	}

	private void startFlip(boolean forceHeads) {
		PacketDistributor.sendToServer(new DoubleOrNothingPacket(forceHeads));
		this.state = GambleState.FLIPPING;
		this.flipStartMillis = System.currentTimeMillis();
		this.pendingHeads = null;
		updateButtonVisibility();
	}

	public void onDoubleOrNothingResult(boolean heads, BigInteger escrowPoints, int winStreak, BigInteger refundPoints) {
		if (this.state != GambleState.FLIPPING) return;

		this.pendingHeads = heads;
		this.pendingEscrow = escrowPoints;
		this.pendingWinStreak = winStreak;
		this.pendingRefund = refundPoints;
	}

	private void tryResolveFlip() {
		if (this.state != GambleState.FLIPPING) return;
		long elapsed = System.currentTimeMillis() - this.flipStartMillis;
		if (this.pendingHeads != null) {
			if (elapsed >= FLIP_MIN_DURATION_MS) {
				resolveFlip();
			}
		} else if (elapsed >= FLIP_TIMEOUT_MS) {
			this.state = GambleState.IDLE;
			updateButtonVisibility();
		}
	}

	private void resolveFlip() {
		boolean heads = this.pendingHeads;
		this.winStreak = this.pendingWinStreak;
		this.pendingHeads = null;

		if (heads) {
			this.displayedEscrow = this.pendingEscrow;
			this.state = GambleState.WON;
			int intensityPercent = Craftorio.CLIENT_CONFIG.GOLD_RAIN_INTENSITY.get();
			int baseSpawnCount = (int) Math.min((long) BASE_INGOT_COUNT << Math.min(this.winStreak - 1, 8), MAX_INGOT_SPAWN_PER_WIN);
			int spawnCount = (int) (baseSpawnCount * (intensityPercent / 100.0));
			spawnGoldRain(spawnCount);
		} else {
			this.displayedEscrow = this.pendingRefund;
			this.state = GambleState.LOST;
			this.fallingIngots.clear();
		}

		updateButtonVisibility();
	}

	private void onSinkPressed() {
		if (this.state == GambleState.WON) {
			PacketDistributor.sendToServer(new CashOutDoubleOrNothingPacket());
			this.state = GambleState.IDLE;
			this.displayedEscrow = BigInteger.ZERO;
			this.winStreak = 0;
			this.fallingIngots.clear();
			updateButtonVisibility();
		} else {
			PacketDistributor.sendToServer(new SinkItemsPacket(true));
			minecraft.player.closeContainer();
		}
	}

	private void onDonePressed() {
		minecraft.player.closeContainer();
	}

	private void updateButtonVisibility() {
		boolean idle = this.state == GambleState.IDLE;
		boolean won = this.state == GambleState.WON;
		boolean lost = this.state == GambleState.LOST;
		boolean creative = this.minecraft.player != null && this.minecraft.player.isCreative();
		boolean gambleUnlocked = this.minecraft.player != null
				&& CraftorioMisc.hasUnlockedUpgrade(this.minecraft.player, Craftorio.prefix("double_or_nothing_unlock"));

		this.doubleOrNothingButton.visible = (idle || won) && gambleUnlocked;
		this.sinkButton.visible = idle || won;
		this.doneButton.visible = lost;
		this.forceHeadsButton.visible = creative && (idle || won);
	}

	private List<ResourceLocation> resolveShoweringItemTextures() {
		List<ResourceLocation> textures = new ArrayList<>();
		BuiltInRegistries.ITEM.getTag(CraftorioItemTagGen.SHOWERING_ITEM).ifPresent(holders -> {
			for (Holder<Item> holder : holders) {
				ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(holder.value());
				textures.add(ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "textures/item/" + itemId.getPath() + ".png"));
			}
		});
		return textures.isEmpty() ? List.of(GOLD_INGOT_TEXTURE) : textures;
	}

	private void spawnGoldRain(int count) {
		count = Math.min(count, Math.max(0, MAX_TOTAL_INGOTS - this.fallingIngots.size()));
		long now = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			float x = rng.nextFloat() * this.width;
			float startY = -20f - rng.nextFloat() * 600f;
			float speed = 80f + rng.nextFloat() * 60f;
			float initialRotation = rng.nextFloat() * 360f;
			float rotationSpeed = (rng.nextFloat() * 2f - 1f) * MAX_ROTATION_SPEED;
			float vanishFraction = MIN_VANISH_HEIGHT_FRACTION + rng.nextFloat() * (MAX_VANISH_HEIGHT_FRACTION - MIN_VANISH_HEIGHT_FRACTION);
			ResourceLocation texture = this.showeringItemTextures.get(rng.nextInt(this.showeringItemTextures.size()));
			this.fallingIngots.add(new ShoweringItem(x, startY, now, speed, initialRotation, rotationSpeed, vanishFraction, texture));
		}
	}

	private void renderGoldRain(GuiGraphics graphics) {
		if (this.fallingIngots.isEmpty()) return;

		long now = System.currentTimeMillis();

		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, SHOWERING_ITEM_ALPHA);

		Iterator<ShoweringItem> it = this.fallingIngots.iterator();
		while (it.hasNext()) {
			ShoweringItem ingot = it.next();
			float elapsedSec = (now - ingot.startTime()) / 1000f;
			float y = ingot.startY() + ingot.speed() * elapsedSec;
			float vanishY = this.height * ingot.vanishFraction();

			float scale = 1f;
			if (y >= vanishY) {
				float shrinkT = (y - vanishY) / SHRINK_DISTANCE;
				scale = Math.max(0f, 1f - shrinkT);
			}

			if (scale <= 0f || y > this.height + INGOT_SIZE) {
				it.remove();
				continue;
			}

			float rotation = ingot.initialRotation() + ingot.rotationSpeed() * elapsedSec;

			graphics.pose().pushPose();
			graphics.pose().translate(ingot.x(), y, 0);
			graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
			graphics.pose().scale(scale, scale, 1f);
			graphics.blit(ingot.texture(), -INGOT_SIZE / 2, -INGOT_SIZE / 2, 0, 0, INGOT_SIZE, INGOT_SIZE, INGOT_SIZE, INGOT_SIZE);
			graphics.pose().popPose();
		}

		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();
	}

	private record ShoweringItem(float x, float startY, long startTime, float speed, float initialRotation, float rotationSpeed, float vanishFraction, ResourceLocation texture) {}

}

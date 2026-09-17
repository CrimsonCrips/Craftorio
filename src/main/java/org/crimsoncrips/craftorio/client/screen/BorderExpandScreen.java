package org.crimsoncrips.craftorio.client.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.BorderExpandPacket;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BorderExpandScreen extends Screen {

	private static final int PANEL_WIDTH = 176;
	private static final int PANEL_PADDING = 12;
	private static final int EXPAND_BUTTON_WIDTH = 100;
	private static final int EXPAND_BUTTON_HEIGHT = 20;
	private static final int AMOUNT_BUTTON_SIZE = 22;
	private static final int AMOUNT_BUTTON_GAP = 4;
	private static final int AMOUNT_EDIT_BOX_WIDTH = 80;
	private static final int CANCEL_BUTTON_WIDTH = 100;
	private static final int CANCEL_BUTTON_HEIGHT = 20;
	private static final int GAP_AFTER_EXPAND = 14;
	private static final int GAP_AFTER_AMOUNT_ROW = 8;
	private static final int GAP_AFTER_GROWTH = 14;
	private static final int GAP_AFTER_LABEL = 4;
	private static final int GAP_TO_CANCEL = 10;

	long amountClaiming = 0;
	BorderExpandScreen borderExpandScreen;
	private EditBox amountBox;

	private int panelTop;
	private int panelHeight;
	private int expandButtonY;
	private int amountRowY;
	private int growthTextY;
	private int pointsLabelY;
	private int pointsValueY;
	private int cancelY;

	public BorderExpandScreen() {
		super(Component.translatable("misc.craftorio.expand_border"));
		this.borderExpandScreen = this;
	}

	private void computeLayout() {
		int lineHeight = this.font.lineHeight;
		this.panelHeight = PANEL_PADDING + EXPAND_BUTTON_HEIGHT + GAP_AFTER_EXPAND + AMOUNT_BUTTON_SIZE + GAP_AFTER_AMOUNT_ROW
				+ lineHeight + GAP_AFTER_GROWTH + lineHeight + GAP_AFTER_LABEL + lineHeight + PANEL_PADDING;

		int totalHeight = this.panelHeight + GAP_TO_CANCEL + CANCEL_BUTTON_HEIGHT;
		this.panelTop = (this.height - totalHeight) / 2;

		this.expandButtonY = this.panelTop + PANEL_PADDING;
		this.amountRowY = this.expandButtonY + EXPAND_BUTTON_HEIGHT + GAP_AFTER_EXPAND;
		this.growthTextY = this.amountRowY + AMOUNT_BUTTON_SIZE + GAP_AFTER_AMOUNT_ROW;
		this.pointsLabelY = this.growthTextY + lineHeight + GAP_AFTER_GROWTH;
		this.pointsValueY = this.pointsLabelY + lineHeight + GAP_AFTER_LABEL;
		this.cancelY = this.panelTop + this.panelHeight + GAP_TO_CANCEL;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;

		long borderGrowth = amountClaiming * Craftorio.SERVER_CONFIG.EXPANSION_AMOUNT.getAsInt();
		guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.expand_border_growth", borderGrowth), centerX, this.growthTextY, 4210752);

		guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.points_required"), centerX, this.pointsLabelY, 4210752);

		long landAmount = CraftorioMisc.getLandAmount(player);
		BigInteger rawCost = CraftorioMisc.pointsToExpand(amountClaiming, landAmount);
		BigInteger pointsToExpand = CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.EXPANSION_COST, rawCost).max(BigInteger.ZERO);
		CraftorioMisc.CraftorioTextEffects.drawCenteredLineFit(guiGraphics, this.font, centerX, this.pointsValueY, true, 16759552, Integer.MAX_VALUE, pointsToExpand);
	}



	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected void init() {
		super.init();
		this.borderButtons.clear();
		computeLayout();

		int centerX = this.width / 2;

		int editBoxX = centerX - AMOUNT_EDIT_BOX_WIDTH / 2;
		int maxMinusX = editBoxX - AMOUNT_BUTTON_GAP - AMOUNT_BUTTON_SIZE;
		int maxPlusX = editBoxX + AMOUNT_EDIT_BOX_WIDTH + AMOUNT_BUTTON_GAP;

		this.addButton(new BorderExpandAmount(maxMinusX, this.amountRowY, AMOUNT_BUTTON_SIZE, AMOUNT_BUTTON_SIZE, false));
		this.addButton(new BorderExpandAmount(maxPlusX, this.amountRowY, AMOUNT_BUTTON_SIZE, AMOUNT_BUTTON_SIZE, true));

		this.amountBox = new EditBox(this.font, editBoxX, this.amountRowY, AMOUNT_EDIT_BOX_WIDTH, AMOUNT_BUTTON_SIZE, Component.translatable("misc.craftorio.expand_border_amount"));
		this.amountBox.setFilter(s -> s.isEmpty() || (s.length() <= 15 && s.chars().allMatch(Character::isDigit)));
		this.amountBox.setValue(String.valueOf(amountClaiming));
		this.amountBox.setResponder(this::onAmountTyped);
		this.addRenderableWidget(this.amountBox);

		this.addButton(new ExpandBorder(centerX - EXPAND_BUTTON_WIDTH / 2, this.expandButtonY, EXPAND_BUTTON_WIDTH, EXPAND_BUTTON_HEIGHT));

		this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.cancel"), b -> this.onClose())
				.bounds(centerX - CANCEL_BUTTON_WIDTH / 2, this.cancelY, CANCEL_BUTTON_WIDTH, CANCEL_BUTTON_HEIGHT).build());
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		int panelX = this.width / 2 - PANEL_WIDTH / 2;
		guiGraphics.fill(panelX, this.panelTop, panelX + PANEL_WIDTH, this.panelTop + this.panelHeight, 0xE0202020);
		guiGraphics.renderOutline(panelX, this.panelTop, PANEL_WIDTH, this.panelHeight, 0xFF808080);
	}

	private final List<ExpansionButtons> borderButtons = Lists.newArrayList();

	@OnlyIn(Dist.CLIENT)
	class ExpansionButtons extends AbstractButton  {
		public ExpansionButtons(int x, int y, int width, int height, Component message) {
			super(x, y, width, height, message);
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
			super(x,y,width,height, Component.translatable("misc.craftorio.expand_border"));
			Tooltip tooltip = Tooltip.create(Component.translatable("misc.craftorio.expand_border"));
			this.setTooltip(tooltip);
		}

		@Override
		public void onPress() {
			PacketDistributor.sendToServer(new BorderExpandPacket(amountClaiming,true));
			if (minecraft == null)return;
			minecraft.setScreen(null);
		}
	}

	private void onAmountTyped(String value) {
		if (getMinecraft().player == null) return;

		long parsed;
		try {
			parsed = value.isEmpty() ? 0 : Long.parseLong(value);
		} catch (NumberFormatException e) {
			parsed = amountClaiming;
		}

		BigInteger points = CraftorioMisc.getPoints(getMinecraft().player);
		long land = CraftorioMisc.getLandAmount(getMinecraft().player);
		long cap = CraftorioMisc.expandCapabilityWithPoints(points, land);
		long clamped = Math.max(0, Math.min(parsed, cap));

		amountClaiming = clamped;
		if (clamped != parsed && this.amountBox != null) {
			this.amountBox.setValue(String.valueOf(clamped));
		}
	}

	class BorderExpandAmount extends ExpansionButtons {
		boolean positive;

		public BorderExpandAmount(int x, int y, int width, int height, boolean positive){
			super(x,y,width,height, positive ? Component.translatable("misc.craftorio.max_plus") : Component.translatable("misc.craftorio.max_minus"));
			this.positive = positive;
			Tooltip tooltip = Tooltip.create(this.getMessage());
			this.setTooltip(tooltip);
		}

		@Override
		public @Nullable Tooltip getTooltip() {
			return super.getTooltip();
		}

		@Override
		public void onPress() {

			if (getMinecraft().player == null)
				return;


			BigInteger points = CraftorioMisc.getPoints(getMinecraft().player);
			long land = CraftorioMisc.getLandAmount(getMinecraft().player);
			long cap = CraftorioMisc.expandCapabilityWithPoints(points,land);

			amountClaiming = positive ? cap : 0;
			if (amountBox != null) {
				amountBox.setValue(String.valueOf(amountClaiming));
			}
		}
	}



	private void addButton(ExpansionButtons beaconButton) {
		this.addRenderableWidget(beaconButton);
		this.borderButtons.add(beaconButton);
	}
}

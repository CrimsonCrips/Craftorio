//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.crimsoncrips.craftorio.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SinkerMenu extends AbstractContainerMenu {
	private static final int SLOTS_PER_ROW = 9;
	private final Container container;
	private final int containerRows;
	private final Player player;

	private boolean gambling = false;
	private BigInteger escrowPoints = BigInteger.ZERO;
	private int winStreak = 0;
	private BigInteger lastLossRefund = BigInteger.ZERO;
	private BigInteger pendingLossRefund = BigInteger.ZERO;


	private SinkerMenu(MenuType<?> type, int containerId, Inventory playerInventory, int rows) {
		this(type, containerId, playerInventory, new SimpleContainer(9 * rows), rows);
	}

	public static SinkerMenu sinkerMenu(int containerId, Inventory playerInventory) {
		return new SinkerMenu(CraftorioMenuTypes.SINKER.get(), containerId, playerInventory, 4);
	}

	public static SinkerMenu sinkMenu(int containerId, Inventory playerInventory,Container container) {
		return new SinkerMenu(CraftorioMenuTypes.SINKER.get(), containerId, playerInventory, container,4);
	}

	public Player getPlayer() {
		return player;
	}

	public SinkerMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, int rows) {
		super(type, containerId);
		checkContainerSize(container, rows * 9);
		this.container = container;
		this.player = playerInventory.player;
		this.containerRows = rows;
		container.startOpen(playerInventory.player);
		int i = (this.containerRows - 4) * 18;

		for(int j = 0; j < this.containerRows; ++j) {
			for(int k = 0; k < 9; ++k) {
				this.addSlot(new Slot(container, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		for(int l = 0; l < 3; ++l) {
			for(int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
			}
		}

		for(int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
		}

	}



	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot)this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < this.containerRows * 9) {
				if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	public void removed(Player player) {
		super.removed(player);
		if (!player.level().isClientSide()) {
			if (this.gambling) {
				cashOutDoubleOrNothing();
			}
			if (this.pendingLossRefund.signum() > 0) {
				CraftorioMisc.setPoints(CraftorioMisc.getPoints(player).add(this.pendingLossRefund), player);
				this.pendingLossRefund = BigInteger.ZERO;
			}
		}
		this.container.stopOpen(player);
	}

	public void sinkPoints(){
		BigInteger pointsToGive = BigInteger.ZERO;
		Level level = player.level();

        List<ItemStack> sinkedItems = new ArrayList<>();
		for(int j = 0; j < this.containerRows; ++j) {
			for(int k = 0; k < 9; ++k) {
				this.addSlot(new Slot(container, k + j * 9, 8 + k * 18, 18 + j * 18));
				Slot slot = this.getSlot(k + j * 9);
				ItemStack item = slot.getItem();

				if (!item.isEmpty() && !level.isClientSide()){
					sinkedItems.add(item);

					pointsToGive = pointsToGive.add(CraftorioMisc.checkValue(item,player,true));
					CraftorioMisc.recordItemSinked(player, item.getItem(), item.getCount());
					slot.set(ItemStack.EMPTY);
				}
			}
		}
		CraftorioMisc.setPoints(CraftorioMisc.getPoints(player).add(pointsToGive),player);

		for (CraftorioContract contract : new ArrayList<>(CraftorioMisc.getCraftorioContracts(player))){
			contract.addSinkedListValue(sinkedItems,player);
		}
	}

	public Container getContainer() {
		return this.container;
	}

	public int getRowCount() {
		return this.containerRows;
	}

	public boolean isGambling() {
		return gambling;
	}

	public BigInteger getEscrowPoints() {
		return escrowPoints;
	}

	public int getWinStreak() {
		return winStreak;
	}

	public BigInteger getLastLossRefund() {
		return lastLossRefund;
	}

	public BigInteger computeContainerValue() {
		BigInteger total = BigInteger.ZERO;
		for (int slot = 0; slot < this.containerRows * 9; slot++) {
			ItemStack stack = container.getItem(slot);
			if (!stack.isEmpty()) {
				total = total.add(CraftorioMisc.checkValue(stack, player, true));
			}
		}
		return total;
	}

	public Boolean flipDoubleOrNothing(boolean forceHeads) {
		if (!gambling) {
			if (!CraftorioMisc.hasUnlockedUpgrade(player, Craftorio.prefix("double_or_nothing_unlock"))) return null;

			BigInteger value = computeContainerValue();
			if (value.signum() == 0) return null;

			container.clearContent();
			gambling = true;
			escrowPoints = value;
			winStreak = 0;
		}

		lastLossRefund = BigInteger.ZERO;

		double headsChance = CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.BET_ODDS, 0.5);
		boolean heads = forceHeads || player.getRandom().nextDouble() < headsChance;

		if (heads) {
			double headsBonusMultiplier = CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.BET_BONUS, 2.0);
			escrowPoints = new BigDecimal(escrowPoints).multiply(BigDecimal.valueOf(headsBonusMultiplier))
					.setScale(0, RoundingMode.HALF_UP).toBigInteger();
			winStreak++;
		} else {
			double refundFraction = CraftorioMisc.getUpgradeModifierSum(player, ModifierTarget.LOST_BET_REFUND, UpgradeOperation.ADD);
			if (refundFraction > 0) {
				BigInteger refund = new BigDecimal(escrowPoints).multiply(BigDecimal.valueOf(refundFraction))
						.setScale(0, RoundingMode.HALF_UP).toBigInteger();
				if (refund.signum() > 0) {
					pendingLossRefund = pendingLossRefund.add(refund);
					lastLossRefund = refund;
				}
			}

			escrowPoints = BigInteger.ZERO;
			gambling = false;
		}
		return heads;
	}

	public void cashOutDoubleOrNothing() {
		if (!gambling) return;
		CraftorioMisc.setPoints(CraftorioMisc.getPoints(player).add(escrowPoints), player);
		resetGamble();
	}

	public void resetGamble() {
		gambling = false;
		escrowPoints = BigInteger.ZERO;
		winStreak = 0;
	}
}

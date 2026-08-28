//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.crimsoncrips.craftorio.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;

import java.math.BigInteger;

public class SinkerMenu extends AbstractContainerMenu {
	private static final int SLOTS_PER_ROW = 9;
	private final Container container;
	private final int containerRows;
	private final Player player;


	private SinkerMenu(MenuType<?> type, int containerId, Inventory playerInventory, int rows) {
		this(type, containerId, playerInventory, new SimpleContainer(9 * rows), rows);
	}

	public static SinkerMenu sinkerMenu(int containerId, Inventory playerInventory) {
		return new SinkerMenu(CraftorioMenuTypes.SINKER.get(), containerId, playerInventory, 4);
	}

	public static SinkerMenu sinkMenu(int containerId, Inventory playerInventory,Container container) {
		return new SinkerMenu(CraftorioMenuTypes.SINKER.get(), containerId, playerInventory, container,4);
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
		this.container.stopOpen(player);
	}

	public void sinkPoints(){
		BigInteger pointsToGive = BigInteger.ZERO;
		ServerLevel serverLevel = (ServerLevel) player.level();
		for(int j = 0; j < this.containerRows; ++j) {
			for(int k = 0; k < 9; ++k) {
				this.addSlot(new Slot(container, k + j * 9, 8 + k * 18, 18 + j * 18));
				Slot slot = this.getSlot(k + j * 9);
				ItemStack item = slot.getItem();

				if (!item.isEmpty() && !serverLevel.isClientSide()){
					pointsToGive = pointsToGive.add(CraftorioMisc.checkValue(item));
					slot.set(ItemStack.EMPTY);
				}
			}
		}
		CraftorioMisc.addPoints(serverLevel, pointsToGive);
	}

	public Container getContainer() {
		return this.container;
	}

	public int getRowCount() {
		return this.containerRows;
	}
}

package org.crimsoncrips.craftorio.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.block.entity.AutoValueCondenserBlockEntity;

import javax.annotation.Nullable;

public class AutoValueCondenserMenu extends AbstractContainerMenu {

    private static final int FILL_PERCENT_INDEX = 0;
    private static final int CARRIER_SLOT_INDEX = 0;

    @Nullable
    private final AutoValueCondenserBlockEntity blockEntity;
    private final SimpleContainerData data;

    public static AutoValueCondenserMenu clientMenu(int containerId, Inventory playerInventory) {
        return new AutoValueCondenserMenu(containerId, playerInventory, null);
    }

    public AutoValueCondenserMenu(int containerId, Inventory playerInventory, @Nullable AutoValueCondenserBlockEntity blockEntity) {
        super(CraftorioMenuTypes.AUTO_VALUE_CONDENSER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = new SimpleContainerData(1);
        if (blockEntity != null) {
            this.data.set(FILL_PERCENT_INDEX, blockEntity.getFillPercent());
        }
        addDataSlots(this.data);

        Container carrierContainer = blockEntity != null ? blockEntity : new SimpleContainer(1);
        int carrierIndex = blockEntity != null ? AutoValueCondenserBlockEntity.CARRIER_SLOT_INDEX : 0;
        this.addSlot(new CarrierSlot(carrierContainer, carrierIndex, 91, 60));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 92 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 150));
        }
    }

    @Override
    public void broadcastChanges() {
        if (blockEntity != null) {
            this.data.set(FILL_PERCENT_INDEX, blockEntity.getFillPercent());
        }
        super.broadcastChanges();
    }

    public int getFillPercent() {
        return this.data.get(FILL_PERCENT_INDEX);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == CARRIER_SLOT_INDEX) {
                if (!this.moveItemStackTo(stack, CARRIER_SLOT_INDEX + 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, CARRIER_SLOT_INDEX, CARRIER_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }

    public static final class CarrierSlot extends Slot {
        public CarrierSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}

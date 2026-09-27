package org.crimsoncrips.craftorio.inventory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.registries.CraftorioMenuTypes;

import java.math.BigInteger;

public class ValueCondenserMenu extends AbstractContainerMenu {
    private static final int SLOTS_PER_ROW = 9;
    private static final int MAIN_ROWS = 4;
    private static final int MAIN_SLOT_COUNT = MAIN_ROWS * SLOTS_PER_ROW;
    private static final int CARRIER_SLOT_INDEX = MAIN_SLOT_COUNT;

    private final Container container;
    private final Player player;

    private ValueCondenserMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(MAIN_SLOT_COUNT + 1));
    }

    public static ValueCondenserMenu clientMenu(int containerId, Inventory playerInventory) {
        return new ValueCondenserMenu(CraftorioMenuTypes.VALUE_CONDENSER.get(), containerId, playerInventory);
    }

    public static ValueCondenserMenu serverMenu(int containerId, Inventory playerInventory, Container container) {
        return new ValueCondenserMenu(CraftorioMenuTypes.VALUE_CONDENSER.get(), containerId, playerInventory, container);
    }

    public Player getPlayer() {
        return player;
    }

    public ValueCondenserMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container) {
        super(type, containerId);
        checkContainerSize(container, MAIN_SLOT_COUNT + 1);
        this.container = container;
        this.player = playerInventory.player;
        container.startOpen(playerInventory.player);

        for (int j = 0; j < MAIN_ROWS; ++j) {
            for (int k = 0; k < SLOTS_PER_ROW; ++k) {
                this.addSlot(new Slot(container, k + j * SLOTS_PER_ROW, 8 + k * 18, 54 + j * 18));
            }
        }

        this.addSlot(new CarrierSlot(container, CARRIER_SLOT_INDEX, 80, 7));

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 140 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
        }
    }

    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < CARRIER_SLOT_INDEX + 1) {
                if (!this.moveItemStackTo(itemstack1, CARRIER_SLOT_INDEX + 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, MAIN_SLOT_COUNT, false)) {
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

    public void condenseValue() {
        Level level = player.level();
        if (level.isClientSide()) return;

        ItemStack carrier = container.getItem(CARRIER_SLOT_INDEX);
        if (carrier.isEmpty()) return;

        boolean capped = CraftorioMisc.condenseIntoCarrier(container, 0, MAIN_SLOT_COUNT, carrier, player);
        container.setChanged();
        if (capped) {
            player.displayClientMessage(Component.translatable("misc.craftorio.value_condenser_full", CraftorioMisc.bigIntFormat(CraftorioMisc.valueCondenserCap(player))).withStyle(ChatFormatting.YELLOW), true);
        }
    }

    public Container getContainer() {
        return this.container;
    }

    public int getMainSlotCount() {
        return MAIN_SLOT_COUNT;
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

package org.crimsoncrips.craftorio.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.server.CraftorioContractDraftStore;

public class ContractCreatorMenu extends AbstractContainerMenu {

    public static final int GRID_COLUMNS = 10;
    public static final int GRID_ROWS = 10;
    public static final int BOUNTY_SLOTS = GRID_COLUMNS * GRID_ROWS;
    public static final int REWARD_SLOTS = GRID_COLUMNS * GRID_ROWS;
    public static final int TOTAL_SLOTS = BOUNTY_SLOTS + REWARD_SLOTS;
    public static final int PLAYER_INV_SLOT_COUNT = 36;
    public static final int PLAYER_INV_END = TOTAL_SLOTS + PLAYER_INV_SLOT_COUNT;

    public static final int PANEL_WIDTH = 280;
    public static final int GRID_LEFT = (PANEL_WIDTH - GRID_COLUMNS * 18) / 2;
    public static final int GRID_TOP = 60;
    public static final int PLAYER_INV_GAP = 62;

    private final Container container;
    private final Container destroyContainer = new DiscardingContainer();
    private final Slot destroySlot;
    private final Player player;
    private boolean showingReward = false;

    public static ContractCreatorMenu contractCreatorMenu(int containerId, Inventory playerInventory) {
        return new ContractCreatorMenu(CraftorioMenuTypes.CONTRACT_CREATOR.get(), containerId, playerInventory, new SimpleContainer(TOTAL_SLOTS));
    }

    public static ContractCreatorMenu serverContractCreatorMenu(int containerId, Inventory playerInventory) {
        return new ContractCreatorMenu(CraftorioMenuTypes.CONTRACT_CREATOR.get(), containerId, playerInventory,
                CraftorioContractDraftStore.getOrCreateDraft(playerInventory.player.getUUID()));
    }

    public ContractCreatorMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container) {
        super(type, containerId);
        checkContainerSize(container, TOTAL_SLOTS);
        this.container = container;
        this.player = playerInventory.player;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < GRID_ROWS; ++row) {
            for (int col = 0; col < GRID_COLUMNS; ++col) {
                int index = row * GRID_COLUMNS + col;
                this.addSlot(new ViewSlot(container, index, GRID_LEFT + col * 18, GRID_TOP + row * 18, true));
            }
        }

        for (int row = 0; row < GRID_ROWS; ++row) {
            for (int col = 0; col < GRID_COLUMNS; ++col) {
                int index = BOUNTY_SLOTS + row * GRID_COLUMNS + col;
                this.addSlot(new ViewSlot(container, index, GRID_LEFT + col * 18, GRID_TOP + row * 18, false));
            }
        }

        int playerInvTop = GRID_TOP + GRID_ROWS * 18 + PLAYER_INV_GAP;
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, GRID_LEFT + j1 * 18, playerInvTop + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, GRID_LEFT + i1 * 18, playerInvTop + 58));
        }

        int destroyX = GRID_LEFT + GRID_COLUMNS * 18 + 21;
        int destroyY = playerInvTop + 18;
        this.destroySlot = this.addSlot(new Slot(destroyContainer, 0, destroyX, destroyY));
    }

    public void showBountyView() {
        this.showingReward = false;
    }

    public void showRewardView() {
        this.showingReward = true;
    }

    public boolean isShowingReward() {
        return showingReward;
    }

    public void clearActiveGrid() {
        int start = showingReward ? BOUNTY_SLOTS : 0;
        int end = showingReward ? TOTAL_SLOTS : BOUNTY_SLOTS;
        for (int i = start; i < end; i++) {
            container.setItem(i, ItemStack.EMPTY);
        }
    }

    public int findNextEmptySlot() {
        int start = showingReward ? BOUNTY_SLOTS : 0;
        int end = showingReward ? TOTAL_SLOTS : BOUNTY_SLOTS;
        for (int i = start; i < end; i++) {
            if (container.getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    public Player getPlayer() {
        return player;
    }

    public Container getContainer() {
        return container;
    }

    public Slot getDestroySlot() {
        return destroySlot;
    }

    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem() && slot.isActive()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < TOTAL_SLOTS) {
                if (!this.moveItemStackTo(itemstack1, TOTAL_SLOTS, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INV_END) {
                int gridStart = showingReward ? BOUNTY_SLOTS : 0;
                int gridEnd = showingReward ? TOTAL_SLOTS : BOUNTY_SLOTS;
                if (!this.moveItemStackTo(itemstack1, gridStart, gridEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
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

    private static class DiscardingContainer implements Container {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }

    private class ViewSlot extends Slot {
        private final boolean bountySlot;

        public ViewSlot(Container container, int index, int x, int y, boolean bountySlot) {
            super(container, index, x, y);
            this.bountySlot = bountySlot;
        }

        @Override
        public boolean isActive() {
            return bountySlot != showingReward;
        }
    }
}

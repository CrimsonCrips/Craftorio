package org.crimsoncrips.craftorio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.crimsoncrips.craftorio.CraftorioDataComponents;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.AutoValueCondenserMenu;

import java.math.BigInteger;
import java.util.UUID;

public class AutoValueCondenserBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int MAIN_SLOT_COUNT = 100;
    public static final int CARRIER_SLOT_INDEX = MAIN_SLOT_COUNT;
    public static final int CONTAINER_SIZE = MAIN_SLOT_COUNT + 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private UUID ownerId;

    public AutoValueCondenserBlockEntity(BlockPos pos, BlockState blockState) {
        super(CraftorioBlockEntityTypes.AUTO_VALUE_CONDENSER.get(), pos, blockState);
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
        setChanged();
    }

    public UUID getOwner() {
        return ownerId;
    }

    public int getFillPercent() {
        int filled = 0;
        for (int i = 0; i < MAIN_SLOT_COUNT; i++) {
            if (!items.get(i).isEmpty()) filled++;
        }
        return filled * 100 / MAIN_SLOT_COUNT;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        checkAutoCondense();
    }

    private void checkAutoCondense() {
        if (level == null || level.isClientSide) return;

        ItemStack carrier = items.get(CARRIER_SLOT_INDEX);
        if (carrier.isEmpty()) return;

        if (!(level.getServer().getPlayerList().getPlayer(ownerId) instanceof ServerPlayer owner)) return;

        BigInteger valueToAdd = BigInteger.ZERO;
        for (int i = 0; i < MAIN_SLOT_COUNT; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            valueToAdd = valueToAdd.add(CraftorioMisc.checkValue(stack, owner, false));
            items.set(i, ItemStack.EMPTY);
        }

        if (valueToAdd.compareTo(BigInteger.ZERO) > 0) {
            BigInteger existing = carrier.get(CraftorioDataComponents.CONDENSED_VALUE);
            carrier.set(CraftorioDataComponents.CONDENSED_VALUE, (existing != null ? existing : BigInteger.ZERO).add(valueToAdd));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, this.items, provider);
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, provider);
        this.ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr((double) this.worldPosition.getX() + 0.5, (double) this.worldPosition.getY() + 0.5, (double) this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AutoValueCondenserMenu(id, inventory, this);
    }
}

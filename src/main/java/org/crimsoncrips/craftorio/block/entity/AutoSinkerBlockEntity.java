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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.AutoSinkerMenu;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutoSinkerBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int CONTAINER_SIZE = 100;
    public static final int DEFAULT_THRESHOLD = 100;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int sinkThresholdPercent = DEFAULT_THRESHOLD;
    private UUID ownerId;

    public AutoSinkerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CraftorioBlockEntityTypes.AUTO_SINKER.get(), pos, blockState);
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
        setChanged();
    }

    public UUID getOwner() {
        return ownerId;
    }

    public int getSinkThresholdPercent() {
        return sinkThresholdPercent;
    }

    public void setSinkThresholdPercent(int percent) {
        this.sinkThresholdPercent = Math.max(1, Math.min(100, percent));
        setChanged();
        checkAutoSink();
    }

    public int getFillPercent() {
        int filled = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) filled++;
        }
        return filled * 100 / CONTAINER_SIZE;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        checkAutoSink();
    }

    private void checkAutoSink() {
        if (level == null || level.isClientSide) return;
        if (getFillPercent() < sinkThresholdPercent) return;

        if (!(level.getServer().getPlayerList().getPlayer(ownerId) instanceof ServerPlayer owner)) return;

        BigInteger pointsToGive = BigInteger.ZERO;
        List<ItemStack> sinkedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            sinkedItems.add(stack);
            pointsToGive = pointsToGive.add(CraftorioMisc.checkValue(stack, owner, false));
            items.set(i, ItemStack.EMPTY);
        }

        CraftorioMisc.setPoints(CraftorioMisc.getPoints(owner).add(pointsToGive), owner);

        for (CraftorioShipmentContract contract : new ArrayList<>(CraftorioMisc.getCraftorioContracts(owner))) {
            contract.addSinkedListValue(sinkedItems, owner);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, this.items, provider);
        tag.putInt("SinkThreshold", sinkThresholdPercent);
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, provider);
        this.sinkThresholdPercent = tag.contains("SinkThreshold") ? tag.getInt("SinkThreshold") : DEFAULT_THRESHOLD;
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
        return new AutoSinkerMenu(id, inventory, this);
    }
}

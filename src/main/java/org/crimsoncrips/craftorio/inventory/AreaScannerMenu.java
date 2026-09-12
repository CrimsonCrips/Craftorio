package org.crimsoncrips.craftorio.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.block.entity.AreaScannerBlockEntity;

import javax.annotation.Nullable;

public class AreaScannerMenu extends AbstractContainerMenu {

    private static final int OFFSET_X_INDEX = 0;
    private static final int OFFSET_Y_INDEX = 1;
    private static final int OFFSET_Z_INDEX = 2;
    private static final int SIZE_X_INDEX = 3;
    private static final int SIZE_Y_INDEX = 4;
    private static final int SIZE_Z_INDEX = 5;

    @Nullable
    private final AreaScannerBlockEntity blockEntity;
    private final SimpleContainerData data;

    public static AreaScannerMenu clientMenu(int containerId, Inventory playerInventory) {
        return new AreaScannerMenu(containerId, playerInventory, null);
    }

    public AreaScannerMenu(int containerId, Inventory playerInventory, @Nullable AreaScannerBlockEntity blockEntity) {
        super(CraftorioMenuTypes.AREA_SCANNER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = new SimpleContainerData(6);
        syncFromBlockEntity();
        addDataSlots(this.data);
    }

    private void syncFromBlockEntity() {
        if (blockEntity == null) return;
        this.data.set(OFFSET_X_INDEX, blockEntity.getOffsetX());
        this.data.set(OFFSET_Y_INDEX, blockEntity.getOffsetY());
        this.data.set(OFFSET_Z_INDEX, blockEntity.getOffsetZ());
        this.data.set(SIZE_X_INDEX, blockEntity.getSizeX());
        this.data.set(SIZE_Y_INDEX, blockEntity.getSizeY());
        this.data.set(SIZE_Z_INDEX, blockEntity.getSizeZ());
    }

    @Override
    public void broadcastChanges() {
        syncFromBlockEntity();
        super.broadcastChanges();
    }

    public int getOffsetX() { return this.data.get(OFFSET_X_INDEX); }
    public int getOffsetY() { return this.data.get(OFFSET_Y_INDEX); }
    public int getOffsetZ() { return this.data.get(OFFSET_Z_INDEX); }
    public int getSizeX() { return this.data.get(SIZE_X_INDEX); }
    public int getSizeY() { return this.data.get(SIZE_Y_INDEX); }
    public int getSizeZ() { return this.data.get(SIZE_Z_INDEX); }

    public void updateRegion(int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ) {
        if (blockEntity == null) return;
        blockEntity.setRegion(offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ);
        syncFromBlockEntity();
    }

    public void scan(ServerPlayer player) {
        if (blockEntity == null) return;
        blockEntity.scanAndWriteFile(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }
}

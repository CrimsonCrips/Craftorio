//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.crimsoncrips.craftorio.block.entity;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.crimsoncrips.craftorio.CraftorioBlockEntities;
import org.crimsoncrips.craftorio.block.SinkerBlock;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

public class SinkerBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openersCounter;


    public SinkerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CraftorioBlockEntities.SINKER.get(), pos, blockState);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level p_155062_, BlockPos p_155063_, BlockState p_155064_) {
                updateBlockState(p_155064_, true);
            }

            protected void onClose(Level p_155072_, BlockPos p_155073_, BlockState p_155074_) {
                updateBlockState(p_155074_, false);
            }

            protected void openerCountChanged(Level p_155066_, BlockPos p_155067_, BlockState p_155068_, int p_155069_, int p_155070_) {
            }

            protected boolean isOwnContainer(Player p_155060_) {
                if (p_155060_.containerMenu instanceof SinkerMenu) {
                    Container container = ((SinkerMenu)p_155060_.containerMenu).getContainer();
                    return container == SinkerBlockEntity.this;
                } else {
                    return false;
                }
            }
        };
    }

    void updateBlockState(BlockState state, boolean open) {
        this.level.setBlock(this.getBlockPos(), state.setValue(SinkerBlock.OPEN, open), 3);
    }

    protected void saveAdditional(CompoundTag p_187459_, HolderLookup.Provider p_323686_) {
        super.saveAdditional(p_187459_, p_323686_);
        System.out.println("IT WORKS ");
        ContainerHelper.saveAllItems(p_187459_, this.items, p_323686_);

    }

    protected void loadAdditional(CompoundTag p_155055_, HolderLookup.Provider p_324230_) {
        super.loadAdditional(p_155055_, p_324230_);
        System.out.println("IT WORKS 2");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(p_155055_, this.items, p_324230_);

    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public int getContainerSize() {
        return 9 * 4;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    protected Component getDefaultName() {
        return this.getBlockState().getBlock().getName();
    }

    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return SinkerMenu.sinkMenu(id, player);
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }


}

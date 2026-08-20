package org.crimsoncrips.craftorio.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioBlockEntities;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;
import org.jetbrains.annotations.Nullable;

public class CraftorioSinkerBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private static final int SIZE = 9 * 5;
    public NonNullList<ItemStack> contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int i) {
            return 0;
        }

        @Override
        public void set(int i, int i1) {

        }

        @Override
        public int getCount() {
            return 9 * 3;
        }
    };

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {


        @Override
        protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int id, int param) {
            Block block = state.getBlock();
            level.blockEvent(pos, block, 1, param);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof SinkerMenu) {
                Container container = ((SinkerMenu)player.containerMenu).getContainer();
                return container == CraftorioSinkerBlockEntity.this;
            } else {
                return false;
            }
        }
    };
    private final ChestLidController chestLidController = new ChestLidController();
    @Nullable
    public ResolvableProfile owner;

    public CraftorioSinkerBlockEntity(BlockPos pos, BlockState state) {
        super(CraftorioBlockEntities.SINKER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CraftorioSinkerBlockEntity te) {
        te.chestLidController.tickLid();
    }

    public ContainerOpenersCounter getOpenersCounter() {
        return this.openersCounter;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.contents;
    }

    @Override
    public void setItems(NonNullList<ItemStack> items) {
        this.contents = items;
    }

    @Override
    protected Component getDefaultName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return new SinkerMenu(CraftorioMenuTypes.SINKER.get(),id,player,3);
    }


    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, this.contents, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.contents, provider);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return 0;
    }

    //if we have a dead player UUID set, then only that player can open the casket
    @Override
    public boolean stillValid(Player player) {
        if (this.owner != null) {
            if (player.hasPermissions(3) || player.getGameProfile().equals(this.owner.gameProfile())) {
                return super.stillValid(player);
            } else {
                return false;
            }
        } else {
            return super.stillValid(player);
        }
    }

    //remove stored player when chest is broken
    @Override
    public void setRemoved() {
        this.owner = null;
        super.setRemoved();
    }
}

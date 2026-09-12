package org.crimsoncrips.craftorio.inventory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.block.entity.AutoSinkerBlockEntity;

import javax.annotation.Nullable;

public class AutoSinkerMenu extends AbstractContainerMenu {

    private static final int FILL_PERCENT_INDEX = 0;
    private static final int THRESHOLD_PERCENT_INDEX = 1;

    @Nullable
    private final AutoSinkerBlockEntity blockEntity;
    private final SimpleContainerData data;

    public static AutoSinkerMenu clientMenu(int containerId, Inventory playerInventory) {
        return new AutoSinkerMenu(containerId, playerInventory, null);
    }

    public AutoSinkerMenu(int containerId, Inventory playerInventory, @Nullable AutoSinkerBlockEntity blockEntity) {
        super(CraftorioMenuTypes.AUTO_SINKER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = new SimpleContainerData(2);
        if (blockEntity != null) {
            this.data.set(FILL_PERCENT_INDEX, blockEntity.getFillPercent());
            this.data.set(THRESHOLD_PERCENT_INDEX, blockEntity.getSinkThresholdPercent());
        }
        addDataSlots(this.data);
    }

    @Override
    public void broadcastChanges() {
        if (blockEntity != null) {
            this.data.set(FILL_PERCENT_INDEX, blockEntity.getFillPercent());
            this.data.set(THRESHOLD_PERCENT_INDEX, blockEntity.getSinkThresholdPercent());
        }
        super.broadcastChanges();
    }

    public int getFillPercent() {
        return this.data.get(FILL_PERCENT_INDEX);
    }

    public int getThresholdPercent() {
        return this.data.get(THRESHOLD_PERCENT_INDEX);
    }

    public void setThresholdPercent(int percent) {
        if (blockEntity == null) return;
        blockEntity.setSinkThresholdPercent(percent);
        this.data.set(THRESHOLD_PERCENT_INDEX, blockEntity.getSinkThresholdPercent());
    }

    public void setOwnerToSelf(ServerPlayer player) {
        if (blockEntity == null || blockEntity.getLevel() == null) return;

        if (CraftorioMisc.universalBased(CraftorioMisc.universalLevel(player))) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.auto_sinker_owner_universal_error").withStyle(ChatFormatting.RED));
            return;
        }

        blockEntity.setOwner(player.getUUID());
        player.sendSystemMessage(Component.translatable("misc.craftorio.auto_sinker_owner_set").withStyle(ChatFormatting.GREEN));
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

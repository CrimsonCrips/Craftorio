package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.Craftorio;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(AbstractContainerMenu.class)
public abstract class CraftorioContainerMenuMixin {

    @WrapMethod(method = "triggerSlotListeners")
    private void craftorio$unlockOnInventoryChanged(int slotIndex, ItemStack stack, Supplier<ItemStack> supplier, Operation<Void> original) {
        original.call(slotIndex, stack, supplier);

        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        Slot slot = self.getSlot(slotIndex);

        if (slot.container instanceof Inventory inventory && inventory.player instanceof ServerPlayer serverPlayer) {
            ItemStack current = slot.getItem();
            if (!current.isEmpty()) {
                Craftorio.UNLOCKED_ITEMS.unlock(serverPlayer, current.getItem());
            }
        }
    }

}

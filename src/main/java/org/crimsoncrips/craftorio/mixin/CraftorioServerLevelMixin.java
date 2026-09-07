package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.crimsoncrips.craftorio.server.BorderCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
public abstract class CraftorioServerLevelMixin {

    @WrapMethod(method = "mayInteract")
    private boolean gateCraftorioBorderInteraction(Player player, BlockPos pos, Operation<Boolean> original) {
        return original.call(player, pos) && BorderCollisionHooks.isWithinCraftorioBorders(player, pos);
    }

}

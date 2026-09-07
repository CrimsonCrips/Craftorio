package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.border.WorldBorder;
import org.crimsoncrips.craftorio.server.BorderCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CraftorioGamePacketListenerMixin {

    @Shadow public ServerPlayer player;

    @WrapOperation(
            method = "handleInteract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean gateCraftorioBorderEntityInteraction(WorldBorder worldBorder, BlockPos pos, Operation<Boolean> original) {
        return original.call(worldBorder, pos) && BorderCollisionHooks.isWithinCraftorioBorders(this.player, pos);
    }

}

package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.crimsoncrips.craftorio.item.ClaimChunkItem;
import org.crimsoncrips.craftorio.server.BorderCollisionHooks;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(MultiPlayerGameMode.class)
public abstract class CraftorioMultiPlayerGameModeMixin {

    @Shadow @Final private Minecraft minecraft;

    @WrapMethod(method = "startDestroyBlock")
    private boolean gateCraftorioBorderStartDestroy(BlockPos loc, Direction face, Operation<Boolean> original) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && !(BorderCollisionHooks.isWithinCraftorioBorders(player, loc) && ChunkCollisionHooks.isWithinClaimedChunk(player, loc))) {
            return false;
        }
        return original.call(loc, face);
    }

    @WrapMethod(method = "continueDestroyBlock")
    private boolean gateCraftorioBorderContinueDestroy(BlockPos posBlock, Direction directionFacing, Operation<Boolean> original) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && !(BorderCollisionHooks.isWithinCraftorioBorders(player, posBlock) && ChunkCollisionHooks.isWithinClaimedChunk(player, posBlock))) {
            return false;
        }
        return original.call(posBlock, directionFacing);
    }

    @WrapMethod(method = "useItemOn")
    private InteractionResult gateCraftorioBorderUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, Operation<InteractionResult> original) {
        boolean claimingChunk = player.getItemInHand(hand).getItem() instanceof ClaimChunkItem;
        if (!claimingChunk && !(BorderCollisionHooks.isWithinCraftorioBorders(player, result.getBlockPos()) && ChunkCollisionHooks.isWithinClaimedChunk(player, result.getBlockPos()))) {
            return InteractionResult.FAIL;
        }
        return original.call(player, hand, result);
    }

}

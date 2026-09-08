package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.BorderCollisionHooks;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;


@Mixin(CollisionGetter.class)
public interface CraftorioCollisionGetter {

    @WrapMethod(method = "borderCollision")
    default VoxelShape gatedBorderCollision(Entity entity, AABB box, Operation<VoxelShape> original) {
        @Nullable VoxelShape borderCollision = original.call(entity, box);

        if ((Object) this instanceof Level level) {
            if (CraftorioMisc.chunkBased(level)) {
                borderCollision = ChunkCollisionHooks.combineWorldAndChunkBorders(level, entity, borderCollision);
            }

            if (entity instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.connection != null) {
                    borderCollision = BorderCollisionHooks.combineCraftorioBorders(level, serverPlayer, borderCollision);
                }
            } else if (entity instanceof Player player) {
                borderCollision = BorderCollisionHooks.combineCraftorioBorders(level, player, borderCollision);
            }
        }

        return borderCollision;
    }

}
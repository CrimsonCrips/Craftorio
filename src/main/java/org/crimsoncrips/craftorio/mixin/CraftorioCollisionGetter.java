package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.BorderCollisionHooks;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(value = CollisionGetter.class, priority = 2000)
public interface CraftorioCollisionGetter {

    @WrapMethod(method = "noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z")
    default boolean gatedNoCollision(@Nullable Entity entity, AABB collisionBox, Operation<Boolean> original) {
        if (!original.call(entity, collisionBox)) {
            return false;
        }
        if (entity == null) {
            return true;
        }

        if ((Object) this instanceof Level level) {
            VoxelShape borderCollision = null;
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

            if (borderCollision != null && Shapes.joinIsNotEmpty(borderCollision, Shapes.create(collisionBox), BooleanOp.AND)) {
                return false;
            }
        }

        return true;
    }

}
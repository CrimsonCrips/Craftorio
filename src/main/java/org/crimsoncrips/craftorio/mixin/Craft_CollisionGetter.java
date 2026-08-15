package org.crimsoncrips.craftorio.mixin;



import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;


@Mixin(CollisionGetter.class)
public interface Craft_CollisionGetter {

    @WrapMethod(method = "borderCollision")
    default VoxelShape gatedBorderCollision(Entity entity, AABB box, Operation<VoxelShape> original) {
        @Nullable VoxelShape borderCollision = original.call(entity, box);

        // Level does in fact implement CollisionGetter
        //noinspection ConstantValue
        if ((Object) this instanceof Level level && ChunkCollisionHooks.levelHasUnlockableChunks(level)) {
            return ChunkCollisionHooks.combineWorldAndChunkBorders(level, entity, borderCollision);
        }

        return borderCollision;
    }

}
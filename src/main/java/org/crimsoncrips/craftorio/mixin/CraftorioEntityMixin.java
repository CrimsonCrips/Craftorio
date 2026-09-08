package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.BorderCollisionHooks;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Entity.class)
public class CraftorioEntityMixin {

    @WrapMethod(method = "collectColliders")
    private static List<VoxelShape> addChunkColliders(@Nullable Entity entity, Level level, List<VoxelShape> collisions, AABB boundingBox, Operation<List<VoxelShape>> operation) {
        List<VoxelShape> original = operation.call(entity, level, collisions, boundingBox);

        if (entity instanceof Player player) {
            VoxelShape extra = Shapes.empty();

            if (CraftorioMisc.chunkBased(level)) {
                extra = ChunkCollisionHooks.combineWorldAndChunkBorders(level, entity, extra);
            }

            extra = BorderCollisionHooks.combineCraftorioBorders(level, player, extra);

            if (!extra.isEmpty()) {
                return ConcatenatedListView.of(original, List.of(extra));
            }
        }

        return original;
    }

}
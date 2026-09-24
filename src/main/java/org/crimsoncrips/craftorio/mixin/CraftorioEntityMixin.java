package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.server.border.BorderCollisionHooks;
import org.crimsoncrips.craftorio.server.border.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = Entity.class, priority = 2000)
public class CraftorioEntityMixin {

    @WrapMethod(method = "collectColliders")
    private static List<VoxelShape> addChunkColliders(@Nullable Entity entity, Level level, List<VoxelShape> collisions, AABB boundingBox, Operation<List<VoxelShape>> operation) {
        List<VoxelShape> original = operation.call(entity, level, collisions, boundingBox);

        if (entity instanceof Player player) {
            VoxelShape extra = craftorioBorderShape(level, player);

            if (!extra.isEmpty()) {
                return ConcatenatedListView.of(original, List.of(extra));
            }
        }

        return original;
    }

    @WrapMethod(method = "collide")
    private Vec3 clampToCraftorioBorder(Vec3 movement, Operation<Vec3> operation) {
        Vec3 result = operation.call(movement);

        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return result;
        }

        VoxelShape border = craftorioBorderShape(self.level(), player);
        if (border.isEmpty()) {
            return result;
        }

        List<VoxelShape> shapes = List.of(border);
        AABB box = self.getBoundingBox();
        double x = result.x;
        double y = result.y;
        double z = result.z;

        if (y != 0.0) {
            y = Shapes.collide(Direction.Axis.Y, box, shapes, y);
            if (y != 0.0) {
                box = box.move(0.0, y, 0.0);
            }
        }

        boolean zBeforeX = Math.abs(x) < Math.abs(z);
        if (zBeforeX && z != 0.0) {
            z = Shapes.collide(Direction.Axis.Z, box, shapes, z);
            if (z != 0.0) {
                box = box.move(0.0, 0.0, z);
            }
        }

        if (x != 0.0) {
            x = Shapes.collide(Direction.Axis.X, box, shapes, x);
            if (!zBeforeX && x != 0.0) {
                box = box.move(x, 0.0, 0.0);
            }
        }

        if (!zBeforeX && z != 0.0) {
            z = Shapes.collide(Direction.Axis.Z, box, shapes, z);
        }

        return new Vec3(x, y, z);
    }

    private static VoxelShape craftorioBorderShape(Level level, Player player) {
        VoxelShape border = Shapes.empty();

        if (level.dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)) {
            return border;
        }

        if (CraftorioMisc.chunkBased(level)) {
            border = ChunkCollisionHooks.combineWorldAndChunkBorders(level, player, border);
        }

        return BorderCollisionHooks.combineCraftorioBorders(level, player, border);
    }

}
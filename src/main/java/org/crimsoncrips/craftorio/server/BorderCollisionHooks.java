package org.crimsoncrips.craftorio.server;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BorderCollisionHooks {

    public static @NotNull VoxelShape combineCraftorioBorders(Level level, Player player, @Nullable VoxelShape original) {
        VoxelShape result = original == null ? Shapes.empty() : original;

        List<CraftorioBorder> borders = CraftorioMisc.getCraftorioBorders(player);

        for (CraftorioBorder border : borders) {
            

            VoxelShape insideShape = Shapes.box(
                    border.getMinX(),
                    Double.NEGATIVE_INFINITY,
                    border.getMinZ(),
                    border.getMaxX(),
                    Double.POSITIVE_INFINITY,
                    border.getMaxZ()
            );

            VoxelShape outsideAsSolid = Shapes.join(Shapes.INFINITY, insideShape, BooleanOp.ONLY_FIRST);

            result = Shapes.or(result, outsideAsSolid);
        }

        return result;
    }
}
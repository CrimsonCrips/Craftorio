package org.crimsoncrips.craftorio.server;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.OWNED;

public class ChunkCollisionHooks {

    //Created By Drullkus

    public static boolean levelHasUnlockableChunks(Level level) {
        return level.dimensionType().bedWorks();
    }

    public static @NotNull VoxelShape combineWorldAndChunkBorders(Level level, Entity entity, @Nullable VoxelShape worldBorderCollision) {
        VoxelShape chunkBorderCollisions = Shapes.INFINITY; // all chunks are solid
        ChunkPos entityChunkPos = entity.chunkPosition();

        for (int z = -2; z < 2; z++) {
            for (int x = -2; x < 2; x++) {
                int chunkX = entityChunkPos.x + x;
                int chunkZ = entityChunkPos.z + z;

                if (!isChunkUnlocked(level, entity, chunkX, chunkZ)) {
                    continue;
                }

                VoxelShape chunkShape = Shapes.box(
                        SectionPos.sectionToBlockCoord(chunkX),
                        Double.NEGATIVE_INFINITY,
                        SectionPos.sectionToBlockCoord(chunkZ),
                        SectionPos.sectionToBlockCoord(chunkX) + 16,
                        Double.POSITIVE_INFINITY,
                        SectionPos.sectionToBlockCoord(chunkZ) + 16
                );

                // Make a hole in the world solid
                chunkBorderCollisions = Shapes.join(
                        chunkBorderCollisions,
                        chunkShape,
                        BooleanOp.ONLY_FIRST
                );
            }
        }

        return worldBorderCollision == null
                ? chunkBorderCollisions
                // OR'ing to the original border collision ensures that players don't enter the damage zone
                : Shapes.or(worldBorderCollision, chunkBorderCollisions);
    }

    private static boolean isChunkUnlocked(Level level, Entity entity, int chunkX, int chunkZ) {
        ChunkAccess chunk = level.getChunk(chunkX,chunkZ);
        if (CraftorioMisc.chunkBased(level)) {
            return chunk.hasData(OWNED) || CraftorioMisc.startingLocations().contains(chunk.getPos());
        } else {
            return true;
        }
    }
}
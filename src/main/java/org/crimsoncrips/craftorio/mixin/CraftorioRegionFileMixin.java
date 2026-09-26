package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioWorldWipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.DataInputStream;
import java.nio.IntBuffer;
import java.nio.file.Path;

@Mixin(RegionFile.class)
public abstract class CraftorioRegionFileMixin {

    @Shadow @Final private Path path;
    @Shadow @Final private IntBuffer timestamps;

    @WrapMethod(method = "getChunkDataInputStream")
    private DataInputStream craftorio$getChunkDataInputStream(ChunkPos pos, Operation<DataInputStream> original) {
        if (CraftorioWorldWipe.isStale(this.path, this.timestamps, pos)) {
            return null;
        }
        return original.call(pos);
    }

    @WrapMethod(method = "doesChunkExist")
    private boolean craftorio$doesChunkExist(ChunkPos pos, Operation<Boolean> original) {
        return !CraftorioWorldWipe.isStale(this.path, this.timestamps, pos) && original.call(pos);
    }

    @WrapMethod(method = "hasChunk")
    private boolean craftorio$hasChunk(ChunkPos pos, Operation<Boolean> original) {
        return !CraftorioWorldWipe.isStale(this.path, this.timestamps, pos) && original.call(pos);
    }

}

package org.amoverride.craftorio;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Unit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.amoverride.craftorio.Craftorio.*;

public class Craft_Misc {

    public static List<ChunkPos> generateSelectionChunks(int startX,int startZ, int endX, int endZ) {

        List<ChunkPos> chunks = new ArrayList<>();

        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        int minY = Math.min(startZ, endZ);
        int maxY = Math.max(startZ, endZ);


        for (int y = minY; y <= maxY; y++) {

            for (int x = minX; x <= maxX; x++) {
                chunks.add(new ChunkPos(x,y));
            }
        }
        return chunks;
    }

    public static void ownLand(ChunkPos chunkPos,Level level, boolean claiming){
        if (level == null) return;
        int claimed_amount = level.getData(AMOUNT_OF_LAND);
        ChunkAccess chunk = level.getChunk(chunkPos.x,chunkPos.z);
        if (claiming && !chunk.hasData(OWNED)){
            chunk.setData(OWNED, Unit.INSTANCE);
            level.setData(AMOUNT_OF_LAND,++claimed_amount);
        }
        if (!claiming && chunk.hasData(OWNED)) {
            chunk.removeData(OWNED);
            level.setData(AMOUNT_OF_LAND,--claimed_amount);
        }

    }

    public static long calculateLandCost(int claimAmount,Level level){
        int currentLand = level.getData(AMOUNT_OF_LAND);
        return (currentLand + claimAmount) > 1 ? ((currentLand + claimAmount) * 10L) : 10L;
    }

}

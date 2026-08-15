package org.crimsoncrips.craftorio;

import com.mojang.datafixers.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

import static org.crimsoncrips.craftorio.Craftorio.*;

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

    public static List<ChunkPos> startingLocations(){
        ChunkPos startPos = new ChunkPos(-1,-1);
        ChunkPos endPos = new ChunkPos(1,1);

        List<ChunkPos> chunkCoords = new ArrayList<>();
        int minX = Math.min(startPos.x, endPos.x);
        int maxX = Math.max(startPos.x, endPos.x);
        int minY = Math.min(startPos.z, endPos.z);
        int maxY = Math.max(startPos.z, endPos.z);


        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                chunkCoords.add(new ChunkPos(x,y));
            }
        }
        return chunkCoords;
    }

    public static void ownLand(ChunkPos chunkPos,Level level, boolean claiming,boolean startingOverride){
        if (level == null) return;
        if (level.isClientSide)return;
        int claimed_amount = level.getData(AMOUNT_OF_LAND);
        ChunkAccess chunk = level.getChunk(chunkPos.x,chunkPos.z);

        if((!startingLocations().contains(chunkPos)) || startingOverride){
            if (claiming && (!chunk.hasData(OWNED))) {
                chunk.setData(OWNED, Unit.INSTANCE);
                int amountSet = claimed_amount + 1;
                level.setData(AMOUNT_OF_LAND, amountSet);
            } else if ((!claiming) && chunk.hasData(OWNED)) {
                chunk.removeData(OWNED);
                int amountSet = claimed_amount - 1;
                level.setData(AMOUNT_OF_LAND,amountSet);
            }
        }

    }

    public static long calculateLandCost(int claimAmount,Level level){
        int currentLand = level.getData(AMOUNT_OF_LAND);
        return (currentLand + claimAmount) > 1 ? ((currentLand + claimAmount) * 10L) : 10L;
    }



}

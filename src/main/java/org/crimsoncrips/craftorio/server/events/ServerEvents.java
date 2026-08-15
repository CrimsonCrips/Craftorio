package org.crimsoncrips.craftorio.server.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.crimsoncrips.craftorio.Craft_Misc;

public class ServerEvents {

    @SubscribeEvent
    public void serverEvents(ServerStartingEvent event) {
        ServerLevel level = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (level == null)return;
        BlockPos blockPos = new BlockPos(0,level.getSharedSpawnPos().getY(),0);
        level.setDefaultSpawnPos(blockPos,0);

        for (ChunkPos chunkSelected : Craft_Misc.startingLocations()) {
            Craft_Misc.ownLand(chunkSelected,level,true,true);
        }

    }
}

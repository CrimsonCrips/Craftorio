package org.crimsoncrips.craftorio.server.events;

import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.crimsoncrips.craftorio.Craft_Misc;
import org.crimsoncrips.craftorio.Craftorio;

import static org.crimsoncrips.craftorio.Craft_Misc.startingLocations;
import static org.crimsoncrips.craftorio.Craftorio.AMOUNT_OF_LAND;
import static org.crimsoncrips.craftorio.Craftorio.OWNED;

public class ServerEvents {

    @SubscribeEvent
    public void serverEvents(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (level == null) return;
        BlockPos blockPos = new BlockPos(0, level.getSharedSpawnPos().getY(), 0);
        level.setDefaultSpawnPos(blockPos, 0);
    }

    @SubscribeEvent
    public void tickEvent(LevelTickEvent.Post tickEvent){
        Level level = tickEvent.getLevel();
        int claimed_amount = level.getData(AMOUNT_OF_LAND);
        for (ChunkPos chunkSelected : Craft_Misc.startingLocations()) {
            ChunkAccess chunk = level.getChunk(chunkSelected.x, chunkSelected.z);
            if (!chunk.hasData(OWNED)){
                chunk.setData(OWNED, Unit.INSTANCE);
                claimed_amount = claimed_amount + 1;
                level.setData(AMOUNT_OF_LAND, claimed_amount);
            }
        }
    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent blockEvent){
        if (blockEvent.getEntity() == null)return;
        Level level = blockEvent.getEntity().level();
        ChunkPos pos = level.getChunkAt(blockEvent.getPos()).getPos();
        
        if (!level.getChunk(pos.x,pos.z).hasData(OWNED)){
            blockEvent.setCanceled(true);
        }

    }

    @SubscribeEvent
    public void itemTooltip(ItemTooltipEvent itemTooltipEvent){
        itemTooltipEvent.getToolTip().add(1,Component.literal("Points : " + Craft_Misc.checkValue(itemTooltipEvent.getItemStack())).withColor(16759552));
    }


}

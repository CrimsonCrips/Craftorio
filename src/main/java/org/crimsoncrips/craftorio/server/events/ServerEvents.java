package org.crimsoncrips.craftorio.server.events;

import com.mojang.datafixers.util.Unit;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.*;

public class ServerEvents {

    @SubscribeEvent
    public void serverEvents(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (level == null) return;
        BlockPos blockPos = new BlockPos(0, level.getSharedSpawnPos().getY(), 0);
        level.setDefaultSpawnPos(blockPos, 0);
        int sizePicked = Craftorio.COMMON_CONFIG.STARTING_LAND_SIZE.getAsInt();

        if (!CraftorioMisc.chunkBased(level) && !level.getData(FINALIZED)){
            level.getWorldBorder().setSize(sizePicked * 10);
            level.getWorldBorder().setCenter(0,0);
            level.setData(AMOUNT_OF_LAND, sizePicked);
        }
        level.setData(FINALIZED,true);
    }

    @SubscribeEvent
    public void tickEvent(PlayerTickEvent.Post serverTickEvent){
        long points = serverTickEvent.getEntity().level().getData(POINTS);
        serverTickEvent.getEntity().level().setData(POINTS,points + 0);
    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent blockEvent){
        if (blockEvent.getEntity() == null)return;
        Level level = blockEvent.getEntity().level();
        if (!CraftorioMisc.chunkBased(level)) return;
        ChunkPos pos = level.getChunkAt(blockEvent.getPos()).getPos();
        
        if (!CraftorioMisc.isOwned(level.getChunk(pos.x,pos.z))){
            blockEvent.setCanceled(true);
        }

    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent blockEvent){
        Level level = blockEvent.getPlayer().level();
        ChunkPos pos = level.getChunkAt(blockEvent.getPos()).getPos();
        if (!CraftorioMisc.chunkBased(level)) return;

        if (!CraftorioMisc.isOwned(level.getChunk(pos.x,pos.z))){
            blockEvent.setCanceled(true);
        }

    }

    @SubscribeEvent
    public void itemTooltip(ItemTooltipEvent itemTooltipEvent){
        itemTooltipEvent.getToolTip().add(1,Component.literal("Points : " + CraftorioMisc.checkValue(itemTooltipEvent.getItemStack())).withColor(16759552));
    }


    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag playerData = event.getEntity().getPersistentData();
        CompoundTag data = playerData.getCompound(Player.PERSISTED_NBT_TAG);

        if (!data.getBoolean("sinker_given")) {
            player.addItem(CraftorioBlocks.SINKER.get().asItem().getDefaultInstance());
            data.putBoolean("sinker_given", true);
            playerData.put(Player.PERSISTED_NBT_TAG, data);
        }
    }


}

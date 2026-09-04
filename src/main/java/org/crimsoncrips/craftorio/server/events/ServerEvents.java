package org.crimsoncrips.craftorio.server.events;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.effects.points.CraftorioPointEffect;
import org.crimsoncrips.craftorio.effects.points.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.effects.points.TagMultiplierEffect;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;

import java.util.List;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.*;

public class ServerEvents {

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (level == null) return;
        BlockPos blockPos = new BlockPos(9, level.getSharedSpawnPos().getY(), 9);
        int sizePicked = Craftorio.SERVER_CONFIG.STARTING_LAND_SIZE.getAsInt();


        if (!level.getData(FINALIZED)){
            level.setData(CHUNK_BASED,Craftorio.SERVER_CONFIG.CHUNK_BASED_EXPANSION.getAsBoolean());
            level.setData(UNIVERSAL_BASED,Craftorio.SERVER_CONFIG.UNIVERSAL_BASED_POINTS.getAsBoolean());
            if (!CraftorioMisc.chunkBased(level)){
                level.getWorldBorder().setSize(sizePicked * 10);
                level.getWorldBorder().setCenter(blockPos.getX(),blockPos.getZ());
                CraftorioMisc.setLandAmount(level,sizePicked);
            }
        }

        level.setData(FINALIZED,true);
    }

    @SubscribeEvent
    public void serverStarting(ServerStartingEvent event) {
        ServerLevel level = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (level == null) return;
        BlockPos blockPos = new BlockPos(9, level.getSharedSpawnPos().getY(), 9);
        level.setDefaultSpawnPos(blockPos, 0);
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
        if (itemTooltipEvent.getEntity() == null)
            return;

        String pointValue = CraftorioMisc.bigIntFormat(CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),false), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        String unmultipliedValue = CraftorioMisc.bigIntFormat(CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),true), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        float multiplierValue = CraftorioMisc.itemMultiplierValue(itemTooltipEvent.getEntity(),itemTooltipEvent.getItemStack());

        String multiplierText = "";
        if (multiplierValue != 0){
            multiplierText = " (" + unmultipliedValue + " * " + multiplierValue + ")";
        }
        itemTooltipEvent.getToolTip().add(1,Component.literal("Points : " + pointValue + multiplierText ).withColor(16759552));
    }


    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (!player.getData(GIVEN)) {
            player.addItem(CraftorioBlocks.SINKER.get().asItem().getDefaultInstance());
            CraftorioMisc.setPoints(event.getEntity().level(), CraftorioMisc.startingValue(), player);

            CraftorioMisc.grantEffect(player, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "copper_boost"));

            player.setData(GIVEN, true);
        }
    }

    @SubscribeEvent
    public void playerTIck(PlayerTickEvent.Post event) {
        Player player = event.getEntity();


        if (!CraftorioMisc.getCraftorioPointEffects(player).isEmpty()){
            for (CraftorioPointEffect effect : ImmutableList.copyOf(CraftorioMisc.getCraftorioPointEffects(player))) {
                if (effect.shouldEnd()) {
                    if (effect instanceof TagMultiplierEffect){
                        List<TagMultiplierEffect> effects = CraftorioMisc.getTagEffects(player);
                        effects.remove(effect);
                        player.setData(TAG_MULTIPLIER_EFFECTS, effects);
                    }
                    if (effect instanceof GeneralMultiplierEffect){
                        List<GeneralMultiplierEffect> effects = CraftorioMisc.getGeneralEffects(player);
                        effects.remove(effect);
                        player.setData(GENERAL_MULTIPLIER_EFFECTS, CraftorioMisc.getGeneralEffects(player));
                    }
                } else {
                    effect.tick();
                }
            }
        }
    }


}

package org.crimsoncrips.craftorio.server.events;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import org.crimsoncrips.craftorio.registries.effect.CraftorioPointEffect;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;


import java.util.List;
import java.util.Set;

import static org.crimsoncrips.craftorio.CraftorioMisc.getPlayerOrigin;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.*;

public class ServerEvents {

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (level == null) return;


        if (!level.getData(FINALIZED)){
            level.setData(CHUNK_BASED,Craftorio.SERVER_CONFIG.CHUNK_BASED_EXPANSION.getAsBoolean());
            level.setData(UNIVERSAL_BASED,Craftorio.SERVER_CONFIG.UNIVERSAL_PROGRESSION.getAsBoolean());
            if (CraftorioMisc.universalBased(level)){
                level.setData(NO_BORDERS,true);
            } else {
                level.setData(NO_BORDERS,Craftorio.SERVER_CONFIG.NO_BORDERS.getAsBoolean());
            }
        }

        level.setData(FINALIZED,true);
    }


    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent blockEvent){
        if (blockEvent.getEntity() instanceof Player player){
            Level level = blockEvent.getEntity().level();
            if (!CraftorioMisc.chunkBased(level)) return;
            ChunkPos pos = level.getChunkAt(blockEvent.getPos()).getPos();

            if (!CraftorioMisc.isOwnedBy(level.getChunk(pos.x,pos.z),player)){
                blockEvent.setCanceled(true);
            }
        }

    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent blockEvent){
        if (blockEvent.getPlayer() instanceof Player player){
            Level level = blockEvent.getPlayer().level();
            ChunkPos pos = level.getChunkAt(blockEvent.getPos()).getPos();
            if (!CraftorioMisc.chunkBased(level)) return;

            if (!CraftorioMisc.isOwnedBy(level.getChunk(pos.x,pos.z),player)){
                blockEvent.setCanceled(true);
            }
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
        Level level = player.level();

        if (!player.getData(GIVEN)) {
            player.addItem(CraftorioBlocks.SINKER.get().asItem().getDefaultInstance());
            CraftorioMisc.setPoints(level, CraftorioMisc.startingValue(), player);
            CraftorioMisc.grantEffect(player, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "general/50_percent_addition"));
            CraftorioMisc.grantEffect(player, ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "tag/copper_block_buff"));

            //Handles player dispersion
            if (player instanceof ServerPlayer serverPlayer) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                BlockPos spawnPos = CraftorioMisc.findDispersedSpawnPos(serverLevel,
                        Craftorio.SERVER_CONFIG.MIN_SPAWN_DISTANCE.get(),
                        Craftorio.SERVER_CONFIG.MAX_SPAWN_DISTANCE.get()
                );

                if (!CraftorioMisc.universalBased(level)){
                   spawnPos = serverPlayer.getRespawnPosition();
                }

                GlobalPos origin = GlobalPos.of(serverLevel.dimension(), spawnPos);
                serverPlayer.setData(CraftorioDataAttachments.SPAWN_ORIGIN.get(), origin);

                serverPlayer.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                serverPlayer.setRespawnPosition(serverLevel.dimension(), spawnPos, 0F, true, false);

                CraftorioMisc.ownChunk(CraftorioMisc.startingLocations(level.getChunk(spawnPos).getPos()),level,true,player,true);
            }



            if (CraftorioMisc.getLandAmount(level,player) <= 0 && CraftorioMisc.isNoBorders(level)){
                CraftorioMisc.setLandAmount(level, CraftorioMisc.startingLand(), player);
            } else if (!CraftorioMisc.isNoBorders(level)){
                CraftorioMisc.setLandAmount(level, CraftorioMisc.startingLand(), player);
            }


            player.setData(CONTRACTS,CraftorioMisc.getCraftorioContracts(level,player));


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
                        List<TagMultiplierEffect> effects = CraftorioMisc.getTagEffects(player.level(), player);
                        effects.remove(effect);
                        player.setData(TAG_MULTIPLIER_EFFECTS, effects);
                    }
                    if (effect instanceof GeneralMultiplierEffect){
                        List<GeneralMultiplierEffect> effects = CraftorioMisc.getGeneralEffects(player.level(), player);
                        effects.remove(effect);
                        player.setData(GENERAL_MULTIPLIER_EFFECTS, CraftorioMisc.getGeneralEffects(player.level(), player));
                    }
                } else {
                    effect.tick();
                }
            }
        }
    }


}

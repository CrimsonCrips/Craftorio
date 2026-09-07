package org.crimsoncrips.craftorio.server.events;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioPointEffect;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementPoints;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;


import java.awt.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.*;

public class ServerEvents {

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
        // CHUNK_BASED/UNIVERSAL_BASED/NO_BORDERS/FINALIZED are level data attachments,
        // which are per-dimension - initialize them on every dimension (not just the
        // overworld) so a level other than the overworld never falls back to the
        // attachment's hardcoded default instead of the configured value.
        for (ServerLevel level : event.getServer().getAllLevels()) {
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
    public void playerDimension(PlayerEvent.PlayerChangedDimensionEvent dimensionEvent){
        Player player = dimensionEvent.getEntity();
        Level level = player.level();

        if (player instanceof ServerPlayer serverPlayer && !CraftorioMisc.getDimensionsExplored(player).contains(dimensionEvent.getTo())) {
            if (CraftorioMisc.chunkBased(level)){
                CraftorioMisc.ownChunk(CraftorioMisc.startingLocations(level.getChunk(serverPlayer.getOnPos()).getPos()),level,true,player,true);
            } else {
                List<CraftorioBorder> newBorder = new ArrayList<>(CraftorioMisc.getCraftorioBorders(player));
                newBorder.add(new CraftorioBorder(serverPlayer.getOnPos(),CraftorioMisc.startingLand(),1,10,1,10, dimensionEvent.getTo()));
                CraftorioMisc.setCraftorioBorders(player,newBorder);
            }

            List<ResourceKey<Level>> newDimensions = new ArrayList<>(CraftorioMisc.getDimensionsExplored(player));
            newDimensions.add(dimensionEvent.getTo());
            CraftorioMisc.setDimensionsExplored(player,newDimensions);


        }


    }


    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (!player.getData(GIVEN)) {
            player.addItem(CraftorioBlocks.SINKER.get().asItem().getDefaultInstance());
            CraftorioMisc.setPoints(CraftorioMisc.startingValue(), player);

            List<CraftorioEffects> craftorioEffectsList = new ArrayList<>();

            for (int i = 0; i < 20;i++){
                craftorioEffectsList.add(CraftorioMisc.getRandomEffect(player.registryAccess(),player.getRandom()));
            }

            CraftorioMisc.giveEffectItem(CraftorioItems.EFFECT_ITEM.get(),(ServerPlayer) player,craftorioEffectsList);
            CraftorioMisc.giveEffectItem(CraftorioItems.MYSTERY_EFFECT_ITEM.get(),(ServerPlayer) player,craftorioEffectsList);



            CraftorioMisc.grantContract(player,ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "starter_contract"));

            if (player instanceof ServerPlayer serverPlayer) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                BlockPos spawnPos = CraftorioMisc.findDispersedSpawnPos(serverLevel,
                        Craftorio.SERVER_CONFIG.MIN_SPAWN_DISTANCE.get(),
                        Craftorio.SERVER_CONFIG.MAX_SPAWN_DISTANCE.get()
                );

                if (CraftorioMisc.universalBased(level)){
                   spawnPos = serverLevel.getSharedSpawnPos();
                }

                List<ResourceKey<Level>> newDimension = new ArrayList<>();
                newDimension.add(serverPlayer.getRespawnDimension());
                CraftorioMisc.setDimensionsExplored(player,newDimension);

                if (CraftorioMisc.chunkBased(level)){
                    CraftorioMisc.ownChunk(CraftorioMisc.startingLocations(level.getChunk(spawnPos).getPos()),level,true,player,true);
                } else {
                    List<CraftorioBorder> newBorder = new ArrayList<>();
                    newBorder.add(new CraftorioBorder(spawnPos,CraftorioMisc.startingLand(),1,10,1,10, serverPlayer.getRespawnDimension()));
                    CraftorioMisc.setCraftorioBorders(player,newBorder);
                }

                GlobalPos origin = GlobalPos.of(serverLevel.dimension(), spawnPos);
                serverPlayer.setData(CraftorioDataAttachments.SPAWN_ORIGIN.get(), origin);

                serverPlayer.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                serverPlayer.setRespawnPosition(serverLevel.dimension(), spawnPos, 0F, true, false);

            }



            if ((CraftorioMisc.getLandAmount(player) <= 0 && CraftorioMisc.isNoBorders(level)) || !CraftorioMisc.isNoBorders(level)){
                CraftorioMisc.setLandAmount(CraftorioMisc.startingLand(), player);
            }

            player.setData(GIVEN, true);
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!CraftorioMisc.getCraftorioPointEffects(player).isEmpty()){
            for (CraftorioPointEffect effect : ImmutableList.copyOf(CraftorioMisc.getCraftorioPointEffects(player))) {
                if (!effect.shouldEnd()) {
                    effect.tick(player);
                }
            }
        }

        if (!CraftorioMisc.getCraftorioContracts(player).isEmpty()){
            for (CraftorioShipmentContract contract : ImmutableList.copyOf(CraftorioMisc.getCraftorioContracts(player))) {
                contract.tick(player);
            }
        }
    }

    @SubscribeEvent
    public void advancementObtained(AdvancementEvent.AdvancementEarnEvent advancementEvent){
        Player player = advancementEvent.getEntity();
        AdvancementHolder advancement = advancementEvent.getAdvancement();

        ResourceLocation id = advancement.id();
        BigInteger value = CraftorioAdvancementPoints.getPoints(id.toString());

        BigInteger pointsOwned = CraftorioMisc.getPoints(player);
        CraftorioMisc.setPoints(pointsOwned.add(value),player);

        String string = Component.translatable("misc.craftorio.advancement_value").getString();

        if (value.compareTo(BigInteger.ZERO) > 0) {
            player.sendSystemMessage(Component.literal(string + value));
        }
    }

    @SubscribeEvent
    public void serverTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            List<CraftorioBorder> borders = CraftorioMisc.getCraftorioBorders(player);

            for (CraftorioBorder border : borders) {
                if (!border.getDimension().equals(player.level().dimension())) {
                    continue;
                }

                double x = player.getX();
                double z = player.getZ();

                if (!border.isWithinBounds(x, z)) {
                    double distanceOutside = -border.getDistanceToBorder(x, z);

                    if (distanceOutside > border.getDamageSafeZone() && player.tickCount % 20 == 0) {
                        int damageMultiplier = (int) Math.max(1, distanceOutside - border.getDamageSafeZone());
                        player.hurt(player.damageSources().outOfBorder(), (float) (damageMultiplier * border.getDamagePerBlock()));
                    }
                }
            }
        }
    }


}

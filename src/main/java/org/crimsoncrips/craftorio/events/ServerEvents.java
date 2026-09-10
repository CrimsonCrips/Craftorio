package org.crimsoncrips.craftorio.events;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.networking.EffectTimerPacket;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementPoints;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.*;

public class ServerEvents {

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
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
    public void itemTooltip(ItemTooltipEvent itemTooltipEvent){
        if (itemTooltipEvent.getEntity() == null)
            return;

        String pointValue = CraftorioMisc.bigIntFormat(CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),true), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        String unmultipliedValue = CraftorioMisc.bigIntFormat(CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),false), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        float multiplierValue = CraftorioMisc.itemMultiplierValue(itemTooltipEvent.getEntity(),itemTooltipEvent.getItemStack());

        String multiplierText = "";
        if (multiplierValue != 0){
            multiplierText = " (" + unmultipliedValue + " * " + multiplierValue + ")";
        }

        String cappedText = CraftorioMisc.bigIntFormat(CraftorioMisc.pointThreshold(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        String negCappedText = "-" + cappedText;

        MutableComponent line = Component.translatable("misc.craftorio.points_label").withColor(16759552);
        if (pointValue.equals(cappedText)) {
            line.append(CraftorioMisc.CraftorioTextEffects.fancyComponent(pointValue, 0));
        } else if (pointValue.equals(negCappedText)) {
            line.append(CraftorioMisc.CraftorioTextEffects.fancyComponent(pointValue, 1));
        } else {
            line.append(Component.literal(pointValue).withColor(16759552));
        }
        line.append(Component.literal(multiplierText).withColor(16759552));

        itemTooltipEvent.getToolTip().add(1, line);
    }

    public void setArea(Player player,BlockPos blockPos,ResourceKey<Level> dimensionLevel){
        Level level = player.level();
        if (CraftorioMisc.chunkBased(level)){
            CraftorioMisc.ownChunks(CraftorioMisc.startingLocations(level.getChunk(blockPos).getPos()),level,true,player,true);
        } else {
            List<CraftorioBorder> newBorder = new ArrayList<>(CraftorioMisc.getCraftorioBorders(player));
            newBorder.removeIf(border -> border.getDimension().equals(dimensionLevel));
            newBorder.add(new CraftorioBorder(blockPos,CraftorioMisc.startingLand() * 5,1,10,1,10, dimensionLevel));
            CraftorioMisc.setCraftorioBorders(player,newBorder);
        }
    }

    @SubscribeEvent
    public void playerDimension(PlayerEvent.PlayerChangedDimensionEvent dimensionEvent){
        Player player = dimensionEvent.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (!CraftorioMisc.getDimensionsExplored(player).contains(dimensionEvent.getTo())) {
                setArea(player,serverPlayer.getOnPos(),dimensionEvent.getTo());

                List<ResourceKey<Level>> newDimensions = new ArrayList<>(CraftorioMisc.getDimensionsExplored(player));
                newDimensions.add(dimensionEvent.getTo());
                CraftorioMisc.setDimensionsExplored(player,newDimensions);
            }


            try {
                AttachmentSync.syncInitialLevelAttachments(serverPlayer.serverLevel(), serverPlayer);
            } catch (Exception e) {
                Craftorio.LOGGER.error("Failed to sync level attachments to {} on dimension change", serverPlayer.getGameProfile().getName(), e);
            }
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

            for (int i = 0; i < 2;i++){
                craftorioEffectsList.add(CraftorioMisc.getRandomEffect(player.registryAccess(),player.getRandom()));
            }

            CraftorioMisc.giveEffectItem(CraftorioItems.EFFECT_ITEM.get(),(ServerPlayer) player,craftorioEffectsList);
            CraftorioMisc.giveEffectItem(CraftorioItems.MYSTERY_EFFECT_ITEM.get(),(ServerPlayer) player,craftorioEffectsList);




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

                setArea(player,spawnPos,serverPlayer.getRespawnDimension());


                GlobalPos origin = GlobalPos.of(serverLevel.dimension(), spawnPos);
                serverPlayer.setData(CraftorioDataAttachments.SPAWN_ORIGIN.get(), origin);

                serverPlayer.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                serverPlayer.setRespawnPosition(serverLevel.dimension(), spawnPos, 0F, true, false);

            }



            if ((CraftorioMisc.getLandAmount(player) <= 0 && CraftorioMisc.isNoBorders(level)) || !CraftorioMisc.isNoBorders(level)){
                CraftorioMisc.setLandAmount(CraftorioMisc.startingLand(), player);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new org.crimsoncrips.craftorio.networking.WelcomeToastPacket());
            }

            player.setData(GIVEN, true);
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!CraftorioMisc.universalBased(player.level())){
            if (!CraftorioMisc.getCraftorioEffects(player).isEmpty()) {
                for (CraftorioEffects effect : ImmutableList.copyOf(CraftorioMisc.getCraftorioEffects(player))) {
                    if (!effect.shouldEnd()) {
                        effect.tick(player);
                    }
                }
            }

            if (!CraftorioMisc.getCraftorioContracts(player).isEmpty()) {
                for (CraftorioShipmentContract contract : ImmutableList.copyOf(CraftorioMisc.getCraftorioContracts(player))) {
                    contract.tick(player);
                }
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
                        if (Craftorio.SERVER_CONFIG.INSTANT_DEATH_OUTSIDE_CLAIM.getAsBoolean()) {
                            killOutsideClaim(player);
                        } else {
                            int damageMultiplier = (int) Math.max(1, distanceOutside - border.getDamageSafeZone());
                            player.hurt(player.damageSources().outOfBorder(), (float) (damageMultiplier * border.getDamagePerBlock()));
                        }
                    }
                }
            }

            Level level = player.level();
            if (CraftorioMisc.chunkBased(level) && player.tickCount % 20 == 0
                    && !ChunkCollisionHooks.isWithinClaimedChunk(player, player.blockPosition())) {
                if (Craftorio.SERVER_CONFIG.INSTANT_DEATH_OUTSIDE_CLAIM.getAsBoolean()) {
                    killOutsideClaim(player);
                } else {
                    player.hurt(player.damageSources().outOfBorder(), (float) Craftorio.SERVER_CONFIG.CHUNK_OUT_OF_BOUNDS_DAMAGE.getAsDouble());
                }
            }
        }
    }

    private void killOutsideClaim(ServerPlayer player) {
        player.serverLevel().explode(player, player.getX(), player.getY(), player.getZ(), 3.0F, Level.ExplosionInteraction.NONE);
        player.hurt(player.damageSources().outOfBorder(), Float.MAX_VALUE);
    }

    private static final Set<UUID> effectTimerViewers = new HashSet<>();

    public static boolean toggleEffectTimerViewer(ServerPlayer player) {
        UUID id = player.getUUID();
        if (effectTimerViewers.remove(id)) {
            return false;
        }
        effectTimerViewers.add(id);
        return true;
    }

    @SubscribeEvent
    public void randomEffectTick(ServerTickEvent.Post event) {
        if (!Craftorio.SERVER_CONFIG.RANDOM_EFFECTS_ENABLED.getAsBoolean()) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            int timeUntilNextEffect = CraftorioMisc.getRandomEffectTime(level) - 1;

            if (!effectTimerViewers.isEmpty() && level.getGameTime() % 20 == 0) {
                for (ServerPlayer player : level.players()) {
                    if (effectTimerViewers.contains(player.getUUID())) {
                        PacketDistributor.sendToPlayer(player, new EffectTimerPacket(true, Math.max(timeUntilNextEffect, 0)));
                    }
                }
            }

            if (timeUntilNextEffect > 0) {
                CraftorioMisc.setRandomEffectTime(level, timeUntilNextEffect);
                continue;
            }

            List<ServerPlayer> players = level.players();
            if (!players.isEmpty()) {
                CraftorioEffects rolledEffect = CraftorioMisc.getRandomEffect(level.registryAccess(), level.random);
                for (ServerPlayer player : players) {
                    CraftorioMisc.grantEffect(player, rolledEffect.copy());
                }
            }

            int minInterval = Craftorio.SERVER_CONFIG.RANDOM_EFFECT_MIN_INTERVAL.get();
            int maxInterval = Craftorio.SERVER_CONFIG.RANDOM_EFFECT_MAX_INTERVAL.get();
            int nextInterval = minInterval + level.random.nextInt(Math.max(1, maxInterval - minInterval + 1));
            CraftorioMisc.setRandomEffectTime(level, nextInterval);
        }
    }

    @SubscribeEvent
    public void contractOfferTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            int timeUntilRefresh = CraftorioMisc.getContractRefreshTime(level) - 1;

            if (timeUntilRefresh > 0) {
                CraftorioMisc.setContractRefreshTime(level, timeUntilRefresh);
                continue;
            }

            List<ServerPlayer> players = level.players();

            if (CraftorioMisc.universalBased(level)) {
                List<ResourceLocation> offer = CraftorioMisc.rollContractOffer(level.registryAccess(), level.random);
                level.setData(CraftorioDataAttachments.CONTRACT_OFFER, offer);
                level.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);

                for (ServerPlayer player : players) {
                    notifyNewContracts(player, offer);
                }
            } else {
                for (ServerPlayer player : players) {
                    List<ResourceLocation> offer = CraftorioMisc.rollContractOffer(player.registryAccess(), player.getRandom());
                    player.setData(CraftorioDataAttachments.CONTRACT_OFFER, offer);
                    player.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);

                    notifyNewContracts(player, offer);
                }
            }

            int refreshTicks = Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS;
            CraftorioMisc.setContractRefreshTime(level, refreshTicks);
        }
    }

    private void notifyNewContracts(ServerPlayer player, List<ResourceLocation> offer) {
        if (offer.isEmpty()) return;
        PacketDistributor.sendToPlayer(player, new org.crimsoncrips.craftorio.networking.ContractOfferStatusPacket(true));
    }

}

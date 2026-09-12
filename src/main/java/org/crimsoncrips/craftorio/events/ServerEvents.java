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
import org.crimsoncrips.craftorio.networking.EffectTimerPacket;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.server.ChunkCollisionHooks;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementPoints;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementMultipliers;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.CraftorioPointsAdvancements;
import org.crimsoncrips.craftorio.server.CraftorioShop;
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
                if (CraftorioMisc.universalBased(level) || !CraftorioMisc.chunkBased(level)){
                    level.setData(NO_BORDERS,true);
                } else {
                    level.setData(NO_BORDERS,Craftorio.SERVER_CONFIG.NO_BORDERS.getAsBoolean());
                }

                CraftorioMisc.setContractRefreshTime(level, Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS);
                CraftorioMisc.setRandomEffectTime(level, Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get());
            }

            level.setData(FINALIZED,true);
        }
    }


    @SubscribeEvent
    public void itemTooltip(ItemTooltipEvent itemTooltipEvent){
        if (itemTooltipEvent.getEntity() == null)
            return;

        String pointValue = CraftorioMisc.bigIntFormat(CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),true), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        BigInteger unmultipliedBigInt = CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),false);
        String unmultipliedValue = CraftorioMisc.bigIntFormat(unmultipliedBigInt, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
        float multiplierValue = CraftorioMisc.overallMultiplierValue(itemTooltipEvent.getEntity(),itemTooltipEvent.getItemStack(), unmultipliedBigInt);

        String multiplierText = "";
        if (multiplierValue != 0){
            multiplierText = " (" + unmultipliedValue + " * " + (multiplierValue + 1) + "x)";
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
            List<CraftorioBorder> existingBorders = CraftorioMisc.getCraftorioBorders(player);
            boolean alreadyHasBorder = existingBorders.stream().anyMatch(border -> border.getDimension().equals(dimensionLevel));
            if (CraftorioMisc.universalBased(level) && alreadyHasBorder) {
                return;
            }

            List<CraftorioBorder> newBorder = new ArrayList<>(existingBorders);
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

            syncUniversalState(serverPlayer);
        }
    }


    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (!player.getData(GIVEN)) {
            player.addItem(CraftorioBlocks.SINKER.get().asItem().getDefaultInstance());

            Level universalLevel = CraftorioMisc.universalLevel(player);
            boolean sharedProgressAlreadyStarted = CraftorioMisc.universalBased(level) && universalLevel.getData(UNIVERSAL_PROGRESS_STARTED);
            if (!sharedProgressAlreadyStarted) {
                CraftorioMisc.setPoints(CraftorioMisc.startingValue(), player);
                if (CraftorioMisc.universalBased(level)) {
                    universalLevel.setData(UNIVERSAL_PROGRESS_STARTED, true);
                }
            }

            CraftorioMisc.setContractRefreshTime(player, Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS);
            CraftorioMisc.setRandomEffectTime(player, Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get());

            List<CraftorioEffects> craftorioEffectsList = new ArrayList<>();

            for (int i = 0; i < 2;i++){
                craftorioEffectsList.add(CraftorioMisc.getRandomEffect(player.registryAccess(),player.getRandom()));
            }



            if (player instanceof ServerPlayer serverPlayer) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                BlockPos spawnPos = CraftorioMisc.findDispersedSpawnPos(serverLevel,
                        Craftorio.SERVER_CONFIG.MIN_SPAWN_DISTANCE.get(),
                        Craftorio.SERVER_CONFIG.MAX_SPAWN_DISTANCE.get()
                );

                if (CraftorioMisc.universalBased(level)){
                   spawnPos = serverLevel.getSharedSpawnPos();
                }

                List<ResourceKey<Level>> existingDimensions = CraftorioMisc.getDimensionsExplored(player);
                if (!existingDimensions.contains(serverPlayer.getRespawnDimension())) {
                    List<ResourceKey<Level>> newDimensions = new ArrayList<>(existingDimensions);
                    newDimensions.add(serverPlayer.getRespawnDimension());
                    CraftorioMisc.setDimensionsExplored(player,newDimensions);
                }

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
                PacketDistributor.sendToPlayer(serverPlayer, new org.crimsoncrips.craftorio.networking.ShopStatusPacket(CraftorioShop.isEnabled()));
                CraftorioPointsAdvancements.checkAndGrant(serverPlayer, CraftorioMisc.getPoints(player));
            }

            player.setData(GIVEN, true);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            var registry = player.level().registryAccess().registryOrThrow(org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade.REGISTRY_KEY);
            for (ResourceLocation upgradeId : CraftorioMisc.getUnlockedUpgrades(player)) {
                var upgrade = registry.get(upgradeId);
                if (upgrade != null) {
                    upgrade.onUnlock(serverPlayer, upgradeId);
                }
            }

            syncUniversalState(serverPlayer);

            PacketDistributor.sendToPlayer(serverPlayer, new org.crimsoncrips.craftorio.networking.UnlockedItemsSyncPacket(
                    new ArrayList<>(Craftorio.UNLOCKED_ITEMS.getUnlocked(serverPlayer))));
        }
    }

    private void syncUniversalState(ServerPlayer player) {
        if (!CraftorioMisc.universalBased(CraftorioMisc.universalLevel(player))) return;

        PacketDistributor.sendToPlayer(player, new org.crimsoncrips.craftorio.networking.UniversalStateSyncPacket(
                CraftorioMisc.getPoints(player),
                CraftorioMisc.getHighestPoints(player),
                CraftorioMisc.getTempPoints(player),
                CraftorioMisc.getLandAmount(player),
                CraftorioMisc.getUnlockedUpgrades(player),
                CraftorioMisc.getGeneralEffects(player),
                CraftorioMisc.getTagEffects(player),
                CraftorioMisc.getShopEffects(player),
                CraftorioMisc.getAdvancementMultiplierBonus(player),
                CraftorioMisc.getCraftorioContracts(player),
                CraftorioMisc.getCraftorioBorders(player)
        ));
    }

    @SubscribeEvent
    public void universalStateSyncTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        if (!CraftorioMisc.universalBased(overworld)) return;
        if (overworld.getGameTime() % 20 != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            syncUniversalState(player);
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!CraftorioMisc.universalBased(player.level())){
            tickEffectsAndContracts(player);
        }
    }

    @SubscribeEvent
    public void universalProgressTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!CraftorioMisc.universalBased(level)) continue;

            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) continue;

            tickEffectsAndContracts(players.get(0));
        }
    }

    private void tickEffectsAndContracts(Player player) {
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

    @SubscribeEvent
    public void xpChange(net.neoforged.neoforge.event.entity.player.PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        double additive = CraftorioMisc.getXpGainModifierSum(player, org.crimsoncrips.craftorio.skill_tree.UpgradeOperation.ADD);
        double multiplicative = CraftorioMisc.getXpGainModifierSum(player, org.crimsoncrips.craftorio.skill_tree.UpgradeOperation.MULTIPLY);
        if (additive == 0 && multiplicative == 0) return;

        double result = (event.getAmount() + additive) * (1.0 + multiplicative);
        event.setAmount((int) Math.max(0, Math.round(result)));
    }

    @SubscribeEvent
    public void advancementObtained(AdvancementEvent.AdvancementEarnEvent advancementEvent){
        Player player = advancementEvent.getEntity();
        AdvancementHolder advancement = advancementEvent.getAdvancement();

        ResourceLocation id = advancement.id();
        BigInteger value = CraftorioAdvancementPoints.getPoints(id.toString());

        BigInteger pointsOwned = CraftorioMisc.getPoints(player);
        CraftorioMisc.setPoints(pointsOwned.add(value),player);

        if (CraftorioMisc.hasUnlockedUpgrade(player, Craftorio.prefix("advancement_multiplier"))) {
            double multiplierBonus = CraftorioAdvancementMultipliers.getMultiplier(id.toString());
            CraftorioMisc.addAdvancementMultiplierBonus(player, multiplierBonus);
        }

        String string = Component.translatable("misc.craftorio.advancement_value").getString();

        if (value.compareTo(BigInteger.ZERO) > 0) {
            player.sendSystemMessage(Component.literal(string + value));
        }

        if (CraftorioMisc.universalBased(player.level()) && player instanceof ServerPlayer earner) {
            for (ServerPlayer other : earner.getServer().getPlayerList().getPlayers()) {
                if (other == earner) continue;
                for (String criterion : advancement.value().criteria().keySet()) {
                    other.getAdvancements().award(advancement, criterion);
                }
            }
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

    private static final Set<UUID> pendingContractScreenPush = new HashSet<>();

    public static void requestInstantContractRefresh(ServerPlayer player) {
        pendingContractScreenPush.add(player.getUUID());
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

        ServerLevel overworld = event.getServer().overworld();
        if (CraftorioMisc.universalBased(overworld)) {
            tickUniversalRandomEffect(event.getServer(), overworld);
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (CraftorioMisc.universalBased(level)) continue;

            for (ServerPlayer player : level.players()) {
                int timeUntilNextEffect = CraftorioMisc.getRandomEffectTime(player) - 1;

                if (level.getGameTime() % 20 == 0 && effectTimerViewers.contains(player.getUUID())) {
                    PacketDistributor.sendToPlayer(player, new EffectTimerPacket(true, Math.max(timeUntilNextEffect, 0)));
                }

                if (timeUntilNextEffect > 0) {
                    CraftorioMisc.setRandomEffectTime(player, timeUntilNextEffect);
                    continue;
                }

                double betterEffectChance = CraftorioMisc.getUpgradeModifierSum(player, org.crimsoncrips.craftorio.skill_tree.ModifierTarget.BETTER_EFFECT_CHANCE, org.crimsoncrips.craftorio.skill_tree.UpgradeOperation.ADD);
                CraftorioEffects rolledEffect = CraftorioMisc.getRandomAmbientEffect(level.registryAccess(), player.getRandom(), betterEffectChance);
                CraftorioMisc.grantEffect(player, rolledEffect.copy());

                int effectInterval = CraftorioMisc.applySpeedUpgrade(player, org.crimsoncrips.craftorio.skill_tree.ModifierTarget.EFFECT_TIMER_SPEED, Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get());
                CraftorioMisc.setRandomEffectTime(player, effectInterval);
            }
        }
    }

    private void tickUniversalRandomEffect(net.minecraft.server.MinecraftServer server, ServerLevel overworld) {
        int timeUntilNextEffect = CraftorioMisc.getRandomEffectTime(overworld) - 1;
        List<ServerPlayer> allPlayers = server.getPlayerList().getPlayers();

        if (!effectTimerViewers.isEmpty() && overworld.getGameTime() % 20 == 0) {
            for (ServerPlayer player : allPlayers) {
                if (effectTimerViewers.contains(player.getUUID())) {
                    PacketDistributor.sendToPlayer(player, new EffectTimerPacket(true, Math.max(timeUntilNextEffect, 0)));
                }
            }
        }

        if (timeUntilNextEffect > 0) {
            CraftorioMisc.setRandomEffectTime(overworld, timeUntilNextEffect);
            return;
        }

        if (!allPlayers.isEmpty()) {
            double betterEffectChance = CraftorioMisc.getUpgradeModifierSum(allPlayers.get(0), org.crimsoncrips.craftorio.skill_tree.ModifierTarget.BETTER_EFFECT_CHANCE, org.crimsoncrips.craftorio.skill_tree.UpgradeOperation.ADD);
            CraftorioEffects rolledEffect = CraftorioMisc.getRandomAmbientEffect(overworld.registryAccess(), overworld.random, betterEffectChance);
            CraftorioMisc.grantEffect(allPlayers.get(0), rolledEffect.copy());
        }

        int effectInterval = allPlayers.isEmpty() ? Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get()
                : CraftorioMisc.applySpeedUpgrade(allPlayers.get(0), org.crimsoncrips.craftorio.skill_tree.ModifierTarget.EFFECT_TIMER_SPEED, Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get());
        CraftorioMisc.setRandomEffectTime(overworld, effectInterval);
    }

    @SubscribeEvent
    public void contractOfferTick(ServerTickEvent.Post event) {
        int baseRefreshTicks = Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS;

        ServerLevel overworld = event.getServer().overworld();
        if (CraftorioMisc.universalBased(overworld)) {
            tickUniversalContractOffer(event.getServer(), overworld, baseRefreshTicks);
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (CraftorioMisc.universalBased(level)) continue;

            for (ServerPlayer player : level.players()) {
                int timeUntilRefresh = CraftorioMisc.getContractRefreshTime(player) - 1;

                if (timeUntilRefresh > 0) {
                    CraftorioMisc.setContractRefreshTime(player, timeUntilRefresh);
                    continue;
                }

                double betterContractChance = CraftorioMisc.getUpgradeModifierSum(player, org.crimsoncrips.craftorio.skill_tree.ModifierTarget.BETTER_CONTRACT_CHANCE, org.crimsoncrips.craftorio.skill_tree.UpgradeOperation.ADD);
                List<ResourceLocation> offer = CraftorioMisc.rollContractOffer(player.registryAccess(), player.getRandom(), CraftorioMisc.getHighestPoints(player), betterContractChance);
                player.setData(CraftorioDataAttachments.CONTRACT_OFFER, offer);
                player.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);

                int refreshTicks = CraftorioMisc.applySpeedUpgrade(player, org.crimsoncrips.craftorio.skill_tree.ModifierTarget.CONTRACT_REFRESH_SPEED, baseRefreshTicks);
                CraftorioMisc.setContractRefreshTime(player, refreshTicks);

                notifyNewContracts(player, offer, refreshTicks);
            }
        }
    }

    private void tickUniversalContractOffer(net.minecraft.server.MinecraftServer server, ServerLevel overworld, int baseRefreshTicks) {
        int timeUntilRefresh = CraftorioMisc.getContractRefreshTime(overworld) - 1;

        if (timeUntilRefresh > 0) {
            CraftorioMisc.setContractRefreshTime(overworld, timeUntilRefresh);
            return;
        }

        List<ServerPlayer> allPlayers = server.getPlayerList().getPlayers();

        BigInteger highestPoints = allPlayers.isEmpty() ? BigInteger.ZERO
                : CraftorioMisc.getHighestPoints(allPlayers.get(0));
        double betterContractChance = allPlayers.isEmpty() ? 0
                : CraftorioMisc.getUpgradeModifierSum(allPlayers.get(0), org.crimsoncrips.craftorio.skill_tree.ModifierTarget.BETTER_CONTRACT_CHANCE, org.crimsoncrips.craftorio.skill_tree.UpgradeOperation.ADD);
        List<ResourceLocation> offer = CraftorioMisc.rollContractOffer(overworld.registryAccess(), overworld.random, highestPoints, betterContractChance);
        overworld.setData(CraftorioDataAttachments.CONTRACT_OFFER, offer);
        overworld.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);

        int refreshTicks = allPlayers.isEmpty() ? baseRefreshTicks
                : CraftorioMisc.applySpeedUpgrade(allPlayers.get(0), org.crimsoncrips.craftorio.skill_tree.ModifierTarget.CONTRACT_REFRESH_SPEED, baseRefreshTicks);
        CraftorioMisc.setContractRefreshTime(overworld, refreshTicks);

        for (ServerPlayer player : allPlayers) {
            notifyNewContracts(player, offer, refreshTicks);
        }
    }

    private void notifyNewContracts(ServerPlayer player, List<ResourceLocation> offer, int refreshTicks) {
        if (pendingContractScreenPush.remove(player.getUUID())) {
            PacketDistributor.sendToPlayer(player, new org.crimsoncrips.craftorio.networking.OpenContractOfferScreenPacket(offer, refreshTicks));
            return;
        }

        if (offer.isEmpty()) return;
        PacketDistributor.sendToPlayer(player, new org.crimsoncrips.craftorio.networking.ContractOfferStatusPacket(true));
    }

}

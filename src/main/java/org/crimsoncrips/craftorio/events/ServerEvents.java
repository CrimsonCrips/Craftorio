package org.crimsoncrips.craftorio.events;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.networking.contract.ContractOfferStatusPacket;
import org.crimsoncrips.craftorio.networking.contract.OpenContractOfferScreenPacket;
import org.crimsoncrips.craftorio.networking.effect.EffectTimerPacket;
import org.crimsoncrips.craftorio.networking.shop.ShopStatusPacket;
import org.crimsoncrips.craftorio.networking.sync.UniversalStateSyncPacket;
import org.crimsoncrips.craftorio.networking.sync.WelcomeToastPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.server.advancement.CraftorioAdvancementMultipliers;
import org.crimsoncrips.craftorio.server.advancement.CraftorioAdvancementPoints;
import org.crimsoncrips.craftorio.server.advancement.CraftorioPointsAdvancements;
import org.crimsoncrips.craftorio.server.border.ChunkCollisionHooks;
import org.crimsoncrips.craftorio.server.border.CraftorioBorder;
import org.crimsoncrips.craftorio.server.config.CraftorioWorldCreationOverrides;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.devtools.CraftorioContractDraftStore;
import org.crimsoncrips.craftorio.server.haven.CraftorioHavenDimension;
import org.crimsoncrips.craftorio.server.rebirth.CraftorioRebirthConsent;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioWipeAreas;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioWorldWipe;
import org.crimsoncrips.craftorio.server.shop.CraftorioShop;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.PlayerActionTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioActionEffectUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments.*;

public class ServerEvents {

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
        CraftorioWorldWipe.onServerStarted(event.getServer());
        CraftorioWorldCreationOverrides.Pending overrides = CraftorioWorldCreationOverrides.consume();

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.getData(FINALIZED)){
                boolean chunkBasedExpansion = overrides != null ? overrides.chunkBasedExpansion() : Craftorio.SERVER_CONFIG.CHUNK_BASED_EXPANSION.getAsBoolean();
                boolean universalProgression = overrides != null ? overrides.universalProgression() : Craftorio.SERVER_CONFIG.UNIVERSAL_PROGRESSION.getAsBoolean();
                level.setData(CHUNK_BASED,chunkBasedExpansion);
                level.setData(UNIVERSAL_BASED,universalProgression);
                if (CraftorioMisc.universalBased(level) || !CraftorioMisc.chunkBased(level)){
                    level.setData(NO_BORDERS,true);
                } else {
                    boolean noBorders = overrides != null ? overrides.noBorders() : Craftorio.SERVER_CONFIG.NO_BORDERS.getAsBoolean();
                    level.setData(NO_BORDERS,noBorders);
                }

                CraftorioMisc.setContractRefreshTime(level, Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS);
                CraftorioMisc.setRandomEffectTime(level, Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get() * CraftorioMisc.SECONDS_TO_TICKS);
            }

            level.setData(FINALIZED,true);
        }
    }

    @SubscribeEvent
    public void serverAboutToStart(ServerAboutToStartEvent event) {
        CraftorioWorldWipe.onServerAboutToStart(event.getServer());
    }

    @SubscribeEvent
    public void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CraftorioSacrifice.onRespawn(serverPlayer);
            CraftorioSacrifice.restoreStranded(serverPlayer);
        }
    }

    @SubscribeEvent
    public void chunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && CraftorioMisc.chunkBased(serverLevel)) {
            CraftorioWipeAreas.indexLoadedChunk(serverLevel, event.getChunk());
        }
    }

    @SubscribeEvent
    public void sacrificeTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer && CraftorioSacrifice.blocksTravel(event.getDimension())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void serverStopped(ServerStoppedEvent event) {
        CraftorioWorldWipe.onServerStopped();
    }

    @SubscribeEvent
    public void sacrificeTick(ServerTickEvent.Post event) {
        CraftorioSacrifice.tick(event.getServer());
    }

    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
        CraftorioSacrifice.onServerStopping(event.getServer());
        CraftorioContractDraftStore.clearAll();
    }

    @SubscribeEvent
    public void onHavenLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)) return;

        CraftorioHavenDimension.placePlatformIfNeeded(serverLevel);
    }

    @SubscribeEvent
    public void onHavenBlockBreak(BlockEvent.BreakEvent event) {
        if (!isHavenLevel(event.getLevel())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHavenBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!isHavenLevel(event.getLevel())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHavenFallDamage(LivingFallEvent event) {
        if (!isHavenLevel(event.getEntity().level())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHavenVoidDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return;
        if (!isHavenLevel(event.getEntity().level())) return;
        event.setCanceled(true);
    }

    private static boolean isHavenLevel(LevelAccessor levelAccessor) {
        return levelAccessor instanceof Level level && level.dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    @SubscribeEvent
    public void havenFallOffTick(ServerTickEvent.Post event) {
        ServerLevel havenLevel = event.getServer().getLevel(CraftorioDimensions.HAVEN_LEVEL_KEY);
        if (havenLevel == null) return;

        for (ServerPlayer player : havenLevel.players()) {
            if (player.getY() < CraftorioHavenDimension.FALL_TELEPORT_THRESHOLD) {
                CraftorioHavenDimension.teleportBackToPlatform(player);
            }
        }
    }


    @SubscribeEvent
    public void itemTooltip(ItemTooltipEvent itemTooltipEvent){
        if (itemTooltipEvent.getEntity() == null)
            return;

        String pointValue = CraftorioMisc.bigIntFormat(CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),true));
        BigInteger unmultipliedBigInt = CraftorioMisc.checkValue(itemTooltipEvent.getItemStack(), itemTooltipEvent.getEntity(),false);
        String unmultipliedValue = CraftorioMisc.bigIntFormat(unmultipliedBigInt);
        float multiplierValue = CraftorioMisc.overallMultiplierValue(itemTooltipEvent.getEntity(),itemTooltipEvent.getItemStack(), unmultipliedBigInt);

        String multiplierText = "";
        if (multiplierValue != 0){
            multiplierText = " (" + unmultipliedValue + " * " + (multiplierValue + 1) + "x)";
        }

        String cappedText = CraftorioMisc.bigIntFormat(CraftorioMisc.pointThreshold());
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

    public static void setArea(Player player,BlockPos blockPos,ResourceKey<Level> dimensionLevel){
        Level level = player.level();
        if (player.getServer() != null && player.getServer().getLevel(dimensionLevel) != null) {
            level = player.getServer().getLevel(dimensionLevel);
        }
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
            CraftorioSacrifice.restoreStranded(serverPlayer);

            if (!dimensionEvent.getTo().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)
                    && !CraftorioMisc.getDimensionsExplored(player).contains(dimensionEvent.getTo())) {
                pendingDimensionAreaSetup.put(player.getUUID(), dimensionEvent.getTo());
            }


            try {
                AttachmentSync.syncInitialLevelAttachments(serverPlayer.serverLevel(), serverPlayer);
            } catch (Exception e) {
                Craftorio.LOGGER.error("Failed to sync level attachments to {} on dimension change", serverPlayer.getGameProfile().getName(), e);
            }

            syncUniversalState(serverPlayer);
        }
    }


    public static void giveFreshStart(Player player) {
        Level level = player.level();

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
        CraftorioMisc.setRandomEffectTime(player, Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get() * CraftorioMisc.SECONDS_TO_TICKS);

        List<CraftorioEffects> craftorioEffectsList = new ArrayList<>();

        for (int i = 0; i < 2;i++){
            craftorioEffectsList.add(CraftorioMisc.getRandomEffect(player.registryAccess(),player.getRandom()));
        }



        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = CraftorioMisc.isInHavenDimension(serverPlayer.level()) ? serverPlayer.server.overworld() : (ServerLevel) serverPlayer.level();
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

            serverPlayer.teleportTo(serverLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, serverPlayer.getYRot(), serverPlayer.getXRot());
            serverPlayer.setRespawnPosition(serverLevel.dimension(), spawnPos, 0F, true, false);

        }



        if ((CraftorioMisc.getLandAmount(player) <= 0 && CraftorioMisc.isNoBorders(level)) || !CraftorioMisc.isNoBorders(level)){
            CraftorioMisc.setLandAmount(CraftorioMisc.startingLand(), player);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new WelcomeToastPacket());
            PacketDistributor.sendToPlayer(serverPlayer, new ShopStatusPacket(CraftorioShop.isEnabled()));
            CraftorioPointsAdvancements.checkAndGrant(serverPlayer, CraftorioMisc.getPoints(player));
        }

        player.setData(GIVEN, true);
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer loggingIn) {
            CraftorioSacrifice.onLogin(loggingIn);
        }

        Player player = event.getEntity();
        Level level = player.level();

        if (!player.getData(GIVEN) && !CraftorioSacrifice.isRunning()) {
            giveFreshStart(player);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            var registry = player.level().registryAccess().registryOrThrow(CraftorioUpgrade.REGISTRY_KEY);
            for (Map.Entry<ResourceLocation, Integer> entry : CraftorioMisc.getUpgradePurchaseCounts(player).entrySet()) {
                var upgrade = registry.get(entry.getKey());
                if (upgrade instanceof CraftorioAttributeUpgrade) {
                    upgrade.onUnlock(serverPlayer, entry.getKey(), entry.getValue());
                }
            }

            var rebirthRegistry = player.level().registryAccess().registryOrThrow(CraftorioUpgrade.REBIRTH_REGISTRY_KEY);
            for (Map.Entry<ResourceLocation, Integer> entry : CraftorioMisc.getRebirthUpgradePurchaseCounts(player).entrySet()) {
                var upgrade = rebirthRegistry.get(entry.getKey());
                if (upgrade instanceof CraftorioAttributeUpgrade) {
                    upgrade.onUnlock(serverPlayer, entry.getKey(), entry.getValue());
                }
            }

            var sacrificeRegistry = player.level().registryAccess().registryOrThrow(CraftorioUpgrade.SACRIFICE_REGISTRY_KEY);
            for (Map.Entry<ResourceLocation, Integer> entry : CraftorioMisc.getSacrificeUpgradePurchaseCounts(player).entrySet()) {
                var upgrade = sacrificeRegistry.get(entry.getKey());
                if (upgrade instanceof CraftorioAttributeUpgrade) {
                    upgrade.onUnlock(serverPlayer, entry.getKey(), entry.getValue());
                }
            }

            CraftorioRebirthConsent.onRosterChanged(serverPlayer.getServer());
            CraftorioSacrifice.onRosterChanged(serverPlayer.getServer());

            syncUniversalState(serverPlayer);
        }
    }

    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
            CraftorioSacrifice.onLogout(serverPlayer);
            CraftorioRebirthConsent.onRosterChanged(serverPlayer.getServer());
            CraftorioSacrifice.onRosterChanged(serverPlayer.getServer());
        }
    }

    public static void syncUniversalState(ServerPlayer player) {
        if (!CraftorioMisc.universalBased(CraftorioMisc.universalLevel(player))) return;

        PacketDistributor.sendToPlayer(player, new UniversalStateSyncPacket(
                CraftorioMisc.getPoints(player),
                CraftorioMisc.getHighestPoints(player),
                CraftorioMisc.getTempPoints(player),
                CraftorioMisc.getLandAmount(player),
                CraftorioMisc.getUpgradePurchaseCounts(player),
                CraftorioMisc.getGeneralEffects(player),
                CraftorioMisc.getTagEffects(player),
                CraftorioMisc.getShopEffects(player),
                CraftorioMisc.getAdvancementMultiplierBonus(player),
                CraftorioMisc.getCraftorioContracts(player),
                CraftorioMisc.getCraftorioBorders(player),
                CraftorioMisc.getContractsCompleted(player),
                CraftorioMisc.getHighestMultiplier(player),
                CraftorioMisc.getLife(player),
                CraftorioMisc.getLifePoints(player),
                CraftorioMisc.getRebirthUpgradePurchaseCounts(player),
                CraftorioMisc.getSacrificeUpgradePurchaseCounts(player),
                CraftorioMisc.getSacrificePoints(player),
                CraftorioMisc.getOverallHighestPoints(player),
                CraftorioMisc.getOverallContractsCompleted(player),
                CraftorioMisc.getOverallItemsSinked(player)
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
            for (CraftorioContract contract : ImmutableList.copyOf(CraftorioMisc.getCraftorioContracts(player))) {
                contract.tick(player);
            }
        }
    }

    @SubscribeEvent
    public void xpChange(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        double additive = CraftorioMisc.getXpGainModifierSum(player, UpgradeOperation.ADD);
        double multiplicative = CraftorioMisc.getXpGainModifierSum(player, UpgradeOperation.MULTIPLY);
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

        if (value.signum() != 0) {
            BigInteger pointsOwned = CraftorioMisc.getPoints(player);
            CraftorioMisc.setPoints(pointsOwned.add(value),player);
        }

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
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!event.updateLevel()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            grantActionEffects(player, PlayerActionTarget.WAKE_UP);
        }
    }

    @SubscribeEvent
    public void onVillagerTrade(TradeWithVillagerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            grantActionEffects(player, PlayerActionTarget.TRADE);
        }
    }

    private void grantActionEffects(ServerPlayer player, PlayerActionTarget actionTarget) {
        var registry = player.level().registryAccess().registryOrThrow(CraftorioUpgrade.REGISTRY_KEY);
        for (ResourceLocation id : CraftorioMisc.getUnlockedUpgrades(player)) {
            CraftorioUpgrade upgrade = registry.get(id);
            if (upgrade instanceof CraftorioActionEffectUpgrade actionEffectUpgrade && actionEffectUpgrade.getTarget() == actionTarget) {
                CraftorioMisc.grantEffect(player, actionEffectUpgrade.getEffect());
            }
        }
    }

    @SubscribeEvent
    public void serverTick(ServerTickEvent.Post event) {
        if (!pendingDimensionAreaSetup.isEmpty()) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                ResourceKey<Level> pendingDimension = pendingDimensionAreaSetup.remove(player.getUUID());
                if (pendingDimension == null) continue;
                if (!player.level().dimension().equals(pendingDimension)) continue;

                setArea(player, player.getOnPos(), pendingDimension);

                List<ResourceKey<Level>> newDimensions = new ArrayList<>(CraftorioMisc.getDimensionsExplored(player));
                if (!newDimensions.contains(pendingDimension)) {
                    newDimensions.add(pendingDimension);
                    CraftorioMisc.setDimensionsExplored(player, newDimensions);
                }
            }
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)) continue;

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

    private static final Map<UUID, ResourceKey<Level>> pendingDimensionAreaSetup = new HashMap<>();

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

                double rarerEffectChance = CraftorioMisc.getUpgradeModifierSum(player, ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD);
                CraftorioEffects rolledEffect = CraftorioMisc.getRandomObtainableEffect(level.registryAccess(), player.getRandom(), rarerEffectChance);
                CraftorioMisc.grantEffect(player, rolledEffect.copy());

                int baseEffectTicks = Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get() * CraftorioMisc.SECONDS_TO_TICKS;
                int effectInterval = CraftorioMisc.applySpeedUpgrade(player, ModifierTarget.EFFECT_TIMER_SPEED, baseEffectTicks);
                CraftorioMisc.setRandomEffectTime(player, effectInterval);
            }
        }
    }

    private void tickUniversalRandomEffect(MinecraftServer server, ServerLevel overworld) {
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
            double rarerEffectChance = CraftorioMisc.getUpgradeModifierSum(allPlayers.get(0), ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD);
            CraftorioEffects rolledEffect = CraftorioMisc.getRandomObtainableEffect(overworld.registryAccess(), overworld.random, rarerEffectChance);
            CraftorioMisc.grantEffect(allPlayers.get(0), rolledEffect.copy());
        }

        int baseEffectTicks = Craftorio.SERVER_CONFIG.RANDOM_EFFECT_INTERVAL.get() * CraftorioMisc.SECONDS_TO_TICKS;
        int effectInterval = allPlayers.isEmpty() ? baseEffectTicks
                : CraftorioMisc.applySpeedUpgrade(allPlayers.get(0), ModifierTarget.EFFECT_TIMER_SPEED, baseEffectTicks);
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

                double rarerContractChance = CraftorioMisc.getUpgradeModifierSum(player, ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD);
                List<ResourceLocation> offer = CraftorioMisc.rollContractOffer(player.registryAccess(), player.getRandom(), CraftorioMisc.getHighestPoints(player), rarerContractChance);
                player.setData(CraftorioDataAttachments.CONTRACT_OFFER, offer);
                player.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);

                int refreshTicks = CraftorioMisc.applySpeedUpgrade(player, ModifierTarget.CONTRACT_REFRESH_SPEED, baseRefreshTicks);
                CraftorioMisc.setContractRefreshTime(player, refreshTicks);

                notifyNewContracts(player, offer, refreshTicks);
            }
        }
    }

    private void tickUniversalContractOffer(MinecraftServer server, ServerLevel overworld, int baseRefreshTicks) {
        int timeUntilRefresh = CraftorioMisc.getContractRefreshTime(overworld) - 1;

        if (timeUntilRefresh > 0) {
            CraftorioMisc.setContractRefreshTime(overworld, timeUntilRefresh);
            return;
        }

        List<ServerPlayer> allPlayers = server.getPlayerList().getPlayers();

        BigInteger highestPoints = allPlayers.isEmpty() ? BigInteger.ZERO
                : CraftorioMisc.getHighestPoints(allPlayers.get(0));
        double rarerContractChance = allPlayers.isEmpty() ? 0
                : CraftorioMisc.getUpgradeModifierSum(allPlayers.get(0), ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD);
        List<ResourceLocation> offer = CraftorioMisc.rollContractOffer(overworld.registryAccess(), overworld.random, highestPoints, rarerContractChance);
        overworld.setData(CraftorioDataAttachments.CONTRACT_OFFER, offer);
        overworld.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);

        int refreshTicks = allPlayers.isEmpty() ? baseRefreshTicks
                : CraftorioMisc.applySpeedUpgrade(allPlayers.get(0), ModifierTarget.CONTRACT_REFRESH_SPEED, baseRefreshTicks);
        CraftorioMisc.setContractRefreshTime(overworld, refreshTicks);

        for (ServerPlayer player : allPlayers) {
            notifyNewContracts(player, offer, refreshTicks);
        }
    }

    private void notifyNewContracts(ServerPlayer player, List<ResourceLocation> offer, int refreshTicks) {
        if (pendingContractScreenPush.remove(player.getUUID())) {
            PacketDistributor.sendToPlayer(player, new OpenContractOfferScreenPacket(offer, refreshTicks));
            return;
        }

        if (offer.isEmpty()) return;
        PacketDistributor.sendToPlayer(player, new ContractOfferStatusPacket(true));
    }

}

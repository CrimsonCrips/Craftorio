package org.crimsoncrips.craftorio.server.sacrifice;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.networking.sacrifice.SacrificeShatterPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.haven.CraftorioHavenDimension;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CraftorioSacrifice {

    private enum Stage { IDLE, UNLOADING, SETTLING, WAITING, SHATTERING }

    private static final BigInteger SACRIFICE_POINTS_PER_SACRIFICE = BigInteger.ONE;
    private static final int UNLOAD_TIMEOUT_TICKS = 1200;
    private static final int SETTLE_TICKS = 60;
    private static final int SHATTER_TICKS = 160;
    private static final int CUTOFF_MARGIN_SECONDS = 3;
    private static final long MILLIS_PER_MINUTE = 60_000L;

    private static Stage stage = Stage.IDLE;
    private static int stageTicks = 0;
    private static int previousSpawnChunkRadius = 2;
    private static boolean releasing = false;
    private static boolean newSeedRequested = false;
    private static Long chosenSeed = null;
    private static boolean areaMode = false;
    private static List<ChunkRect> areaRects = List.of();
    private static final Map<ResourceKey<Level>, long[]> forcedBackup = new HashMap<>();
    private static final Set<UUID> deadlineWarned = new HashSet<>();
    private static final Set<UUID> participantIds = new HashSet<>();
    private static final Set<UUID> gatheredIds = new HashSet<>();

    private static Set<UUID> requiredPlayers = null;
    private static final Set<UUID> consented = new HashSet<>();

    private CraftorioSacrifice() {}

    public static boolean isPending(ServerPlayer player) {
        return player.getData(CraftorioDataAttachments.SACRIFICE_PENDING);
    }

    private static boolean inHaven(ServerPlayer player) {
        return player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    public static void enter(ServerPlayer player) {
        if (inHaven(player) || isPending(player)) return;
        if (isRunning()) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_in_progress").withStyle(ChatFormatting.RED));
            return;
        }

        long remaining = player.getData(CraftorioDataAttachments.SACRIFICE_COOLDOWN_UNTIL) - System.currentTimeMillis();
        if (remaining > 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_cooldown", (remaining + MILLIS_PER_MINUTE - 1) / MILLIS_PER_MINUTE).withStyle(ChatFormatting.RED));
            return;
        }
        if (!hasRequiredLife(player)) {
            notifyLifeRequirement(player);
            return;
        }
        if (player.getServer() == null || player.getServer().getLevel(CraftorioDimensions.HAVEN_LEVEL_KEY) == null) return;

        Inventory inventory = player.getInventory();
        List<ItemStack> stored = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            stored.add(inventory.getItem(slot).copy());
        }
        player.setData(CraftorioDataAttachments.SACRIFICE_STORED_INVENTORY, stored);
        player.setData(CraftorioDataAttachments.SACRIFICE_PENDING, true);
        player.setData(CraftorioDataAttachments.SACRIFICE_DEADLINE, System.currentTimeMillis() + timeLimitMillis());
        deadlineWarned.remove(player.getUUID());
        inventory.clearContent();

        CraftorioHavenDimension.enterForSacrifice(player);
        player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_entered",
                Component.keybind("key.craftorio.open_hub")).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_time_limit",
                Craftorio.SERVER_CONFIG.SACRIFICE_TIME_LIMIT_MINUTES.get(), Craftorio.SERVER_CONFIG.SACRIFICE_COOLDOWN_MINUTES.get()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    private static long timeLimitMillis() {
        return Craftorio.SERVER_CONFIG.SACRIFICE_TIME_LIMIT_MINUTES.get() * MILLIS_PER_MINUTE;
    }

    private static long cooldownMillis() {
        return Craftorio.SERVER_CONFIG.SACRIFICE_COOLDOWN_MINUTES.get() * MILLIS_PER_MINUTE;
    }

    private static boolean hasRequiredLife(ServerPlayer player) {
        return player.isCreative() || CraftorioMisc.getLife(player) >= Craftorio.SERVER_CONFIG.SACRIFICE_REQUIRED_LIFE.get();
    }

    private static void notifyLifeRequirement(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_requires_life",
                Craftorio.SERVER_CONFIG.SACRIFICE_REQUIRED_LIFE.get(), CraftorioMisc.getLife(player)).withStyle(ChatFormatting.RED));
    }

    public static void refuse(ServerPlayer player) {
        if (isRunning() || !isPending(player)) return;

        release(player, false);
    }

    private static void release(ServerPlayer player, boolean timedOut) {
        MinecraftServer server = player.getServer();
        cancelVote(server, player);
        restoreInventory(player);

        if (server != null && inHaven(player)) {
            teleportToRespawn(server, player);
        }

        if (timedOut) {
            player.setData(CraftorioDataAttachments.SACRIFICE_COOLDOWN_UNTIL, System.currentTimeMillis() + cooldownMillis());
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_timed_out", Craftorio.SERVER_CONFIG.SACRIFICE_COOLDOWN_MINUTES.get()).withStyle(ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_refused").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    private static void checkDeadlines(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            if (!isPending(player) || !inHaven(player)) continue;

            long deadline = player.getData(CraftorioDataAttachments.SACRIFICE_DEADLINE);
            if (deadline <= 0) {
                player.setData(CraftorioDataAttachments.SACRIFICE_DEADLINE, now + timeLimitMillis());
            } else if (now >= deadline) {
                deadlineWarned.remove(player.getUUID());
                release(player, true);
            } else if (deadline - now <= MILLIS_PER_MINUTE && deadlineWarned.add(player.getUUID())) {
                player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_time_warning").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    public static void restoreStranded(ServerPlayer player) {
        if (isRunning() || !isPending(player) || inHaven(player)) return;

        cancelVote(player.getServer(), player);
        restoreInventory(player);
    }

    private static void restoreInventory(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        List<ItemStack> stored = player.getData(CraftorioDataAttachments.SACRIFICE_STORED_INVENTORY);

        inventory.clearContent();
        for (int slot = 0; slot < stored.size() && slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, stored.get(slot).copy());
        }

        player.setData(CraftorioDataAttachments.SACRIFICE_STORED_INVENTORY, new ArrayList<>());
        player.setData(CraftorioDataAttachments.SACRIFICE_PENDING, false);
        player.setData(CraftorioDataAttachments.SACRIFICE_DEADLINE, 0L);
    }

    private static void teleportToRespawn(MinecraftServer server, ServerPlayer player) {
        ServerLevel level = server.getLevel(player.getRespawnDimension());
        BlockPos respawn = player.getRespawnPosition();
        if (level == null || respawn == null) {
            level = server.overworld();
            respawn = level.getSharedSpawnPos();
        }
        player.teleportTo(level, respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    public static void accept(ServerPlayer player, boolean newSeed) {
        MinecraftServer server = player.getServer();
        if (server == null || !isPending(player) || !inHaven(player)) return;
        if (isRunning()) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_in_progress").withStyle(ChatFormatting.RED));
            return;
        }
        if (!hasRequiredLife(player)) {
            notifyLifeRequirement(player);
            return;
        }

        List<ServerPlayer> participants;
        if (CraftorioMisc.universalBased(server.overworld())) {
            Set<UUID> onlineIds = onlineIds(server);
            if (requiredPlayers != null && !requiredPlayers.equals(onlineIds)) {
                cancelVote(server, null);
            }
            if (requiredPlayers == null) {
                requiredPlayers = onlineIds;
                consented.clear();
            }

            consented.add(player.getUUID());
            newSeedRequested |= newSeed;
            if (!consented.containsAll(requiredPlayers)) {
                for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                    online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_consent_progress", consented.size(), requiredPlayers.size()).withStyle(ChatFormatting.YELLOW));
                }
                return;
            }

            participants = new ArrayList<>(server.getPlayerList().getPlayers());
            clearVote();
            for (ServerPlayer participant : participants) {
                if (!isPending(participant) || !inHaven(participant)) {
                    for (ServerPlayer online : participants) {
                        online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_consent_cancelled").withStyle(ChatFormatting.RED));
                    }
                    return;
                }
            }
        } else {
            newSeedRequested = false;
            participants = List.of(player);
        }

        boolean changeSeed = newSeedRequested;
        newSeedRequested = false;
        execute(server, participants, changeSeed);
    }

    public static void onRosterChanged(MinecraftServer server) {
        if (requiredPlayers != null && !requiredPlayers.equals(onlineIds(server))) {
            cancelVote(server, null);
        }
    }

    private static void cancelVote(MinecraftServer server, ServerPlayer refusing) {
        if (requiredPlayers == null) return;
        clearVoteState(server, refusing);
    }

    private static void clearVoteState(MinecraftServer server, ServerPlayer refusing) {
        clearVote();
        if (server == null) return;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online != refusing) {
                online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_consent_cancelled").withStyle(ChatFormatting.RED));
            }
        }
    }

    private static void clearVote() {
        requiredPlayers = null;
        consented.clear();
    }

    private static Set<UUID> onlineIds(MinecraftServer server) {
        Set<UUID> ids = new HashSet<>();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            ids.add(online.getUUID());
        }
        return ids;
    }

    public static boolean forceStart(MinecraftServer server, boolean newSeed, ServerPlayer target) {
        if (isRunning()) return false;

        if (CraftorioMisc.universalBased(server.overworld())) {
            execute(server, new ArrayList<>(server.getPlayerList().getPlayers()), newSeed);
            return true;
        }
        if (target == null) return false;

        execute(server, List.of(target), false);
        return true;
    }

    private static void execute(MinecraftServer server, List<ServerPlayer> participants, boolean newSeed) {
        ServerLevel overworld = server.overworld();
        boolean universal = CraftorioMisc.universalBased(overworld);
        areaMode = !universal;
        areaRects = areaMode && !participants.isEmpty() ? CraftorioWipeAreas.forPlayer(server, participants.get(0)) : List.of();
        Craftorio.LOGGER.info("Sacrifice started ({}), unloading every dimension except the Haven", areaMode ? "reset area of " + participants.size() + " player, " + areaRects.size() + " chunk regions" : "whole world");

        GameRules.IntegerValue spawnRadius = server.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS);
        previousSpawnChunkRadius = spawnRadius.get();

        stage = Stage.UNLOADING;
        stageTicks = 0;
        participantIds.clear();
        gatheredIds.clear();
        forcedBackup.clear();
        chosenSeed = universal && newSeed ? new Random().nextLong() : null;

        CraftorioWorldWipe.beginPurge(server, previousSpawnChunkRadius, areaMode ? areaRects : null);
        spawnRadius.set(0, server);

        for (ServerLevel level : server.getAllLevels()) {
            if (isHavenLevel(level)) continue;
            long[] forced = level.getForcedChunks().toLongArray();
            if (areaMode && forced.length > 0) {
                forcedBackup.put(level.dimension(), forced);
            }
            for (long chunk : forced) {
                level.setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false);
            }
        }

        if (universal) {
            overworld.setData(CraftorioDataAttachments.SACRIFICE_POINTS, overworld.getData(CraftorioDataAttachments.SACRIFICE_POINTS).add(SACRIFICE_POINTS_PER_SACRIFICE));
        }

        for (ServerPlayer participant : participants) {
            participantIds.add(participant.getUUID());
            if (!inHaven(participant)) {
                CraftorioHavenDimension.enterForSacrifice(participant);
            }
            if (!universal) {
                participant.setData(CraftorioDataAttachments.SACRIFICE_POINTS, participant.getData(CraftorioDataAttachments.SACRIFICE_POINTS).add(SACRIFICE_POINTS_PER_SACRIFICE));
            }
            participant.setData(CraftorioDataAttachments.SACRIFICE_PENDING, false);
            participant.setData(CraftorioDataAttachments.SACRIFICE_DEADLINE, 0L);
            setHavenRespawn(participant);
        }

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (participantIds.contains(online.getUUID())) {
                online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_purging").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            } else {
                gather(online);
                online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_waiting").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            }
        }
    }

    private static void gather(ServerPlayer player) {
        if (player.getData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN).isEmpty()) {
            BlockPos respawn = player.getRespawnPosition();
            GlobalPos origin;
            if (!inHaven(player)) {
                origin = GlobalPos.of(player.level().dimension(), player.blockPosition());
            } else if (respawn == null) {
                origin = GlobalPos.of(Level.OVERWORLD, player.getServer().overworld().getSharedSpawnPos());
            } else {
                origin = GlobalPos.of(player.getRespawnDimension(), respawn);
            }
            player.setData(CraftorioDataAttachments.HAVEN_RETURN_POS, origin);
        }

        if (!inHaven(player)) {
            CraftorioHavenDimension.enterForSacrifice(player);
        }
        gatheredIds.add(player.getUUID());
        saveRespawn(player);
        setHavenRespawn(player);
        player.setData(CraftorioDataAttachments.SACRIFICE_WAITING, true);
    }

    private static void returnFromHaven(MinecraftServer server, ServerPlayer player, boolean wiped) {
        GlobalPos origin = player.getData(CraftorioDataAttachments.HAVEN_RETURN_POS);
        ServerLevel level = server.getLevel(origin.dimension());
        if (level == null) {
            level = server.overworld();
        }

        if (wiped || CraftorioWipeAreas.contains(areaRects, level.dimension().location().toString(), origin.pos().getX() >> 4, origin.pos().getZ() >> 4)) {
            relocateToSpawn(server, player);
            return;
        }
        player.teleportTo(level, origin.pos().getX() + 0.5, origin.pos().getY(), origin.pos().getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    private static void restoreForced(MinecraftServer server) {
        for (Map.Entry<ResourceKey<Level>, long[]> entry : forcedBackup.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) continue;

            String dimension = entry.getKey().location().toString();
            for (long chunk : entry.getValue()) {
                if (!CraftorioWipeAreas.contains(areaRects, dimension, ChunkPos.getX(chunk), ChunkPos.getZ(chunk))) {
                    level.setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true);
                }
            }
        }
        forcedBackup.clear();
    }

    private static void saveRespawn(ServerPlayer player) {
        if (player.getData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN).isPresent()) return;

        BlockPos position = player.getRespawnPosition();
        Optional<GlobalPos> pos = position == null ? Optional.empty() : Optional.of(GlobalPos.of(player.getRespawnDimension(), position));
        player.setData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN,
                Optional.of(new SavedRespawn(pos, player.getRespawnAngle(), player.isRespawnForced())));
    }

    private static void setHavenRespawn(ServerPlayer player) {
        player.setRespawnPosition(CraftorioDimensions.HAVEN_LEVEL_KEY, CraftorioHavenDimension.platformCenter(), 0F, true, false);
    }

    private static void restoreRespawn(ServerPlayer player) {
        Optional<SavedRespawn> saved = player.getData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN);
        if (saved.isEmpty()) return;

        SavedRespawn respawn = saved.get();
        if (respawn.pos().isPresent()) {
            GlobalPos pos = respawn.pos().get();
            player.setRespawnPosition(pos.dimension(), pos.pos(), respawn.angle(), respawn.forced(), false);
        } else {
            player.setRespawnPosition(Level.OVERWORLD, null, 0F, false, false);
        }
        player.setData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN, Optional.empty());
    }

    public static boolean blocksTravel(ResourceKey<Level> destination) {
        return isRunning() && !releasing && !destination.equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    public static void onRespawn(ServerPlayer player) {
        if (isRunning() && !releasing && !inHaven(player)) {
            CraftorioHavenDimension.enterForSacrifice(player);
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        if (isRunning()) {
            finish(server);
        }
        reset();
    }

    public static void reset() {
        stage = Stage.IDLE;
        stageTicks = 0;
        releasing = false;
        newSeedRequested = false;
        chosenSeed = null;
        areaMode = false;
        areaRects = List.of();
        forcedBackup.clear();
        participantIds.clear();
        gatheredIds.clear();
        clearVote();
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 == 0) {
            if (stage == Stage.IDLE) {
                checkDeadlines(server);
            }
        }
        if (stage == Stage.IDLE) return;
        stageTicks++;

        switch (stage) {
            case UNLOADING -> {
                boolean timedOut = stageTicks >= UNLOAD_TIMEOUT_TICKS;
                if (stageTicks % 20 == 0 && (allUnloaded(server) || timedOut)) {
                    if (timedOut) {
                        Craftorio.LOGGER.warn("Some chunks stayed loaded during the sacrifice, continuing anyway");
                    }
                    Craftorio.LOGGER.info("Sacrifice: all chunks unloaded, settling");
                    stage = Stage.SETTLING;
                    stageTicks = 0;
                }
            }
            case SETTLING -> {
                if (stageTicks >= SETTLE_TICKS) {
                    CraftorioWorldWipe.finishPurge(server, CraftorioWorldWipe.nowSeconds() + CUTOFF_MARGIN_SECONDS);
                    Craftorio.LOGGER.info("Sacrifice: old chunk data invalidated, waiting for the cutoff");
                    stage = Stage.WAITING;
                    stageTicks = 0;
                }
            }
            case WAITING -> {
                if (CraftorioWorldWipe.nowSeconds() >= CraftorioWorldWipe.cutoff()) {
                    startShattering(server);
                }
            }
            case SHATTERING -> {
                if (stageTicks >= SHATTER_TICKS) {
                    complete(server);
                }
            }
            default -> {
            }
        }
    }

    private static boolean allUnloaded(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isHavenLevel(level)) continue;
            if (level.getChunkSource().getLoadedChunksCount() > 0 || level.getChunkSource().chunkMap.hasWork()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHavenLevel(ServerLevel level) {
        return level.dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    private static void startShattering(MinecraftServer server) {
        Craftorio.LOGGER.info("Sacrifice: world reset, shattering the players' screens before disconnecting");
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (!areaMode || participantIds.contains(online.getUUID())) {
                PacketDistributor.sendToPlayer(online, new SacrificeShatterPacket());
            }
        }
        stage = Stage.SHATTERING;
        stageTicks = 0;
    }

    private static void complete(MinecraftServer server) {
        boolean halt = !areaMode;
        Craftorio.LOGGER.info(halt ? "Sacrifice complete, disconnecting players so the world can be finalized" : "Sacrifice complete, disconnecting the sacrificing player");

        List<ServerPlayer> leaving = new ArrayList<>();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (halt || participantIds.contains(online.getUUID())) {
                leaving.add(online);
            }
        }
        finish(server);

        Component reason = Component.translatable("misc.craftorio.sacrifice_disconnect");
        for (ServerPlayer player : leaving) {
            player.connection.disconnect(reason);
        }
        if (halt) {
            server.halt(false);
        }
    }

    private static void finish(MinecraftServer server) {
        server.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(previousSpawnChunkRadius, server);
        CraftorioWorldWipe.spawnRadiusRestored(server);

        ServerLevel overworld = server.overworld();
        int count = overworld.getData(CraftorioDataAttachments.SACRIFICE_COUNT) + 1;
        overworld.setData(CraftorioDataAttachments.SACRIFICE_COUNT, count);
        if (!areaMode) {
            resetProgress(overworld);
        }

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (participantIds.contains(online.getUUID())) {
                resetPlayer(online);
                online.setData(CraftorioDataAttachments.SACRIFICES_APPLIED, count);
            } else if (gatheredIds.contains(online.getUUID())) {
                online.setData(CraftorioDataAttachments.SACRIFICE_WAITING, false);
                if (isPending(online)) {
                    restoreInventory(online);
                }
                restoreRespawn(online);
                if (areaMode) {
                    returnFromHaven(server, online, false);
                    online.setData(CraftorioDataAttachments.SACRIFICES_APPLIED, count);
                }
            }
        }

        if (areaMode) {
            restoreForced(server);
            CraftorioWipeAreas.log(server, count, areaRects);
            for (UUID participant : participantIds) {
                CraftorioWipeAreas.forgetChunks(server, areaRects, participant.toString());
            }
        } else {
            CraftorioWorldWipe.writeFinalizeMarker(server, chosenSeed);
        }
        reset();
    }

    public static void onLogout(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || !isRunning() || !participantIds.contains(player.getUUID())) return;
        if (!areaMode) return;

        resetPlayer(player);
        player.setData(CraftorioDataAttachments.SACRIFICES_APPLIED, server.overworld().getData(CraftorioDataAttachments.SACRIFICE_COUNT) + 1);
    }

    public static boolean isRunning() {
        return stage != Stage.IDLE;
    }

    public static void onLogin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (isRunning()) {
            if (!areaMode) {
                participantIds.add(player.getUUID());
            }

            if (participantIds.contains(player.getUUID())) {
                if (!inHaven(player)) {
                    CraftorioHavenDimension.enterForSacrifice(player);
                }
                setHavenRespawn(player);
            } else {
                gather(player);
            }
            return;
        }

        int count = server.overworld().getData(CraftorioDataAttachments.SACRIFICE_COUNT);
        int applied = player.getData(CraftorioDataAttachments.SACRIFICES_APPLIED);

        if (inHaven(player) && !isPending(player) && player.getData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN).isPresent()) {
            GlobalPos origin = player.getData(CraftorioDataAttachments.HAVEN_RETURN_POS);
            boolean wiped = CraftorioWipeAreas.wipedSince(server, applied, origin.dimension().location().toString(), new ChunkPos(origin.pos()));
            returnFromHaven(server, player, wiped);
        }
        restoreRespawn(player);
        restoreStranded(player);

        if (applied >= count) return;

        if (CraftorioMisc.universalBased(server.overworld())) {
            resetPlayer(player);
        } else if (CraftorioWipeAreas.wipedSince(server, applied, player.level().dimension().location().toString(), player.chunkPosition())) {
            relocateToSpawn(server, player);
        }
        player.setData(CraftorioDataAttachments.SACRIFICES_APPLIED, count);
    }

    private static void relocateToSpawn(MinecraftServer server, ServerPlayer player) {
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        BlockPos top = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, spawn);
        player.teleportTo(overworld, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    private static void resetPlayer(ServerPlayer player) {
        removeUpgradeModifiers(player);

        player.getInventory().clearContent();
        player.getEnderChestInventory().clearContent();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.setData(CraftorioDataAttachments.SACRIFICE_STORED_INVENTORY, new ArrayList<>());
        player.setData(CraftorioDataAttachments.SACRIFICE_PENDING, false);
        player.setData(CraftorioDataAttachments.SACRIFICE_DEADLINE, 0L);

        resetProgress(player);
        player.setData(CraftorioDataAttachments.GIVEN, false);
        player.setData(CraftorioDataAttachments.SPAWN_ORIGIN, GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO));
        player.setRespawnPosition(Level.OVERWORLD, null, 0F, false, false);

        revokeAdvancements(player);
    }

    private static void resetProgress(IAttachmentHolder holder) {
        BigInteger starting = CraftorioMisc.startingValue();

        holder.setData(CraftorioDataAttachments.POINTS, starting);
        holder.setData(CraftorioDataAttachments.TEMP_POINTS, BigInteger.ZERO);
        holder.setData(CraftorioDataAttachments.HIGHEST_REACHED_POINTS, starting);
        holder.setData(CraftorioDataAttachments.GENERAL_MULTIPLIER_EFFECTS, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.TAG_MULTIPLIER_EFFECTS, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.SHOP_MULTIPLIER_EFFECTS, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.CONTRACTS, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.CONTRACT_OFFER, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, false);
        holder.setData(CraftorioDataAttachments.CONTRACT_REFRESH_TIME, 0);
        holder.setData(CraftorioDataAttachments.RANDOM_EFFECT_TIME, 0);
        holder.setData(CraftorioDataAttachments.UNLOCKED_UPGRADES, new HashMap<>());
        holder.setData(CraftorioDataAttachments.REBIRTH_UPGRADES_UNLOCKED, new HashMap<>());
        holder.setData(CraftorioDataAttachments.ITEMS_SINKED, new HashMap<>());
        holder.setData(CraftorioDataAttachments.CONTRACTS_COMPLETED, 0);
        holder.setData(CraftorioDataAttachments.HIGHEST_MULTIPLIER, 0.0F);
        holder.setData(CraftorioDataAttachments.ADVANCEMENT_MULTIPLIER_BONUS, 0.0);
        holder.setData(CraftorioDataAttachments.LIFE, 1);
        holder.setData(CraftorioDataAttachments.LIFE_POINTS, BigInteger.ZERO);
        holder.setData(CraftorioDataAttachments.AMOUNT_OF_LAND, 0L);
        holder.setData(CraftorioDataAttachments.PLAYER_BORDERS, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.DIMENSIONS_EXPLORED, new ArrayList<>());
        holder.setData(CraftorioDataAttachments.UNIVERSAL_PROGRESS_STARTED, false);
    }

    private static void removeUpgradeModifiers(ServerPlayer player) {
        removeModifiers(player, CraftorioUpgrade.REGISTRY_KEY);
        removeModifiers(player, CraftorioUpgrade.REBIRTH_REGISTRY_KEY);
    }

    private static void removeModifiers(ServerPlayer player, ResourceKey<Registry<CraftorioUpgrade>> registryKey) {
        Registry<CraftorioUpgrade> registry = player.level().registryAccess().registryOrThrow(registryKey);
        for (ResourceLocation id : registry.keySet()) {
            if (!(registry.get(id) instanceof CraftorioAttributeUpgrade attributeUpgrade)) continue;

            Holder<Attribute> attribute = attributeUpgrade.getTarget().getAttribute();
            if (attribute == null) continue;

            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(id);
            }
        }
    }

    private static void revokeAdvancements(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            List<String> completed = new ArrayList<>();
            progress.getCompletedCriteria().forEach(completed::add);
            for (String criterion : completed) {
                player.getAdvancements().revoke(holder, criterion);
            }
        }
    }
}

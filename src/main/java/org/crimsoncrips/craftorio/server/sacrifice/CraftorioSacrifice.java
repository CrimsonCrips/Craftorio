package org.crimsoncrips.craftorio.server.sacrifice;

import java.util.OptionalLong;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.networking.sacrifice.OpenSacrificeSkillTreeScreenPacket;
import org.crimsoncrips.craftorio.networking.sacrifice.SacrificeShatterPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.server.consent.CraftorioConsentSync;
import org.crimsoncrips.craftorio.server.haven.CraftorioHavenDimension;
import org.crimsoncrips.craftorio.server.loan.CraftorioLoanShark;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CraftorioSacrifice {

    private enum Stage { IDLE, COUNTDOWN, UNLOADING, SETTLING, WAITING, SHATTERING }

    private static final BigInteger SACRIFICE_POINTS_PER_SACRIFICE = BigInteger.ONE;
    private static final int COUNTDOWN_TICKS = 200;
    private static final int UNLOAD_TIMEOUT_TICKS = 1200;
    private static final int SETTLE_TICKS = 60;
    private static final int SHATTER_TICKS = 160;
    private static final int CUTOFF_MARGIN_SECONDS = 3;
    private static final int TREE_OPEN_DELAY_TICKS = 60;
    public static final int KEEP_DIFFICULTY = -1;
    private static final List<UUID> pendingParticipants = new ArrayList<>();
    private static Long pendingSeed = null;
    private static int pendingDifficulty = KEEP_DIFFICULTY;
    private static final long MILLIS_PER_MINUTE = 60_000L;

    private static Stage stage = Stage.IDLE;
    private static int stageTicks = 0;
    private static int previousSpawnChunkRadius = 2;
    private static boolean releasing = false;
    private static Long requestedSeed = null;
    private static final Set<UUID> lifeWaived = new HashSet<>();
    private static Long chosenSeed = null;
    private static int requestedDifficulty = KEEP_DIFFICULTY;
    private static int chosenDifficulty = KEEP_DIFFICULTY;
    private static boolean areaMode = false;
    private static List<ChunkRect> areaRects = List.of();
    private static final Map<ResourceKey<Level>, long[]> forcedBackup = new HashMap<>();
    private static final Set<UUID> deadlineWarned = new HashSet<>();
    private static final Set<UUID> participantIds = new HashSet<>();
    private static final Set<UUID> gatheredIds = new HashSet<>();

    private static Set<UUID> requiredPlayers = null;
    private static final Set<UUID> consented = new HashSet<>();
    private static UUID voteProposer = null;
    private static long voteDeadline = 0L;

    private CraftorioSacrifice() {}

    public static boolean isPending(ServerPlayer player) {
        return player.getData(CraftorioDataAttachments.SACRIFICE_PENDING);
    }

    private static boolean inHaven(ServerPlayer player) {
        return player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    public static void enter(ServerPlayer player) {
        enter(player, false);
    }

    public static boolean enter(ServerPlayer player, boolean waiveLife) {
        if (inHaven(player) || isPending(player)) return false;
        if (isRunning()) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_in_progress").withStyle(ChatFormatting.RED));
            return false;
        }

        long remaining = player.getData(CraftorioDataAttachments.SACRIFICE_COOLDOWN_UNTIL) - System.currentTimeMillis();
        if (remaining > 0) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_cooldown", (remaining + MILLIS_PER_MINUTE - 1) / MILLIS_PER_MINUTE).withStyle(ChatFormatting.RED));
            return false;
        }
        if (!waiveLife && !hasRequiredLife(player)) {
            notifyLifeRequirement(player);
            return false;
        }
        if (player.getServer() == null || player.getServer().getLevel(CraftorioDimensions.HAVEN_LEVEL_KEY) == null) return false;

        if (waiveLife) {
            lifeWaived.add(player.getUUID());
        } else {
            lifeWaived.remove(player.getUUID());
        }

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
        return true;
    }

    private static long timeLimitMillis() {
        return Craftorio.SERVER_CONFIG.SACRIFICE_TIME_LIMIT_MINUTES.get() * MILLIS_PER_MINUTE;
    }

    private static long cooldownMillis() {
        return Craftorio.SERVER_CONFIG.SACRIFICE_COOLDOWN_MINUTES.get() * MILLIS_PER_MINUTE;
    }

    private static boolean hasRequiredLife(ServerPlayer player) {
        return CraftorioMisc.getLife(player) >= Craftorio.SERVER_CONFIG.SACRIFICE_REQUIRED_LIFE.get();
    }

    private static void notifyLifeRequirement(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_requires_life",
                Craftorio.SERVER_CONFIG.SACRIFICE_REQUIRED_LIFE.get(), CraftorioMisc.getLife(player)).withStyle(ChatFormatting.RED));
    }

    public static void refuse(ServerPlayer player) {
        if (isRunning() || !isPending(player)) return;

        release(player, ReleaseReason.REFUSED);
    }

    private enum ReleaseReason { REFUSED, TIMED_OUT, VOTE_FAILED }

    private static void release(ServerPlayer player, ReleaseReason reason) {
        MinecraftServer server = player.getServer();
        lifeWaived.remove(player.getUUID());
        cancelVote(server, player);
        clearPending(player);

        if (server != null && inHaven(player)) {
            CraftorioHavenDimension.leaveHaven(player, target -> {
                teleportToRespawn(server, target);
                restoreStoredItems(target);
            });
        } else {
            restoreStoredItems(player);
        }

        int cooldownMinutes = Craftorio.SERVER_CONFIG.SACRIFICE_COOLDOWN_MINUTES.get();
        switch (reason) {
            case TIMED_OUT -> {
                player.setData(CraftorioDataAttachments.SACRIFICE_COOLDOWN_UNTIL, System.currentTimeMillis() + cooldownMillis());
                player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_timed_out", cooldownMinutes).withStyle(ChatFormatting.RED));
            }
            case REFUSED -> {
                player.setData(CraftorioDataAttachments.SACRIFICE_COOLDOWN_UNTIL, System.currentTimeMillis() + cooldownMillis());
                player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_refused_cooldown", cooldownMinutes).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            case VOTE_FAILED -> player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_refused").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    private static void openDueTrees(MinecraftServer server) {
        if (isRunning()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getData(CraftorioDataAttachments.SACRIFICE_TREE_PENDING) && player.tickCount >= TREE_OPEN_DELAY_TICKS && !inHaven(player)) {
                player.setData(CraftorioDataAttachments.SACRIFICE_TREE_PENDING, false);
                ServerEvents.syncUniversalState(player);
                PacketDistributor.sendToPlayer(player, new OpenSacrificeSkillTreeScreenPacket());
            }
        }
    }

    private static void checkDeadlines(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            if (!isPending(player)) continue;
            if (!inHaven(player)) {
                restoreStranded(player);
                continue;
            }

            long deadline = player.getData(CraftorioDataAttachments.SACRIFICE_DEADLINE);
            if (deadline <= 0) {
                player.setData(CraftorioDataAttachments.SACRIFICE_DEADLINE, now + timeLimitMillis());
            } else if (now >= deadline) {
                deadlineWarned.remove(player.getUUID());
                release(player, ReleaseReason.TIMED_OUT);
            } else if (deadline - now <= MILLIS_PER_MINUTE && deadlineWarned.add(player.getUUID())) {
                player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_time_warning").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    public static void restoreStranded(ServerPlayer player) {
        if (isRunning() || !isPending(player) || inHaven(player) || CraftorioHavenDimension.isTransitioning(player)) return;

        deadlineWarned.remove(player.getUUID());
        release(player, ReleaseReason.REFUSED);
    }

    private static void restoreStoredItems(ServerPlayer player) {
        List<ItemStack> stored = player.getData(CraftorioDataAttachments.SACRIFICE_STORED_INVENTORY);
        if (stored.isEmpty()) return;

        Inventory inventory = player.getInventory();
        inventory.clearContent();
        for (int slot = 0; slot < stored.size() && slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, stored.get(slot).copy());
        }
        player.setData(CraftorioDataAttachments.SACRIFICE_STORED_INVENTORY, new ArrayList<>());
    }

    private static void clearPending(ServerPlayer player) {
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

    public static void accept(ServerPlayer player, String seed, int difficulty) {
        acceptVote(player, seed, difficulty);
        if (requiredPlayers == null || !consented.contains(player.getUUID())) {
            CraftorioConsentSync.sendInactive(player, ConsentKind.SACRIFICE);
        }
    }

    public static void withdrawConsent(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || requiredPlayers == null || !consented.remove(player.getUUID())) return;

        CraftorioConsentSync.notifyWithdrawn(server, player);
        if (consented.isEmpty()) {
            failVote(server, null);
            return;
        }
        broadcastVoteStatus(server);
    }

    public static void voteConsent(ServerPlayer player, boolean agree) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        if (requiredPlayers == null) {
            CraftorioConsentSync.sendInactive(player, ConsentKind.SACRIFICE);
            return;
        }

        if (agree) {
            accept(player, "", KEEP_DIFFICULTY);
            return;
        }

        CraftorioConsentSync.notifyDeclined(server, player);
        failVote(server, null);
    }

    private static void failVote(MinecraftServer server, ServerPlayer exclude) {
        clearVote();
        requestedSeed = null;
        requestedDifficulty = KEEP_DIFFICULTY;
        releaseWaiting(server, exclude);
    }

    private static void releaseWaiting(MinecraftServer server, ServerPlayer exclude) {
        if (server == null || isRunning()) return;
        for (ServerPlayer online : new ArrayList<>(server.getPlayerList().getPlayers())) {
            if (online != exclude && isPending(online) && inHaven(online)) {
                release(online, ReleaseReason.VOTE_FAILED);
            }
        }
    }

    private static void checkVoteTimeout(MinecraftServer server) {
        if (requiredPlayers == null || System.currentTimeMillis() < voteDeadline) return;

        CraftorioConsentSync.notifyTimedOut(server);
        failVote(server, null);
    }

    private static void broadcastVoteStatus(MinecraftServer server) {
        Component seedDetail = requestedSeed == null
                ? Component.translatable("misc.craftorio.consent_detail_seed_keep")
                : Component.translatable("misc.craftorio.consent_detail_seed", String.valueOf(requestedSeed));
        Component difficultyValue = requestedDifficulty == KEEP_DIFFICULTY
                ? Component.translatable("misc.craftorio.sacrifice_difficulty_keep")
                : Difficulty.byId(requestedDifficulty).getDisplayName();
        List<Component> details = List.of(seedDetail, Component.translatable("misc.craftorio.sacrifice_difficulty", difficultyValue));
        CraftorioConsentSync.broadcast(server, ConsentKind.SACRIFICE, voteProposer, requiredPlayers, consented, details, voteDeadline);
    }

    private static void acceptVote(ServerPlayer player, String seed, int difficulty) {
        MinecraftServer server = player.getServer();
        if (server == null || !isPending(player) || !inHaven(player)) return;
        if (isRunning()) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_in_progress").withStyle(ChatFormatting.RED));
            return;
        }
        if (!lifeWaived.contains(player.getUUID()) && !hasRequiredLife(player)) {
            notifyLifeRequirement(player);
            return;
        }

        List<ServerPlayer> participants;
        if (CraftorioMisc.universalBased(server.overworld())) {
            Set<UUID> onlineIds = onlineIds(server);
            if (requiredPlayers != null && !requiredPlayers.equals(onlineIds)) {
                clearVoteState(server, null);
            }
            if (requiredPlayers == null) {
                requiredPlayers = onlineIds;
                consented.clear();
                voteProposer = player.getUUID();
                voteDeadline = System.currentTimeMillis() + CraftorioConsentSync.VOTE_DURATION_MS;
                OptionalLong parsedSeed = WorldOptions.parseSeed(seed);
                requestedSeed = parsedSeed.isPresent() ? parsedSeed.getAsLong() : null;
                requestedDifficulty = difficulty;
            }

            consented.add(player.getUUID());
            if (!consented.containsAll(requiredPlayers)) {
                for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                    online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_consent_progress", consented.size(), requiredPlayers.size()).withStyle(ChatFormatting.YELLOW));
                }
                broadcastVoteStatus(server);
                return;
            }

            participants = new ArrayList<>(server.getPlayerList().getPlayers());
            clearVote();
            for (ServerPlayer participant : participants) {
                if (!isPending(participant) || !inHaven(participant)) {
                    for (ServerPlayer online : participants) {
                        online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_consent_cancelled").withStyle(ChatFormatting.RED));
                    }
                    requestedSeed = null;
                    requestedDifficulty = KEEP_DIFFICULTY;
                    releaseWaiting(server, null);
                    return;
                }
            }
        } else {
            requestedSeed = null;
            requestedDifficulty = KEEP_DIFFICULTY;
            participants = List.of(player);
        }

        Long seedChoice = requestedSeed;
        requestedSeed = null;
        int difficultyChoice = requestedDifficulty;
        requestedDifficulty = KEEP_DIFFICULTY;
        beginCountdown(server, participants, seedChoice, difficultyChoice);
    }

    private static void beginCountdown(MinecraftServer server, List<ServerPlayer> participants, Long seed, int difficulty) {
        pendingParticipants.clear();
        for (ServerPlayer participant : participants) {
            pendingParticipants.add(participant.getUUID());
        }
        pendingSeed = seed;
        pendingDifficulty = difficulty;
        stage = Stage.COUNTDOWN;
        stageTicks = 0;

        int seconds = COUNTDOWN_TICKS / 20;
        Component sacrificer = participants.size() == 1 ? participants.get(0).getDisplayName() : null;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (pendingParticipants.contains(online.getUUID())) {
                online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_countdown_self_chat", seconds).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            } else {
                warnPulled(online, sacrificer, seconds);
            }
        }
    }

    private static void warnPulled(ServerPlayer player, Component sacrificer, int seconds) {
        Component message = sacrificer != null
                ? Component.translatable("misc.craftorio.sacrifice_countdown_warning", sacrificer, seconds)
                : Component.translatable("misc.craftorio.sacrifice_countdown_warning_generic", seconds);
        player.sendSystemMessage(message.copy().withStyle(ChatFormatting.DARK_RED));
    }

    private static void tickCountdown(MinecraftServer server) {
        int remainingTicks = COUNTDOWN_TICKS - stageTicks;
        if (remainingTicks > 0) {
            if (remainingTicks % 20 == 0) {
                int seconds = remainingTicks / 20;
                for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                    String key = pendingParticipants.contains(online.getUUID()) ? "misc.craftorio.sacrifice_countdown_self" : "misc.craftorio.sacrifice_countdown_others";
                    online.displayClientMessage(Component.translatable(key, seconds).withStyle(ChatFormatting.DARK_RED), true);
                }
            }
            return;
        }

        List<ServerPlayer> participants = new ArrayList<>();
        for (UUID id : pendingParticipants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && isPending(player) && inHaven(player)) {
                participants.add(player);
            }
        }
        boolean complete = participants.size() == pendingParticipants.size();
        if (complete && CraftorioMisc.universalBased(server.overworld())) {
            for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                if (!participants.contains(online)) {
                    participants.add(online);
                }
            }
        }
        Long seed = pendingSeed;
        int difficulty = pendingDifficulty;
        pendingParticipants.clear();
        pendingSeed = null;
        pendingDifficulty = KEEP_DIFFICULTY;
        stage = Stage.IDLE;
        stageTicks = 0;

        if (!complete || participants.isEmpty()) {
            for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_countdown_cancelled").withStyle(ChatFormatting.RED));
            }
            return;
        }

        execute(server, participants, seed, true);
        chosenDifficulty = areaMode ? KEEP_DIFFICULTY : difficulty;
    }

    public static void onRosterChanged(MinecraftServer server) {
        if (requiredPlayers != null && !requiredPlayers.equals(onlineIds(server))) {
            cancelVote(server, null);
        }
    }

    private static void cancelVote(MinecraftServer server, ServerPlayer refusing) {
        if (requiredPlayers == null) return;
        clearVoteState(server, refusing);
        releaseWaiting(server, refusing);
    }

    private static void clearVoteState(MinecraftServer server, ServerPlayer refusing) {
        clearVote();
        requestedSeed = null;
        requestedDifficulty = KEEP_DIFFICULTY;
        if (server == null) return;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online != refusing) {
                online.sendSystemMessage(Component.translatable("misc.craftorio.sacrifice_consent_cancelled").withStyle(ChatFormatting.RED));
            }
        }
    }

    private static void clearVote() {
        boolean wasActive = requiredPlayers != null;
        requiredPlayers = null;
        voteProposer = null;
        consented.clear();
        if (wasActive) {
            CraftorioConsentSync.broadcastInactive(ConsentKind.SACRIFICE);
        }
    }

    private static Set<UUID> onlineIds(MinecraftServer server) {
        Set<UUID> ids = new HashSet<>();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            ids.add(online.getUUID());
        }
        return ids;
    }

    private static void execute(MinecraftServer server, List<ServerPlayer> participants, Long seed, boolean awardPoints) {
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
        chosenSeed = universal ? seed : null;

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

        if (universal && awardPoints) {
            overworld.setData(CraftorioDataAttachments.SACRIFICE_POINTS, overworld.getData(CraftorioDataAttachments.SACRIFICE_POINTS).add(SACRIFICE_POINTS_PER_SACRIFICE));
        }

        for (ServerPlayer participant : participants) {
            participantIds.add(participant.getUUID());
            if (!inHaven(participant)) {
                CraftorioHavenDimension.enterForSacrifice(participant);
            }
            if (!universal && awardPoints) {
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
        gather(player, false);
    }

    private static void gather(ServerPlayer player, boolean immediate) {
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
            if (immediate) {
                CraftorioHavenDimension.enterForSacrificeNow(player);
            } else {
                CraftorioHavenDimension.enterForSacrifice(player);
            }
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
        return isRunning() && stage != Stage.COUNTDOWN && !releasing && !destination.equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    public static void onRespawn(ServerPlayer player) {
        if (isPurging() && !releasing && !inHaven(player)) {
            CraftorioHavenDimension.enterForSacrificeNow(player);
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        if (isRunning()) {
            finish(server);
        }
        reset();
    }

    public static void reset() {
        lifeWaived.clear();
        pendingParticipants.clear();
        pendingSeed = null;
        pendingDifficulty = KEEP_DIFFICULTY;
        stage = Stage.IDLE;
        stageTicks = 0;
        releasing = false;
        requestedSeed = null;
        chosenSeed = null;
        requestedDifficulty = KEEP_DIFFICULTY;
        chosenDifficulty = KEEP_DIFFICULTY;
        areaMode = false;
        areaRects = List.of();
        forcedBackup.clear();
        participantIds.clear();
        gatheredIds.clear();
        clearVote();
    }

    public static void tick(MinecraftServer server) {
        checkVoteTimeout(server);
        if (server.getTickCount() % 20 == 0) {
            openDueTrees(server);
            if (stage == Stage.IDLE) {
                checkDeadlines(server);
            }
        }
        if (stage == Stage.IDLE) return;
        stageTicks++;

        switch (stage) {
            case COUNTDOWN -> tickCountdown(server);
            case UNLOADING -> {
                if (!everyoneInHaven(server)) {
                    stageTicks = 0;
                    return;
                }
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

    private static boolean everyoneInHaven(MinecraftServer server) {
        boolean allPresent = true;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (inHaven(online)) continue;
            allPresent = false;
            if (!CraftorioHavenDimension.isTransitioning(online)) {
                CraftorioHavenDimension.enterForSacrifice(online);
            }
        }
        return allPresent;
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

    private static boolean restartRequired() {
        return !areaMode && chosenSeed != null;
    }

    private static void startShattering(MinecraftServer server) {
        Craftorio.LOGGER.info("Sacrifice: world reset, shattering the players' screens");
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (!areaMode || participantIds.contains(online.getUUID())) {
                PacketDistributor.sendToPlayer(online, new SacrificeShatterPacket());
            }
        }
        stage = Stage.SHATTERING;
        stageTicks = 0;
    }

    private static void complete(MinecraftServer server) {
        if (!areaMode && !restartRequired()) {
            completeLive(server);
            return;
        }

        boolean halt = restartRequired();
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

    private static void completeLive(MinecraftServer server) {
        Craftorio.LOGGER.info("Sacrifice complete, resetting the world live and disconnecting everyone (the server keeps running)");

        List<ServerPlayer> online = new ArrayList<>(server.getPlayerList().getPlayers());
        finish(server);
        CraftorioWorldWipe.resetWorldStateLive(server);

        Component reason = Component.translatable("misc.craftorio.sacrifice_disconnect");
        for (ServerPlayer player : online) {
            player.setData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN, Optional.empty());
            ServerEvents.giveFreshStart(player);
            player.connection.disconnect(reason);
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
                CraftorioLoanShark.onSacrifice(online);
                resetPlayer(online);
                online.setData(CraftorioDataAttachments.SACRIFICES_APPLIED, count);
            } else if (gatheredIds.contains(online.getUUID())) {
                online.setData(CraftorioDataAttachments.SACRIFICE_WAITING, false);
                if (isPending(online)) {
                    clearPending(online);
                }
                restoreRespawn(online);
                if (areaMode) {
                    CraftorioHavenDimension.leaveHaven(online, target -> {
                        returnFromHaven(server, target, false);
                        restoreStoredItems(target);
                    });
                    online.setData(CraftorioDataAttachments.SACRIFICES_APPLIED, count);
                } else {
                    restoreStoredItems(online);
                }
            }
        }

        if (areaMode) {
            restoreForced(server);
            CraftorioWipeAreas.log(server, count, areaRects);
            for (UUID participant : participantIds) {
                CraftorioWipeAreas.revokeSharedClaims(server, participant.toString());
                CraftorioWipeAreas.forgetChunks(server, areaRects, participant.toString());
                CraftorioWipeAreas.forgetBorders(server, participant.toString());
            }
        } else {
            if (chosenDifficulty != KEEP_DIFFICULTY) {
                server.setDifficulty(Difficulty.byId(chosenDifficulty), true);
            }
            if (chosenSeed != null) {
                CraftorioWorldWipe.writeFinalizeMarker(server, chosenSeed);
            }
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

    public static boolean isPurging() {
        return isRunning() && stage != Stage.COUNTDOWN;
    }

    public static boolean isRunning() {
        return stage != Stage.IDLE;
    }

    public static void onLogin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (stage == Stage.COUNTDOWN && !pendingParticipants.contains(player.getUUID())) {
            warnPulled(player, null, Math.max(1, (COUNTDOWN_TICKS - stageTicks) / 20));
        }

        if (isRunning() && stage != Stage.COUNTDOWN) {
            if (!areaMode) {
                participantIds.add(player.getUUID());
            }

            if (participantIds.contains(player.getUUID())) {
                if (!inHaven(player)) {
                    CraftorioHavenDimension.enterForSacrificeNow(player);
                }
                setHavenRespawn(player);
            } else {
                gather(player, true);
            }
            return;
        }

        int count = server.overworld().getData(CraftorioDataAttachments.SACRIFICE_COUNT);
        int applied = player.getData(CraftorioDataAttachments.SACRIFICES_APPLIED);

        if (inHaven(player) && !isPending(player) && player.getData(CraftorioDataAttachments.SACRIFICE_SAVED_RESPAWN).isPresent()) {
            GlobalPos origin = player.getData(CraftorioDataAttachments.HAVEN_RETURN_POS);
            boolean wiped = CraftorioWipeAreas.wipedSince(server, applied, origin.dimension().location().toString(), new ChunkPos(origin.pos()));
            CraftorioHavenDimension.leaveHavenNow(player, target -> returnFromHaven(server, target, wiped));
        }
        restoreRespawn(player);
        restoreStranded(player);
        if (!inHaven(player) && !isPending(player)) {
            restoreStoredItems(player);
        }

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
        player.setData(CraftorioDataAttachments.SACRIFICE_TREE_PENDING, true);

        resetProgress(player);
        player.setData(CraftorioDataAttachments.GIVEN, false);
        player.setData(CraftorioDataAttachments.SPAWN_ORIGIN, GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO));
        CraftorioMisc.recordSpawnOrigin(player.getServer(), player.getStringUUID(), null);
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
        holder.setData(CraftorioDataAttachments.CONTRACT_OFFER_CLAIMED, true);
        holder.setData(CraftorioDataAttachments.CONTRACT_REFRESH_TIME, Craftorio.SERVER_CONFIG.CONTRACT_REFRESH_SECONDS.get() * CraftorioMisc.SECONDS_TO_TICKS);
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
        holder.setData(CraftorioDataAttachments.REWARDED_ADVANCEMENTS, new HashSet<>());
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

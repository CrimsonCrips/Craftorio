package org.crimsoncrips.craftorio.server.rebirth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.networking.skill_tree.OpenRebirthSkillTreeScreenPacket;
import org.crimsoncrips.craftorio.server.consent.CraftorioConsentSync;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CraftorioRebirthConsent {

    private static Integer pendingSkipCount = null;
    private static Set<UUID> requiredPlayers = null;
    private static final Set<UUID> consented = new HashSet<>();
    private static final Set<UUID> requesters = new HashSet<>();
    private static UUID proposer = null;
    private static long voteDeadline = 0L;

    private CraftorioRebirthConsent() {}

    public static void requestRebirth(ServerPlayer player, int skipCount) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Set<UUID> onlineIds = onlineIds(server);

        if (requiredPlayers != null && !requiredPlayers.equals(onlineIds)) {
            cancelVote(server);
        }

        if (requiredPlayers == null) {
            requiredPlayers = onlineIds;
            consented.clear();
            proposer = player.getUUID();
            pendingSkipCount = skipCount;
            voteDeadline = System.currentTimeMillis() + CraftorioConsentSync.VOTE_DURATION_MS;
        }

        consented.add(player.getUUID());
        requesters.add(player.getUUID());
        addAutoConsenters(server);
        completeIfAgreed(server, player);
    }

    public static void withdraw(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || requiredPlayers == null || !requesters.remove(player.getUUID())) return;

        if (!player.getData(CraftorioDataAttachments.AUTO_CONSENT_REBIRTH)) {
            consented.remove(player.getUUID());
        }
        CraftorioConsentSync.notifyWithdrawn(server, player);
        if (requesters.isEmpty()) {
            clearVoteState();
            return;
        }
        broadcastStatus(server);
    }

    public static void vote(ServerPlayer player, boolean agree) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        if (requiredPlayers == null) {
            CraftorioConsentSync.sendInactive(player, ConsentKind.REBIRTH);
            return;
        }

        if (agree) {
            consented.add(player.getUUID());
            requesters.add(player.getUUID());
            completeIfAgreed(server, player);
            return;
        }

        CraftorioConsentSync.notifyDeclined(server, player);
        clearVoteState();
    }

    private static void broadcastStatus(MinecraftServer server) {
        int skip = pendingSkipCount == null ? 0 : pendingSkipCount;
        List<Component> details = List.of(skip > 0
                ? Component.translatable("misc.craftorio.consent_detail_skip", skip)
                : Component.translatable("misc.craftorio.consent_detail_no_skip"));
        CraftorioConsentSync.broadcast(server, ConsentKind.REBIRTH, proposer, requiredPlayers, consented, details, voteDeadline);
    }

    public static void tick(MinecraftServer server) {
        if (requiredPlayers == null || System.currentTimeMillis() < voteDeadline) return;

        CraftorioConsentSync.notifyTimedOut(server);
        clearVoteState();
    }

    public static void onAutoConsentEnabled(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || requiredPlayers == null) return;

        addAutoConsenters(server);
        completeIfAgreed(server, player);
    }

    private static void addAutoConsenters(MinecraftServer server) {
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online.getData(CraftorioDataAttachments.AUTO_CONSENT_REBIRTH)) {
                consented.add(online.getUUID());
            }
        }
    }

    private static void completeIfAgreed(MinecraftServer server, ServerPlayer player) {
        if (!consented.containsAll(requiredPlayers)) {
            broadcastProgress(server);
            broadcastStatus(server);
            return;
        }

        int finalSkip = pendingSkipCount;
        List<ServerPlayer> online = server.getPlayerList().getPlayers();
        Set<UUID> manualVoters = new HashSet<>(requesters);
        manualVoters.add(player.getUUID());
        clearVoteState();

        boolean success = CraftorioRebirth.performRebirth(player, finalSkip);
        for (ServerPlayer p : online) {
            if (success && manualVoters.contains(p.getUUID())) {
                PacketDistributor.sendToPlayer(p, new OpenRebirthSkillTreeScreenPacket());
            } else if (success) {
                p.sendSystemMessage(Component.translatable("misc.craftorio.rebirth_auto_consent_done").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                p.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
            }
        }
    }

    public static void onRosterChanged(MinecraftServer server) {
        if (requiredPlayers == null) return;
        if (!requiredPlayers.equals(onlineIds(server))) {
            cancelVote(server);
        }
    }

    private static void cancelVote(MinecraftServer server) {
        if (requiredPlayers == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.translatable("misc.craftorio.rebirth_consent_cancelled").withStyle(ChatFormatting.RED));
        }
        clearVoteState();
    }

    private static void clearVoteState() {
        requiredPlayers = null;
        consented.clear();
        requesters.clear();
        proposer = null;
        pendingSkipCount = null;
        CraftorioConsentSync.broadcastInactive(ConsentKind.REBIRTH);
    }

    private static Set<UUID> onlineIds(MinecraftServer server) {
        Set<UUID> ids = new HashSet<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ids.add(p.getUUID());
        }
        return ids;
    }

    private static void broadcastProgress(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.translatable("misc.craftorio.rebirth_consent_progress", consented.size(), requiredPlayers.size()).withStyle(ChatFormatting.YELLOW));
        }
    }
}

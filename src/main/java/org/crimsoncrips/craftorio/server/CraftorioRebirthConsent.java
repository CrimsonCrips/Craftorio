package org.crimsoncrips.craftorio.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.networking.OpenRebirthSkillTreeScreenPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CraftorioRebirthConsent {

    private static Integer pendingSkipCount = null;
    private static Set<UUID> requiredPlayers = null;
    private static final Set<UUID> consented = new HashSet<>();

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
        }

        pendingSkipCount = skipCount;
        consented.add(player.getUUID());

        if (!consented.containsAll(requiredPlayers)) {
            broadcastProgress(server);
            return;
        }

        int finalSkip = pendingSkipCount;
        List<ServerPlayer> online = server.getPlayerList().getPlayers();
        clearVoteState();

        boolean success = CraftorioRebirth.performRebirth(player, finalSkip);
        for (ServerPlayer p : online) {
            if (success) {
                CraftorioHavenDimension.enterForRebirth(p);
                PacketDistributor.sendToPlayer(p, new OpenRebirthSkillTreeScreenPacket());
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
        pendingSkipCount = null;
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

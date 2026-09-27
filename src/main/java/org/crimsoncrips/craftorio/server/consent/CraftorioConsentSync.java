package org.crimsoncrips.craftorio.server.consent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.networking.consent.ConsentStatusPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CraftorioConsentSync {

    public static final long VOTE_DURATION_MS = 90_000L;

    private CraftorioConsentSync() {}

    public static void broadcast(MinecraftServer server, ConsentKind kind, UUID proposer, Set<UUID> required, Set<UUID> agreed, List<Component> details, long deadlineMillis) {
        if (server == null) return;

        List<String> agreedNames = new ArrayList<>();
        List<String> requiredNames = new ArrayList<>();
        for (UUID id : required) {
            String name = nameOf(server, id);
            requiredNames.add(name);
            if (agreed.contains(id)) {
                agreedNames.add(name);
            }
        }
        String proposerName = proposer == null ? "" : nameOf(server, proposer);
        PacketDistributor.sendToAllPlayers(new ConsentStatusPacket(kind, true, proposerName, agreedNames, requiredNames, details, Math.max(0L, deadlineMillis - System.currentTimeMillis())));
    }

    public static void broadcastInactive(ConsentKind kind) {
        if (ServerLifecycleHooks.getCurrentServer() == null) return;
        PacketDistributor.sendToAllPlayers(ConsentStatusPacket.inactive(kind));
    }

    public static void sendInactive(ServerPlayer player, ConsentKind kind) {
        PacketDistributor.sendToPlayer(player, ConsentStatusPacket.inactive(kind));
    }

    public static void notifyWithdrawn(MinecraftServer server, ServerPlayer withdrawing) {
        if (server == null) return;
        Component message = Component.translatable("misc.craftorio.consent_withdrawn", withdrawing.getDisplayName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online != withdrawing) {
                online.sendSystemMessage(message);
            }
        }
    }

    public static void notifyDeclined(MinecraftServer server, ServerPlayer declining) {
        if (server == null) return;
        Component message = Component.translatable("misc.craftorio.consent_declined", declining.getDisplayName()).withStyle(ChatFormatting.RED);
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            online.sendSystemMessage(message);
        }
    }

    public static void notifyTimedOut(MinecraftServer server) {
        if (server == null) return;
        Component message = Component.translatable("misc.craftorio.consent_timed_out").withStyle(ChatFormatting.RED);
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            online.sendSystemMessage(message);
        }
    }

    private static String nameOf(MinecraftServer server, UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        return player != null ? player.getGameProfile().getName() : id.toString().substring(0, 8);
    }
}

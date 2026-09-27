package org.crimsoncrips.craftorio.server.haven;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.networking.sacrifice.PlayerDissolvePacket;
import org.crimsoncrips.craftorio.networking.sacrifice.HavenTransitionPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.worldgen.CraftorioFeatures;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class CraftorioHavenDimension {

    public static final int PLATFORM_Y = 100;
    public static final int PLATFORM_RADIUS = 5;
    public static final int FALL_TELEPORT_DISTANCE = 200;
    public static final int FALL_TELEPORT_THRESHOLD = PLATFORM_Y - FALL_TELEPORT_DISTANCE;
    private static final int TRANSITION_DELAY_TICKS = 40;
    private static final int LEVITATION_AMPLIFIER = 1;

    private static final Map<UUID, PendingTransition> pendingTransitions = new HashMap<>();
    private static final Map<UUID, Integer> pendingAssembles = new HashMap<>();
    private static final int ASSEMBLE_DELAY_TICKS = 5;

    private CraftorioHavenDimension() {}

    public static void placePlatformIfNeeded(ServerLevel level) {
        if (level.getData(CraftorioDataAttachments.HAVEN_PLATFORM_PLACED)) return;

        CraftorioFeatures.HAVEN_PLATFORM.get().place(new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                level.getRandom(),
                platformCenter(),
                NoneFeatureConfiguration.INSTANCE
        ));

        level.setData(CraftorioDataAttachments.HAVEN_PLATFORM_PLACED, true);
    }

    public static BlockPos platformCenter() {
        return new BlockPos(0, PLATFORM_Y, 0);
    }

    public static void teleportBackToPlatform(ServerPlayer player) {
        BlockPos center = platformCenter();
        player.teleportTo((ServerLevel) player.level(), center.getX() + 0.5, center.getY() + FALL_TELEPORT_DISTANCE, center.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.fallDistance = 0;
        player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
    }

    public static void enterForSacrifice(ServerPlayer player) {
        if (inHaven(player) || pendingTransitions.containsKey(player.getUUID())) return;

        schedule(player, true, CraftorioHavenDimension::enterForSacrificeNow);
    }

    public static void enterForSacrificeNow(ServerPlayer player) {
        pendingTransitions.remove(player.getUUID());
        if (inHaven(player)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel havenLevel = server.getLevel(CraftorioDimensions.HAVEN_LEVEL_KEY);
        if (havenLevel == null) return;

        placePlatformIfNeeded(havenLevel);

        PacketDistributor.sendToPlayer(player, new HavenTransitionPacket(true, true));
        BlockPos center = platformCenter();
        player.teleportTo(havenLevel, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    public static void leaveHaven(ServerPlayer player, Consumer<ServerPlayer> teleport) {
        if (!inHaven(player)) {
            teleport.accept(player);
            return;
        }

        schedule(player, false, target -> leaveHavenNow(target, teleport));
    }

    public static void leaveHavenNow(ServerPlayer player, Consumer<ServerPlayer> teleport) {
        pendingTransitions.remove(player.getUUID());
        if (inHaven(player)) {
            PacketDistributor.sendToPlayer(player, new HavenTransitionPacket(true, false));
        }
        teleport.accept(player);
    }

    private static void schedule(ServerPlayer player, boolean entering, Consumer<ServerPlayer> action) {
        PacketDistributor.sendToPlayer(player, new HavenTransitionPacket(false, entering));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayerDissolvePacket(player.getId(), PlayerDissolvePacket.STAGE_START));
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, TRANSITION_DELAY_TICKS + 20, LEVITATION_AMPLIFIER, false, false, false));
        pendingTransitions.put(player.getUUID(), new PendingTransition(target -> {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new PlayerDissolvePacket(target.getId(), PlayerDissolvePacket.STAGE_SHATTER));
            target.removeEffect(MobEffects.LEVITATION);
            action.accept(target);
            target.fallDistance = 0;
            pendingAssembles.put(target.getUUID(), ASSEMBLE_DELAY_TICKS);
        }));
    }

    private static boolean inHaven(ServerPlayer player) {
        return player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY);
    }

    public static void tickPendingTransitions(MinecraftServer server) {
        tickPendingAssembles(server);
        if (pendingTransitions.isEmpty()) return;

        List<ServerPlayer> due = new ArrayList<>();
        for (Map.Entry<UUID, PendingTransition> entry : pendingTransitions.entrySet()) {
            PendingTransition transition = entry.getValue();
            transition.ticks = Math.max(0, transition.ticks - 1);
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (transition.ticks == 0 && player != null) {
                due.add(player);
            }
        }

        for (ServerPlayer player : due) {
            PendingTransition transition = pendingTransitions.remove(player.getUUID());
            if (transition != null) {
                transition.action.accept(player);
            }
        }
    }

    private static void tickPendingAssembles(MinecraftServer server) {
        if (pendingAssembles.isEmpty()) return;

        Iterator<Map.Entry<UUID, Integer>> iterator = pendingAssembles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining > 0) {
                entry.setValue(remaining);
                continue;
            }
            iterator.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayerDissolvePacket(player.getId(), PlayerDissolvePacket.STAGE_ASSEMBLE));
            }
        }
    }

    public static boolean isTransitioning(ServerPlayer player) {
        return pendingTransitions.containsKey(player.getUUID());
    }

    public static void clearPendingTransitions() {
        pendingTransitions.clear();
        pendingAssembles.clear();
    }

    private static final class PendingTransition {
        private final Consumer<ServerPlayer> action;
        private int ticks = TRANSITION_DELAY_TICKS;

        private PendingTransition(Consumer<ServerPlayer> action) {
            this.action = action;
        }
    }
}

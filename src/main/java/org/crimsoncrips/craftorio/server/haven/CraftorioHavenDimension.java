package org.crimsoncrips.craftorio.server.haven;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.crimsoncrips.craftorio.registries.CraftorioDimensions;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.worldgen.CraftorioFeatures;

import java.util.Optional;

public class CraftorioHavenDimension {

    public static final int PLATFORM_Y = 100;
    public static final int PLATFORM_RADIUS = 5;
    public static final int FALL_TELEPORT_DISTANCE = 200;
    public static final int FALL_TELEPORT_THRESHOLD = PLATFORM_Y - FALL_TELEPORT_DISTANCE;

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

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel havenLevel = server.getLevel(CraftorioDimensions.HAVEN_LEVEL_KEY);
        if (havenLevel == null) return;

        if (player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.haven_already_inside"));
            return;
        }

        GlobalPos returnPos = GlobalPos.of(player.level().dimension(), player.blockPosition());
        player.setData(CraftorioDataAttachments.HAVEN_RETURN_POS, returnPos);

        placePlatformIfNeeded(havenLevel);

        BlockPos center = platformCenter();
        player.teleportTo(havenLevel, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setRespawnPosition(CraftorioDimensions.HAVEN_LEVEL_KEY, center, 0F, true, false);
        player.sendSystemMessage(Component.translatable("misc.craftorio.haven_entered"));
    }

    public static void leave(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (!player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.haven_not_inside"));
            return;
        }

        GlobalPos returnPos = player.getData(CraftorioDataAttachments.HAVEN_RETURN_POS);
        ServerLevel returnLevel = server.getLevel(returnPos.dimension());
        if (returnLevel == null) {
            returnLevel = server.overworld();
        }

        BlockPos pos = returnPos.pos();
        player.teleportTo(returnLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setRespawnPosition(returnLevel.dimension(), pos, 0F, false, false);
        player.sendSystemMessage(Component.translatable("misc.craftorio.haven_left"));
    }

    public static void teleportBackToPlatform(ServerPlayer player) {
        BlockPos center = platformCenter();
        player.teleportTo((ServerLevel) player.level(), center.getX() + 0.5, center.getY() + FALL_TELEPORT_DISTANCE, center.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.fallDistance = 0;
        player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
    }

    public static void enterForRebirth(ServerPlayer player) {
        if (player.level().dimension().equals(CraftorioDimensions.HAVEN_LEVEL_KEY)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel havenLevel = server.getLevel(CraftorioDimensions.HAVEN_LEVEL_KEY);
        if (havenLevel == null) return;

        GlobalPos returnPos = GlobalPos.of(player.level().dimension(), player.blockPosition());
        player.setData(CraftorioDataAttachments.HAVEN_RETURN_POS, returnPos);

        placePlatformIfNeeded(havenLevel);

        BlockPos center = platformCenter();
        player.teleportTo(havenLevel, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setRespawnPosition(CraftorioDimensions.HAVEN_LEVEL_KEY, center, 0F, true, false);
    }
}

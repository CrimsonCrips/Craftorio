package org.crimsoncrips.craftorio.client.schematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.crimsoncrips.craftorio.networking.schematic.BuildBlitzPacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class BuildBlitzEffect {

    private static final float BLOCK_SCALE = 0.7F;
    private static final double MAX_ARC_HEIGHT = 8.0;

    private static final double LAUNCH_HEIGHT_OFFSET = 0.4;

    private static final class Flight {
        final int shooterId;
        Vec3 from;
        final Vec3 to;
        final BlockState state;
        final ItemStack item;
        final long start;
        final int duration;
        final float spin;
        boolean launched;

        Flight(int shooterId, BuildBlitzPacket.Shot shot, long now) {
            this.shooterId = shooterId;
            this.from = shot.from();
            this.to = Vec3.atCenterOf(shot.to());
            this.state = shot.state();
            this.item = shot.item();
            this.start = now + shot.delay();
            this.duration = Math.max(1, shot.flight());
            this.spin = (float) ((shot.to().asLong() * 31L) % 360L);
        }

        void launch(ClientLevel level) {
            if (this.launched) return;
            this.launched = true;
            Entity shooter = level.getEntity(this.shooterId);
            if (shooter != null) {
                this.from = shooter.getEyePosition().subtract(0.0, LAUNCH_HEIGHT_OFFSET, 0.0);
            }
        }

        float progress(long gameTime, float partialTick) {
            return (gameTime - this.start + partialTick) / (float) this.duration;
        }

        Vec3 position(float progress) {
            Vec3 straight = this.from.lerp(this.to, progress);
            double arcHeight = Math.min(MAX_ARC_HEIGHT, 1.0 + this.from.distanceTo(this.to) * 0.25);
            return straight.add(0.0, arcHeight * 4.0 * progress * (1.0 - progress), 0.0);
        }
    }

    private static final List<Flight> FLIGHTS = new ArrayList<>();

    private BuildBlitzEffect() {}

    public static void add(int shooterId, List<BuildBlitzPacket.Shot> shots) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        long now = level.getGameTime();
        for (BuildBlitzPacket.Shot shot : shots) {
            FLIGHTS.add(new Flight(shooterId, shot, now));
        }
    }

    public static boolean isLocalBlitzActive() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return false;
        int playerId = minecraft.player.getId();
        for (Flight flight : FLIGHTS) {
            if (flight.shooterId == playerId) return true;
        }
        return false;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || FLIGHTS.isEmpty()) return;

        long gameTime = level.getGameTime();
        boolean launchSoundPlayed = false;
        Iterator<Flight> iterator = FLIGHTS.iterator();
        while (iterator.hasNext()) {
            Flight flight = iterator.next();
            float progress = flight.progress(gameTime, 0F);
            if (progress > 1.05F) {
                iterator.remove();
                continue;
            }
            if (progress < 0F) continue;

            if (!flight.launched) {
                flight.launch(level);
                if (!launchSoundPlayed) {
                    launchSoundPlayed = true;
                    level.playLocalSound(flight.from.x, flight.from.y, flight.from.z, SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 0.4F, 1.4F, false);
                }
            }
        }
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || FLIGHTS.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;

        long gameTime = level.getGameTime();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        for (Flight flight : FLIGHTS) {
            float progress = flight.progress(gameTime, partialTick);
            if (progress < 0F || progress > 1F) continue;

            flight.launch(level);
            Vec3 position = flight.position(progress);
            int light = LevelRenderer.getLightColor(level, BlockPos.containing(position));
            float rotation = flight.spin + (gameTime + partialTick - flight.start) * 7.2F;

            poseStack.pushPose();
            poseStack.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.5F));
            poseStack.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);

            if (flight.state.getRenderShape() == RenderShape.MODEL) {
                poseStack.translate(-0.5, -0.5, -0.5);
                minecraft.getBlockRenderer().renderSingleBlock(flight.state, poseStack, buffers, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            } else if (!flight.item.isEmpty()) {
                minecraft.getItemRenderer().renderStatic(flight.item, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
            }
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FLIGHTS.clear();
    }
}

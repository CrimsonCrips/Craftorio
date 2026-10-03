package org.crimsoncrips.craftorio.server.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.crimsoncrips.craftorio.registries.CraftorioDamageTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class ChronosphereRemoval {

    private static final int MIN_REMOVAL_TICKS = 16;
    private static final int MAX_REMOVAL_TICKS = 400;
    private static final int BLOCKS_PER_TICK = 600;
    private static final int CONSUME_INTERVAL_TICKS = 20;
    private static final int REMOVE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    private static final int TIER_ATTACHED = 0;
    private static final int TIER_UNSUPPORTED = 1;
    private static final int TIER_SOLID = 2;

    private record Target(BlockPos pos, int tier, double distance) {}

    private static final class Job {
        final ServerLevel level;
        final AABB bounds;
        final List<BlockPos> order;
        final int perTick;
        int index;
        int ticksRun;

        Job(ServerLevel level, AABB bounds, List<BlockPos> order, int ticks) {
            this.level = level;
            this.bounds = bounds;
            this.order = order;
            this.perTick = Math.max(1, (int) Math.ceil(order.size() / (double) Math.max(1, ticks)));
        }
    }

    private static final List<Job> JOBS = new ArrayList<>();

    private ChronosphereRemoval() {}

    public static int removalTicks(int blockCount) {
        int ticks = (int) Math.ceil(blockCount / (double) BLOCKS_PER_TICK);
        return Math.max(MIN_REMOVAL_TICKS, Math.min(MAX_REMOVAL_TICKS, ticks));
    }

    public static void start(ServerLevel level, AABB bounds, Iterable<BlockPos> positions, int ticks) {
        JOBS.add(new Job(level, bounds, order(level, bounds, positions), ticks));
    }

    public static void tick(MinecraftServer server) {
        if (JOBS.isEmpty()) return;

        Iterator<Job> iterator = JOBS.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job.level.getServer() != server) {
                iterator.remove();
                continue;
            }
            if (job.ticksRun++ % CONSUME_INTERVAL_TICKS == 0) {
                consumePlayers(job.level, job.bounds);
            }

            int end = Math.min(job.order.size(), job.index + job.perTick);
            for (; job.index < end; job.index++) {
                remove(job.level, job.order.get(job.index));
            }
            discardDrops(job.level, job.bounds);

            if (job.index >= job.order.size()) {
                iterator.remove();
            }
        }
    }

    private static List<BlockPos> order(ServerLevel level, AABB bounds, Iterable<BlockPos> positions) {
        Vec3 center = bounds.getCenter();
        List<Target> targets = new ArrayList<>();
        for (BlockPos position : positions) {
            BlockPos pos = position.immutable();
            if (!level.isLoaded(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            targets.add(new Target(pos, tier(level, pos, state), pos.getCenter().distanceToSqr(center)));
        }

        targets.sort(Comparator.comparingInt(Target::tier)
                .thenComparing((a, b) -> a.tier() == TIER_SOLID
                        ? Double.compare(b.distance(), a.distance())
                        : Integer.compare(b.pos().getY(), a.pos().getY())));

        List<BlockPos> order = new ArrayList<>(targets.size());
        for (Target target : targets) {
            order.add(target.pos());
        }
        return order;
    }

    private static int tier(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.getFluidState().isEmpty() && !state.isSolidRender(level, pos)) return TIER_ATTACHED;
        if (state.getPistonPushReaction() == PushReaction.DESTROY) return TIER_ATTACHED;
        if (state.getCollisionShape(level, pos).isEmpty()) return TIER_ATTACHED;
        if (state.getBlock() instanceof FallingBlock) return TIER_UNSUPPORTED;
        if (!Block.isShapeFullBlock(state.getCollisionShape(level, pos))) return TIER_UNSUPPORTED;
        return TIER_SOLID;
    }

    private static void remove(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        Clearable.tryClear(level.getBlockEntity(pos));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), REMOVE_FLAGS);
    }

    private static void consumePlayers(ServerLevel level, AABB bounds) {
        DamageSource wipe = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CraftorioDamageTypes.CHRONOSPHERE));
        for (ServerPlayer inside : new ArrayList<>(level.players())) {
            if (inside.getBoundingBox().intersects(bounds)) {
                inside.hurt(wipe, Float.MAX_VALUE);
            }
        }
    }

    private static void discardDrops(ServerLevel level, AABB bounds) {
        AABB area = bounds.inflate(1.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area, entity -> entity.tickCount <= 1)) {
            item.discard();
        }
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, area, entity -> entity.tickCount <= 1)) {
            orb.discard();
        }
    }
}

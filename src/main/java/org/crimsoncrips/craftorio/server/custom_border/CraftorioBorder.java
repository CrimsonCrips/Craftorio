package org.crimsoncrips.craftorio.server.custom_border;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CraftorioBorder {

    private double centerX;
    private double centerZ;
    private double baseSize;
    private double targetSize;
    private long lerpDurationMillis;
    private transient long lerpStartTimeMillis;
    private double damagePerBlock;
    private double damageSafeZone;
    private int warningBlocks;
    private int warningTime;
    private ResourceKey<Level> dimension;

    public enum CraftorioBorderStatus {
        GROWING(0x40FF80),
        SHRINKING(0xFF3030),
        STATIONARY(0x20A0FF);

        private final int color;

        CraftorioBorderStatus(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    public static final Codec<CraftorioBorder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("centerX").forGetter(CraftorioBorder::getCenterX),
                    Codec.DOUBLE.fieldOf("centerZ").forGetter(CraftorioBorder::getCenterZ),
                    Codec.DOUBLE.fieldOf("size").forGetter(CraftorioBorder::getSize),
                    Codec.DOUBLE.fieldOf("damagePerBlock").forGetter(CraftorioBorder::getDamagePerBlock),
                    Codec.DOUBLE.fieldOf("damageSafeZone").forGetter(CraftorioBorder::getDamageSafeZone),
                    Codec.INT.fieldOf("warningBlocks").forGetter(CraftorioBorder::getWarningBlocks),
                    Codec.INT.fieldOf("warningTime").forGetter(CraftorioBorder::getWarningTime),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CraftorioBorder::getDimension),
                    Codec.DOUBLE.fieldOf("targetSize").forGetter(CraftorioBorder::getLerpTarget),
                    Codec.LONG.fieldOf("lerpTimeRemaining").forGetter(CraftorioBorder::getLerpRemainingTime)
            ).apply(instance, CraftorioBorder::new)
    );

    private static final StreamCodec<ByteBuf, ResourceKey<Level>> DIMENSION_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(
                    loc -> ResourceKey.create(Registries.DIMENSION, loc),
                    ResourceKey::location
            );

    public static final StreamCodec<ByteBuf, CraftorioBorder> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CraftorioBorder decode(ByteBuf buffer) {
            double centerX = ByteBufCodecs.DOUBLE.decode(buffer);
            double centerZ = ByteBufCodecs.DOUBLE.decode(buffer);
            double size = ByteBufCodecs.DOUBLE.decode(buffer);
            double damagePerBlock = ByteBufCodecs.DOUBLE.decode(buffer);
            double damageSafeZone = ByteBufCodecs.DOUBLE.decode(buffer);
            int warningBlocks = ByteBufCodecs.INT.decode(buffer);
            int warningTime = ByteBufCodecs.INT.decode(buffer);
            ResourceKey<Level> dimension = DIMENSION_STREAM_CODEC.decode(buffer);
            double targetSize = ByteBufCodecs.DOUBLE.decode(buffer);
            long lerpTimeRemaining = ByteBufCodecs.VAR_LONG.decode(buffer);
            return new CraftorioBorder(centerX, centerZ, size, damagePerBlock, damageSafeZone, warningBlocks, warningTime, dimension, targetSize, lerpTimeRemaining);
        }

        @Override
        public void encode(ByteBuf buffer, CraftorioBorder border) {
            ByteBufCodecs.DOUBLE.encode(buffer, border.getCenterX());
            ByteBufCodecs.DOUBLE.encode(buffer, border.getCenterZ());
            ByteBufCodecs.DOUBLE.encode(buffer, border.getSize());
            ByteBufCodecs.DOUBLE.encode(buffer, border.getDamagePerBlock());
            ByteBufCodecs.DOUBLE.encode(buffer, border.getDamageSafeZone());
            ByteBufCodecs.INT.encode(buffer, border.getWarningBlocks());
            ByteBufCodecs.INT.encode(buffer, border.getWarningTime());
            DIMENSION_STREAM_CODEC.encode(buffer, border.getDimension());
            ByteBufCodecs.DOUBLE.encode(buffer, border.getLerpTarget());
            ByteBufCodecs.VAR_LONG.encode(buffer, border.getLerpRemainingTime());
        }
    };

    public CraftorioBorder(BlockPos blockPos, double size, double damagePerBlock, double damageSafeZone, int warningBlocks, int warningTime, ResourceKey<Level> dimension) {
        this(blockPos.getX(), blockPos.getZ(), size, damagePerBlock, damageSafeZone, warningBlocks, warningTime, dimension);
    }

    public CraftorioBorder(double centerX, double centerZ, double size, double damagePerBlock, double damageSafeZone, int warningBlocks, int warningTime, ResourceKey<Level> dimension) {
        this(centerX, centerZ, size, damagePerBlock, damageSafeZone, warningBlocks, warningTime, dimension, size, 0L);
    }

    public CraftorioBorder(double centerX, double centerZ, double size, double damagePerBlock, double damageSafeZone, int warningBlocks, int warningTime, ResourceKey<Level> dimension, double targetSize, long lerpTimeRemaining) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.baseSize = size;
        this.targetSize = targetSize;
        this.lerpDurationMillis = lerpTimeRemaining;
        this.lerpStartTimeMillis = System.currentTimeMillis();
        this.damagePerBlock = damagePerBlock;
        this.damageSafeZone = damageSafeZone;
        this.warningBlocks = warningBlocks;
        this.warningTime = warningTime;
        this.dimension = dimension;
    }

    public double getSize() {
        if (lerpDurationMillis <= 0) return targetSize;
        long elapsed = System.currentTimeMillis() - lerpStartTimeMillis;
        if (elapsed >= lerpDurationMillis) return targetSize;
        double progress = elapsed / (double) lerpDurationMillis;
        return baseSize + (targetSize - baseSize) * progress;
    }

    public double getLerpTarget() {
        return targetSize;
    }

    public long getLerpRemainingTime() {
        if (lerpDurationMillis <= 0) return 0L;
        long elapsed = System.currentTimeMillis() - lerpStartTimeMillis;
        return Math.max(0L, lerpDurationMillis - elapsed);
    }

    public void lerpSizeBetween(double newTarget, long timeMillis) {
        this.baseSize = getSize();
        this.targetSize = newTarget;
        this.lerpDurationMillis = timeMillis;
        this.lerpStartTimeMillis = System.currentTimeMillis();
    }

    public void setSize(double newSize) {
        this.baseSize = newSize;
        this.targetSize = newSize;
        this.lerpDurationMillis = 0L;
        this.lerpStartTimeMillis = System.currentTimeMillis();
    }

    public CraftorioBorderStatus getStatus() {
        double current = getSize();
        if (getLerpRemainingTime() <= 0 || current == targetSize) {
            return CraftorioBorderStatus.STATIONARY;
        }
        return targetSize > current ? CraftorioBorderStatus.GROWING : CraftorioBorderStatus.SHRINKING;
    }

    public double getMinX() { return centerX - getSize() / 2.0; }
    public double getMaxX() { return centerX + getSize() / 2.0; }
    public double getMinZ() { return centerZ - getSize() / 2.0; }
    public double getMaxZ() { return centerZ + getSize() / 2.0; }

    public boolean isWithinBounds(double x, double z) {
        return x >= getMinX() && x <= getMaxX() && z >= getMinZ() && z <= getMaxZ();
    }

    public double getDistanceToBorder(double x, double z) {
        double distMinX = x - getMinX();
        double distMaxX = getMaxX() - x;
        double distMinZ = z - getMinZ();
        double distMaxZ = getMaxZ() - z;
        return Math.min(Math.min(distMinX, distMaxX), Math.min(distMinZ, distMaxZ));
    }

    public double getCenterX() { return centerX; }
    public double getCenterZ() { return centerZ; }
    public double getDamagePerBlock() { return damagePerBlock; }
    public double getDamageSafeZone() { return damageSafeZone; }
    public int getWarningBlocks() { return warningBlocks; }
    public int getWarningTime() { return warningTime; }
    public ResourceKey<Level> getDimension() { return dimension; }
}
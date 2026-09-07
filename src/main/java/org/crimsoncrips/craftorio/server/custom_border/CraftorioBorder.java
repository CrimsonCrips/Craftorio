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
    private double size;
    private double damagePerBlock;
    private double damageSafeZone;
    private int warningBlocks;
    private int warningTime;
    private ResourceKey<Level> dimension;

    public static final Codec<CraftorioBorder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("centerX").forGetter(CraftorioBorder::getCenterX),
                    Codec.DOUBLE.fieldOf("centerZ").forGetter(CraftorioBorder::getCenterZ),
                    Codec.DOUBLE.fieldOf("size").forGetter(CraftorioBorder::getSize),
                    Codec.DOUBLE.fieldOf("damagePerBlock").forGetter(CraftorioBorder::getDamagePerBlock),
                    Codec.DOUBLE.fieldOf("damageSafeZone").forGetter(CraftorioBorder::getDamageSafeZone),
                    Codec.INT.fieldOf("warningBlocks").forGetter(CraftorioBorder::getWarningBlocks),
                    Codec.INT.fieldOf("warningTime").forGetter(CraftorioBorder::getWarningTime),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CraftorioBorder::getDimension)
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
            return new CraftorioBorder(centerX, centerZ, size, damagePerBlock, damageSafeZone, warningBlocks, warningTime, dimension);
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
        }
    };

    public CraftorioBorder(BlockPos blockPos, double size, double damagePerBlock, double damageSafeZone, int warningBlocks, int warningTime, ResourceKey<Level> dimension) {
        this(blockPos.getX(),blockPos.getZ(),size,damagePerBlock,damageSafeZone,warningBlocks,warningTime,dimension);
    }

    public CraftorioBorder(double centerX, double centerZ, double size, double damagePerBlock, double damageSafeZone, int warningBlocks, int warningTime, ResourceKey<Level> dimension) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.size = size;
        this.damagePerBlock = damagePerBlock;
        this.damageSafeZone = damageSafeZone;
        this.warningBlocks = warningBlocks;
        this.warningTime = warningTime;
        this.dimension = dimension;
    }

    public double getMinX() { return centerX - size / 2.0; }
    public double getMaxX() { return centerX + size / 2.0; }
    public double getMinZ() { return centerZ - size / 2.0; }
    public double getMaxZ() { return centerZ + size / 2.0; }

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
    public double getSize() { return size; }
    public double getDamagePerBlock() { return damagePerBlock; }
    public double getDamageSafeZone() { return damageSafeZone; }
    public int getWarningBlocks() { return warningBlocks; }
    public int getWarningTime() { return warningTime; }
    public ResourceKey<Level> getDimension() { return dimension; }
}
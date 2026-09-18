package org.crimsoncrips.craftorio.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;

public class CraftorioHavenPlatformFeature extends Feature<NoneFeatureConfiguration> {

    public static final int PLATFORM_RADIUS = 5;

    public CraftorioHavenPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockState platformState = CraftorioBlocks.INVISIBLE_BLOCK.get().defaultBlockState();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -PLATFORM_RADIUS; x < PLATFORM_RADIUS; x++) {
            for (int z = -PLATFORM_RADIUS; z < PLATFORM_RADIUS; z++) {
                pos.setWithOffset(origin, x, 0, z);
                level.setBlock(pos, platformState, 3);
            }
        }

        return true;
    }
}

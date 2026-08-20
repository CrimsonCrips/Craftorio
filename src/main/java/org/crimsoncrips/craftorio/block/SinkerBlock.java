package org.crimsoncrips.craftorio.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.crimsoncrips.craftorio.block.entity.SinkerBlockEntity;

import javax.annotation.Nullable;

public class SinkerBlock extends BaseEntityBlock {
    public static final MapCodec<SinkerBlock> CODEC = simpleCodec(SinkerBlock::new);
    public static final BooleanProperty OPEN;


    public MapCodec<SinkerBlock> codec() {
        return CODEC;
    }

    public SinkerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));

    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof SinkerBlockEntity) {
                player.openMenu((SinkerBlockEntity)blockentity);
            }

            return InteractionResult.CONSUME;
        }
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof SinkerBlockEntity) {
            ((SinkerBlockEntity)blockentity).recheckOpen();
        }

    }

    protected void onRemove(BlockState p_49076_, Level p_49077_, BlockPos p_49078_, BlockState p_49079_, boolean p_49080_) {
        Containers.dropContentsOnDestroy(p_49076_, p_49079_, p_49077_, p_49078_);
        super.onRemove(p_49076_, p_49077_, p_49078_, p_49079_, p_49080_);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{OPEN});
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SinkerBlockEntity(pos, state);
    }

    static {
        OPEN = BlockStateProperties.OPEN;
    }
}
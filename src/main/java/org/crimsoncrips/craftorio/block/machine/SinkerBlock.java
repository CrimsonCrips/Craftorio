package org.crimsoncrips.craftorio.block.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.crimsoncrips.craftorio.block.blockentity.SinkerBlockEntity;

import javax.annotation.Nullable;

public class SinkerBlock extends BaseEntityBlock {
    public static final MapCodec<SinkerBlock> CODEC = simpleCodec(SinkerBlock::new);


    public MapCodec<SinkerBlock> codec() {
        return CODEC;
    }

    public SinkerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any());

    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    protected void onRemove(BlockState p_49076_, Level p_49077_, BlockPos p_49078_, BlockState p_49079_, boolean p_49080_) {
        Containers.dropContentsOnDestroy(p_49076_, p_49079_, p_49077_, p_49078_);
        super.onRemove(p_49076_, p_49077_, p_49078_, p_49079_, p_49080_);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SinkerBlockEntity(pos, state);
    }


}
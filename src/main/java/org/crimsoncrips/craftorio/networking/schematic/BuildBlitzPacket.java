package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.schematic.BuildBlitzEffect;

import java.util.List;

public record BuildBlitzPacket(int shooterId, List<Shot> shots) implements CustomPacketPayload {

    public record Shot(Vec3 from, BlockPos to, BlockState state, ItemStack item, int delay, int flight) {

        private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3 = StreamCodec.composite(
                ByteBufCodecs.DOUBLE, Vec3::x,
                ByteBufCodecs.DOUBLE, Vec3::y,
                ByteBufCodecs.DOUBLE, Vec3::z,
                Vec3::new
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Shot> STREAM_CODEC = StreamCodec.composite(
                VEC3, Shot::from,
                BlockPos.STREAM_CODEC, Shot::to,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), Shot::state,
                ItemStack.STREAM_CODEC, Shot::item,
                ByteBufCodecs.VAR_INT, Shot::delay,
                ByteBufCodecs.VAR_INT, Shot::flight,
                Shot::new
        );
    }

    public static final Type<BuildBlitzPacket> TYPE = new Type<>(Craftorio.prefix("build_blitz_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BuildBlitzPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BuildBlitzPacket::shooterId,
            Shot.STREAM_CODEC.apply(ByteBufCodecs.list()), BuildBlitzPacket::shots,
            BuildBlitzPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BuildBlitzPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BuildBlitzEffect.add(message.shooterId(), message.shots()));
    }
}

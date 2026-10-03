package org.crimsoncrips.craftorio.networking.skill_tree;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.skill_tree.SkillTreeReveal;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;

public record RevealSkillTreePacket(UpgradeTree tree) implements CustomPacketPayload {

    public static final Type<RevealSkillTreePacket> TYPE = new Type<>(Craftorio.prefix("reveal_skill_tree_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RevealSkillTreePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(UpgradeTree::byOrdinal, UpgradeTree::ordinal), RevealSkillTreePacket::tree,
            RevealSkillTreePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RevealSkillTreePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                SkillTreeReveal.markRevealed(player, message.tree());
            }
        });
    }
}

package org.crimsoncrips.craftorio.networking.skill_tree;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.skill_tree.SkillTreePurchasing;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;


public record UnlockUpgradePacket(UpgradeTree tree, ResourceLocation upgradeId) implements CustomPacketPayload {

    public static final Type<UnlockUpgradePacket> TYPE = new Type<>(Craftorio.prefix("unlock_upgrade_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockUpgradePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(UpgradeTree::byOrdinal, UpgradeTree::ordinal), UnlockUpgradePacket::tree,
            ResourceLocation.STREAM_CODEC, UnlockUpgradePacket::upgradeId,
            UnlockUpgradePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockUpgradePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            SkillTreePurchasing.Result result = SkillTreePurchasing.purchase(player, message.tree, message.upgradeId);
            if (result == SkillTreePurchasing.Result.LOCKED) {
                player.sendSystemMessage(Component.translatable("misc.craftorio.upgrade_locked_tooltip").withStyle(ChatFormatting.RED));
            } else if (result == SkillTreePurchasing.Result.TOO_EXPENSIVE) {
                PacketDistributor.sendToPlayer(player, new UnlockUpgradeFailedPacket("not_enough_points"));
            }
        });
    }
}

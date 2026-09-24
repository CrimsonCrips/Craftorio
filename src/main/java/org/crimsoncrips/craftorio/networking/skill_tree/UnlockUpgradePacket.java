package org.crimsoncrips.craftorio.networking.skill_tree;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;

public record UnlockUpgradePacket(ResourceLocation upgradeId) implements CustomPacketPayload {

    public static final Type<UnlockUpgradePacket> TYPE = new Type<>(Craftorio.prefix("unlock_upgrade_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockUpgradePacket> STREAM_CODEC = StreamCodec.composite(
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

            Registry<CraftorioUpgrade> registry = player.level().registryAccess().registryOrThrow(CraftorioUpgrade.REGISTRY_KEY);
            CraftorioUpgrade upgrade = registry.get(message.upgradeId);
            if (upgrade == null) return;

            if (CraftorioMisc.getUpgradeCount(player, message.upgradeId) >= upgrade.getMaxPurchases()) return;

            if (upgrade.getParent().isPresent() && !CraftorioMisc.hasUnlockedUpgrade(player, upgrade.getParent().get())) {
                player.sendSystemMessage(Component.translatable("misc.craftorio.upgrade_locked_tooltip").withStyle(ChatFormatting.RED));
                return;
            }

            BigInteger cost = upgrade.getCost();
            BigInteger points = CraftorioMisc.getPoints(player);
            if (points.compareTo(cost) < 0) {
                PacketDistributor.sendToPlayer(player, new UnlockUpgradeFailedPacket("not_enough_points"));
                return;
            }

            int purchaseCount = CraftorioMisc.purchaseUpgrade(player, message.upgradeId, upgrade.getMaxPurchases());
            if (purchaseCount < 0) return;

            CraftorioMisc.setPoints(points.subtract(cost), player);

            if (CraftorioMisc.universalBased(player.level())) {
                for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
                    upgrade.onUnlock(other, message.upgradeId, purchaseCount);
                }
            } else {
                upgrade.onUnlock(player, message.upgradeId, purchaseCount);
            }
        });
    }
}

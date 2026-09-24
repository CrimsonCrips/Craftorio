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

public record UnlockRebirthUpgradePacket(ResourceLocation upgradeId) implements CustomPacketPayload {

    public static final Type<UnlockRebirthUpgradePacket> TYPE = new Type<>(Craftorio.prefix("unlock_rebirth_upgrade_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockRebirthUpgradePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, UnlockRebirthUpgradePacket::upgradeId,
            UnlockRebirthUpgradePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockRebirthUpgradePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Registry<CraftorioUpgrade> registry = player.level().registryAccess().registryOrThrow(CraftorioUpgrade.REBIRTH_REGISTRY_KEY);
            CraftorioUpgrade upgrade = registry.get(message.upgradeId);
            if (upgrade == null) return;

            if (CraftorioMisc.getRebirthUpgradeCount(player, message.upgradeId) >= upgrade.getMaxPurchases()) return;

            if (upgrade.getParent().isPresent() && !CraftorioMisc.hasUnlockedRebirthUpgrade(player, upgrade.getParent().get())) {
                player.sendSystemMessage(Component.translatable("misc.craftorio.upgrade_locked_tooltip").withStyle(ChatFormatting.RED));
                return;
            }

            BigInteger cost = upgrade.getCost();
            BigInteger lifePoints = CraftorioMisc.getLifePoints(player);
            if (lifePoints.compareTo(cost) < 0) {
                PacketDistributor.sendToPlayer(player, new UnlockUpgradeFailedPacket("not_enough_points"));
                return;
            }

            int purchaseCount = CraftorioMisc.purchaseRebirthUpgrade(player, message.upgradeId, upgrade.getMaxPurchases());
            if (purchaseCount < 0) return;

            CraftorioMisc.setLifePoints(lifePoints.subtract(cost), player);

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

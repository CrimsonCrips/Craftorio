package org.crimsoncrips.craftorio.networking.skill_tree;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.skill_tree.SkillTreePurchasing;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;

public record PurchaseAllUpgradesPacket(UpgradeTree tree) implements CustomPacketPayload {

    public static final Type<PurchaseAllUpgradesPacket> TYPE = new Type<>(Craftorio.prefix("purchase_all_upgrades_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurchaseAllUpgradesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(UpgradeTree::byOrdinal, UpgradeTree::ordinal), PurchaseAllUpgradesPacket::tree,
            PurchaseAllUpgradesPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PurchaseAllUpgradesPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            int purchased = SkillTreePurchasing.purchaseAll(player, message.tree());
            player.displayClientMessage(purchased > 0
                    ? Component.translatable("misc.craftorio.purchase_all_done", purchased).withStyle(ChatFormatting.GREEN)
                    : Component.translatable("misc.craftorio.purchase_all_nothing").withStyle(ChatFormatting.YELLOW), true);
        });
    }
}

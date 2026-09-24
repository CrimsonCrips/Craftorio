package org.crimsoncrips.craftorio.networking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItem;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItemReward;

public record LoadContractIntoCreatorPacket(ResourceLocation contractId) implements CustomPacketPayload {

    public static final Type<LoadContractIntoCreatorPacket> TYPE = new Type<>(Craftorio.prefix("load_contract_into_creator_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LoadContractIntoCreatorPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, LoadContractIntoCreatorPacket::contractId,
            LoadContractIntoCreatorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LoadContractIntoCreatorPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;
            if (!(serverPlayer.containerMenu instanceof ContractCreatorMenu menu)) return;

            Registry<CraftorioContract> registry = serverPlayer.serverLevel().registryAccess().registryOrThrow(CraftorioContract.REGISTRY_KEY);
            CraftorioContract contract = registry.get(message.contractId());
            if (contract == null) return;

            Container container = menu.getContainer();
            container.clearContent();

            int tagEntries = 0;
            int overflow = 0;

            int bountyIndex = 0;
            for (CraftorioContractItem item : contract.getItemBounty()) {
                ItemStack stack = item.getStackNeeded().map(s -> new ItemStack(s.getItem()))
                        .or(() -> item.getItemNeeded().map(ItemStack::new))
                        .orElse(ItemStack.EMPTY);
                if (stack.isEmpty()) {
                    tagEntries++;
                    continue;
                }
                if (bountyIndex >= ContractCreatorMenu.BOUNTY_SLOTS) {
                    overflow++;
                    continue;
                }
                stack.setCount(item.getAmountRequired());
                container.setItem(bountyIndex++, stack);
            }

            int rewardIndex = 0;
            for (CraftorioContractItemReward reward : contract.getRewards()) {
                if (rewardIndex >= ContractCreatorMenu.REWARD_SLOTS) {
                    overflow++;
                    continue;
                }
                container.setItem(ContractCreatorMenu.BOUNTY_SLOTS + rewardIndex++, reward.getRewardingStack().copyWithCount(reward.getAmountGiving()));
            }

            if (tagEntries > 0 || overflow > 0) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_contract_load_partial", tagEntries, overflow).withStyle(ChatFormatting.YELLOW));
            }
        });
    }
}

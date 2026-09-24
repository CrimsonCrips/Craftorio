package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;

import java.util.LinkedHashMap;
import java.util.Map;

public record CopyInventoryToContractCreatorPacket() implements CustomPacketPayload {

    public static final Type<CopyInventoryToContractCreatorPacket> TYPE = new Type<>(Craftorio.prefix("copy_inventory_to_contract_creator_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CopyInventoryToContractCreatorPacket> STREAM_CODEC = StreamCodec.unit(new CopyInventoryToContractCreatorPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CopyInventoryToContractCreatorPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;
            if (!(serverPlayer.containerMenu instanceof ContractCreatorMenu menu)) return;

            Map<Item, Integer> counts = new LinkedHashMap<>();
            for (ItemStack stack : serverPlayer.getInventory().items) {
                if (stack.isEmpty()) continue;
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }

            Container container = menu.getContainer();
            for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
                int slotIndex = menu.findNextEmptySlot();
                if (slotIndex < 0) break;

                int count = Math.min(entry.getValue(), entry.getKey().getDefaultInstance().getMaxStackSize());
                container.setItem(slotIndex, new ItemStack(entry.getKey(), count));
            }
        });
    }
}

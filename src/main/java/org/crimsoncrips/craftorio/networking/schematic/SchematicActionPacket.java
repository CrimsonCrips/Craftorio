package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.schematic.ContractSchematicItem;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

public record SchematicActionPacket(InteractionHand hand, int action) implements CustomPacketPayload {

    public static final int ROTATE = 0;
    public static final int PICK_UP = 1;
    public static final int SUBMIT = 2;
    public static final int INSTA_COMPLETE = 3;

    public static final Type<SchematicActionPacket> TYPE = new Type<>(Craftorio.prefix("schematic_action_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> InteractionHand.values()[index], InteractionHand::ordinal), SchematicActionPacket::hand,
            ByteBufCodecs.VAR_INT, SchematicActionPacket::action,
            SchematicActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SchematicActionPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ItemStack stack = player.getItemInHand(message.hand());
            SchematicData data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
            if (!(stack.getItem() instanceof ContractSchematicItem) || data == null || !CraftorioSchematics.isLinked(player, data)) return;

            switch (message.action()) {
                case ROTATE -> CraftorioSchematics.place(player, stack, data, data.origin(), data.rotation().getRotated(Rotation.CLOCKWISE_90));
                case PICK_UP -> CraftorioSchematics.pickUp(player, stack, data);
                case SUBMIT -> CraftorioSchematics.submit(player, stack, data);
                case INSTA_COMPLETE -> CraftorioSchematics.instaComplete(player, data);
                default -> {
                }
            }
        });
    }
}

package org.crimsoncrips.craftorio.networking.schematic;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

public record RequestSchematicStructurePacket(ResourceLocation structure) implements CustomPacketPayload {

    public static final Type<RequestSchematicStructurePacket> TYPE = new Type<>(Craftorio.prefix("request_schematic_structure_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSchematicStructurePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RequestSchematicStructurePacket::structure,
            RequestSchematicStructurePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestSchematicStructurePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            SchematicStructurePacket.send(player, message.structure(), CraftorioSchematics.tag(player.server, message.structure()));
        });
    }
}

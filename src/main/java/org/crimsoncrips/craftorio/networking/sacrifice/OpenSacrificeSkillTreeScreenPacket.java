package org.crimsoncrips.craftorio.networking.sacrifice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

public record OpenSacrificeSkillTreeScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenSacrificeSkillTreeScreenPacket> TYPE = new Type<>(Craftorio.prefix("open_sacrifice_skill_tree_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSacrificeSkillTreeScreenPacket> STREAM_CODEC = StreamCodec.unit(new OpenSacrificeSkillTreeScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenSacrificeSkillTreeScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(ClientEvents::openSacrificeSkillTreeScreen);
    }
}

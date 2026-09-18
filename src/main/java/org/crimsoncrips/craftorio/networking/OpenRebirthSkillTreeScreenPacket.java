package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.events.ClientEvents;

public record OpenRebirthSkillTreeScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenRebirthSkillTreeScreenPacket> TYPE = new Type<>(Craftorio.prefix("open_rebirth_skill_tree_screen_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRebirthSkillTreeScreenPacket> STREAM_CODEC = StreamCodec.unit(new OpenRebirthSkillTreeScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenRebirthSkillTreeScreenPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(ClientEvents::openRebirthSkillTreeScreen);
    }
}

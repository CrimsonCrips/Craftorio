package org.crimsoncrips.craftorio.networking.consent;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.screen.consent.ClientConsentState;

import java.util.List;

public record ConsentStatusPacket(ConsentKind kind, boolean active, String proposer, List<String> agreed, List<String> required, List<Component> details, long remainingMs) implements CustomPacketPayload {

    public static final Type<ConsentStatusPacket> TYPE = new Type<>(Craftorio.prefix("consent_status_packet"));
    private static final StreamCodec<ByteBuf, List<String>> NAMES_CODEC = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Component>> DETAILS_CODEC = ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list());
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsentStatusPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ConsentKind.STREAM_CODEC.encode(buf, packet.kind());
                buf.writeBoolean(packet.active());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.proposer());
                NAMES_CODEC.encode(buf, packet.agreed());
                NAMES_CODEC.encode(buf, packet.required());
                DETAILS_CODEC.encode(buf, packet.details());
                buf.writeVarLong(packet.remainingMs());
            },
            buf -> new ConsentStatusPacket(
                    ConsentKind.STREAM_CODEC.decode(buf),
                    buf.readBoolean(),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    NAMES_CODEC.decode(buf),
                    NAMES_CODEC.decode(buf),
                    DETAILS_CODEC.decode(buf),
                    buf.readVarLong())
    );

    public static ConsentStatusPacket inactive(ConsentKind kind) {
        return new ConsentStatusPacket(kind, false, "", List.of(), List.of(), List.of(), 0L);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConsentStatusPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientConsentState.update(message));
    }
}

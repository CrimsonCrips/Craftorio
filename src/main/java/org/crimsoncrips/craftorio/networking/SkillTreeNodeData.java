package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SkillTreeNodeData(int localId, int parentLocalId, String externalParent, String category,
                                 String id, String modId, String description, String cost,
                                 String target, String operation, String value, String itemTag, String name,
                                 boolean manual, double x, double y) {

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeNodeData> STREAM_CODEC = StreamCodec.of(
            (buffer, node) -> {
                ByteBufCodecs.INT.encode(buffer, node.localId());
                ByteBufCodecs.INT.encode(buffer, node.parentLocalId());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.externalParent());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.category());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.id());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.modId());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.description());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.cost());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.target());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.operation());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.value());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.itemTag());
                ByteBufCodecs.STRING_UTF8.encode(buffer, node.name());
                ByteBufCodecs.BOOL.encode(buffer, node.manual());
                ByteBufCodecs.DOUBLE.encode(buffer, node.x());
                ByteBufCodecs.DOUBLE.encode(buffer, node.y());
            },
            buffer -> new SkillTreeNodeData(
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.DOUBLE.decode(buffer),
                    ByteBufCodecs.DOUBLE.decode(buffer)
            )
    );
}

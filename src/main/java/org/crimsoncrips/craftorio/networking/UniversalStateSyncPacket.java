package org.crimsoncrips.craftorio.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.ClientUniversalState;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public record UniversalStateSyncPacket(
        BigInteger points,
        BigInteger highestPoints,
        BigInteger tempPoints,
        long landAmount,
        Set<ResourceLocation> unlockedUpgrades,
        List<GeneralMultiplierEffect> generalEffects,
        List<TagMultiplierEffect> tagEffects,
        List<ShopMultiplierEffect> shopEffects,
        double advancementMultiplierBonus,
        List<CraftorioShipmentContract> contracts,
        List<CraftorioBorder> borders
) implements CustomPacketPayload {

    public static final Type<UniversalStateSyncPacket> TYPE = new Type<>(Craftorio.prefix("universal_state_sync_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UniversalStateSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.points());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.highestPoints());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.tempPoints());
                ByteBufCodecs.VAR_LONG.encode(buffer, packet.landAmount());
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, List.copyOf(packet.unlockedUpgrades()));
                GeneralMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.generalEffects());
                TagMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.tagEffects());
                ShopMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.shopEffects());
                ByteBufCodecs.DOUBLE.encode(buffer, packet.advancementMultiplierBonus());
                CraftorioShipmentContract.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.contracts());
                CraftorioBorder.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, packet.borders());
            },
            buffer -> {
                BigInteger points = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger highestPoints = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger tempPoints = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                long landAmount = ByteBufCodecs.VAR_LONG.decode(buffer);
                List<ResourceLocation> unlockedUpgrades = ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
                List<GeneralMultiplierEffect> generalEffects = GeneralMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                List<TagMultiplierEffect> tagEffects = TagMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                List<ShopMultiplierEffect> shopEffects = ShopMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                double advancementMultiplierBonus = ByteBufCodecs.DOUBLE.decode(buffer);
                List<CraftorioShipmentContract> contracts = CraftorioShipmentContract.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                List<CraftorioBorder> borders = CraftorioBorder.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
                return new UniversalStateSyncPacket(points, highestPoints, tempPoints, landAmount, Set.copyOf(unlockedUpgrades),
                        generalEffects, tagEffects, shopEffects, advancementMultiplierBonus, contracts, borders);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UniversalStateSyncPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientUniversalState.update(
                message.points(), message.highestPoints(), message.tempPoints(), message.landAmount(),
                message.unlockedUpgrades(), message.generalEffects(), message.tagEffects(), message.shopEffects(),
                message.advancementMultiplierBonus(), message.contracts(), message.borders()
        ));
    }
}

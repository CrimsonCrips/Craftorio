package org.crimsoncrips.craftorio.networking.sync;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.state.ClientUniversalState;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.border.CraftorioBorder;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UniversalStateSyncPacket(
        BigInteger points,
        BigInteger highestPoints,
        BigInteger tempPoints,
        long landAmount,
        Map<ResourceLocation, Integer> unlockedUpgrades,
        List<GeneralMultiplierEffect> generalEffects,
        List<TagMultiplierEffect> tagEffects,
        List<ShopMultiplierEffect> shopEffects,
        double advancementMultiplierBonus,
        List<CraftorioContract> contracts,
        List<CraftorioBorder> borders,
        int contractsCompleted,
        float highestMultiplier,
        int life,
        BigInteger lifePoints,
        Map<ResourceLocation, Integer> rebirthUpgradesUnlocked,
        BigInteger overallHighestPoints,
        int overallContractsCompleted,
        Map<ResourceLocation, Long> overallItemsSinked
) implements CustomPacketPayload {

    public static final Type<UniversalStateSyncPacket> TYPE = new Type<>(Craftorio.prefix("universal_state_sync_packet"));

    private static final Codec<Map<ResourceLocation, Long>> ITEMS_SINKED_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG);

    public static final StreamCodec<RegistryFriendlyByteBuf, UniversalStateSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.points());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.highestPoints());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.tempPoints());
                ByteBufCodecs.VAR_LONG.encode(buffer, packet.landAmount());
                ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, Integer, Map<ResourceLocation, Integer>>map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT).encode(buffer, packet.unlockedUpgrades());
                GeneralMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.generalEffects());
                TagMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.tagEffects());
                ShopMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.shopEffects());
                ByteBufCodecs.DOUBLE.encode(buffer, packet.advancementMultiplierBonus());
                CraftorioContract.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, packet.contracts());
                CraftorioBorder.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, packet.borders());
                ByteBufCodecs.VAR_INT.encode(buffer, packet.contractsCompleted());
                ByteBufCodecs.FLOAT.encode(buffer, packet.highestMultiplier());
                ByteBufCodecs.VAR_INT.encode(buffer, packet.life());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.lifePoints());
                ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, Integer, Map<ResourceLocation, Integer>>map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT).encode(buffer, packet.rebirthUpgradesUnlocked());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, packet.overallHighestPoints());
                ByteBufCodecs.VAR_INT.encode(buffer, packet.overallContractsCompleted());
                ByteBufCodecs.fromCodec(ITEMS_SINKED_CODEC).encode(buffer, packet.overallItemsSinked());
            },
            buffer -> {
                BigInteger points = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger highestPoints = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger tempPoints = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                long landAmount = ByteBufCodecs.VAR_LONG.decode(buffer);
                Map<ResourceLocation, Integer> unlockedUpgrades = ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT).decode(buffer);
                List<GeneralMultiplierEffect> generalEffects = GeneralMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                List<TagMultiplierEffect> tagEffects = TagMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                List<ShopMultiplierEffect> shopEffects = ShopMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                double advancementMultiplierBonus = ByteBufCodecs.DOUBLE.decode(buffer);
                List<CraftorioContract> contracts = CraftorioContract.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                List<CraftorioBorder> borders = CraftorioBorder.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
                int contractsCompleted = ByteBufCodecs.VAR_INT.decode(buffer);
                float highestMultiplier = ByteBufCodecs.FLOAT.decode(buffer);
                int life = ByteBufCodecs.VAR_INT.decode(buffer);
                BigInteger lifePoints = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                Map<ResourceLocation, Integer> rebirthUpgradesUnlocked = ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT).decode(buffer);
                BigInteger overallHighestPoints = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                int overallContractsCompleted = ByteBufCodecs.VAR_INT.decode(buffer);
                Map<ResourceLocation, Long> overallItemsSinked = ByteBufCodecs.fromCodec(ITEMS_SINKED_CODEC).decode(buffer);
                return new UniversalStateSyncPacket(points, highestPoints, tempPoints, landAmount, unlockedUpgrades,
                        generalEffects, tagEffects, shopEffects, advancementMultiplierBonus, contracts, borders,
                        contractsCompleted, highestMultiplier, life, lifePoints, rebirthUpgradesUnlocked,
                        overallHighestPoints, overallContractsCompleted, overallItemsSinked);
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
                message.advancementMultiplierBonus(), message.contracts(), message.borders(),
                message.contractsCompleted(), message.highestMultiplier(),
                message.life(), message.lifePoints(), message.rebirthUpgradesUnlocked(),
                message.overallHighestPoints(), message.overallContractsCompleted(), message.overallItemsSinked()
        ));
    }
}

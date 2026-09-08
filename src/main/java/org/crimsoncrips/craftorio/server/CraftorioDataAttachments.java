package org.crimsoncrips.craftorio.server;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.crimsoncrips.craftorio.CraftorioMisc.BIGINT_CODEC;


public class CraftorioDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftorio.MODID);

    public static final Supplier<AttachmentType<Long>> AMOUNT_OF_LAND = ATTACHMENT_TYPES.register(
            "amount_of_land", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).sync(ByteBufCodecs.VAR_LONG).build()
    );

    public static final Supplier<AttachmentType<List<String>>> OWNED_BY = ATTACHMENT_TYPES.register(
            "owned_by", () -> AttachmentType.<List<String>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(Codec.STRING))
                    .sync(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<BigInteger>> POINTS = ATTACHMENT_TYPES.register(
            "points", () -> AttachmentType.builder(CraftorioMisc::startingValue).serialize(BIGINT_CODEC()).sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<BigInteger>> TEMP_POINTS = ATTACHMENT_TYPES.register(
            "temp_points", () -> AttachmentType.builder(() -> BigInteger.ZERO).serialize(BIGINT_CODEC()).sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<Boolean>> CHUNK_BASED = ATTACHMENT_TYPES.register(
            "chunk_based", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> UNIVERSAL_BASED = ATTACHMENT_TYPES.register(
            "universal_based", () -> AttachmentType.builder(() -> true).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> NO_BORDERS = ATTACHMENT_TYPES.register(
            "no_borders", () -> AttachmentType.builder(() -> true).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> FINALIZED = ATTACHMENT_TYPES.register(
            "finalized", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> GIVEN = ATTACHMENT_TYPES.register(
            "given", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<List<TagMultiplierEffect>>> TAG_MULTIPLIER_EFFECTS = ATTACHMENT_TYPES.register(
            "tag_multiplier_effects", () -> AttachmentType.<List<TagMultiplierEffect>>builder((holder) -> new ArrayList<>())
                            .serialize(Codec.list(TagMultiplierEffect.CODEC))
                            .sync(TagMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()))
                            .build());

    public static final Supplier<AttachmentType<List<GeneralMultiplierEffect>>> GENERAL_MULTIPLIER_EFFECTS = ATTACHMENT_TYPES.register(
            "general_multiplier_effects", () -> AttachmentType.<List<GeneralMultiplierEffect>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(GeneralMultiplierEffect.CODEC))
                    .sync(GeneralMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<List<ShopMultiplierEffect>>> SHOP_MULTIPLIER_EFFECTS = ATTACHMENT_TYPES.register(
            "shop_multiplier_effects", () -> AttachmentType.<List<ShopMultiplierEffect>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(ShopMultiplierEffect.CODEC))
                    .sync(ShopMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<List<CraftorioShipmentContract>>> SHIPMENT_CONTRACTS = ATTACHMENT_TYPES.register(
            "shipment_contracts", () -> AttachmentType.<List<CraftorioShipmentContract>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(CraftorioShipmentContract.CODEC))
                    .sync(CraftorioShipmentContract.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<GlobalPos>> SPAWN_ORIGIN = ATTACHMENT_TYPES.register(
            "spawn_origin", () -> AttachmentType.builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
                    .serialize(GlobalPos.CODEC)
                    .sync(GlobalPos.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<AttachmentType<List<CraftorioBorder>>> PLAYER_BORDERS =
            ATTACHMENT_TYPES.register("player_borders", () ->
                    AttachmentType.<List<CraftorioBorder>>builder((holder) -> new ArrayList<>())
                            .serialize(Codec.list(CraftorioBorder.CODEC))
                            .sync(CraftorioBorder.STREAM_CODEC.apply(ByteBufCodecs.list()))
                            .build());

    private static final StreamCodec<ByteBuf, ResourceKey<Level>> DIMENSION_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(
                    loc -> ResourceKey.create(Registries.DIMENSION, loc),
                    ResourceKey::location
            );

    public static final Supplier<AttachmentType<List<ResourceKey<Level>>>> DIMENSIONS_EXPLORED =
            ATTACHMENT_TYPES.register("dimensions_explored", () ->
                    AttachmentType.<List<ResourceKey<Level>>>builder((holder) -> new ArrayList<>())
                            .serialize(Codec.list(Level.RESOURCE_KEY_CODEC))
                            .sync(DIMENSION_STREAM_CODEC.apply(ByteBufCodecs.list()))
                            .build());

}

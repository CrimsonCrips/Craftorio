package org.crimsoncrips.craftorio.server;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.contracts.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


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
            "points", () -> AttachmentType.builder(CraftorioMisc::startingValue).serialize(Codec.STRING.xmap(BigInteger::new, BigInteger::toString)).sync(ByteBufCodecs.fromCodec(Codec.STRING.xmap(BigInteger::new, BigInteger::toString))).build()
    );

    public static final Supplier<AttachmentType<BigInteger>> TEMP_POINTS = ATTACHMENT_TYPES.register(
            "temp_points", () -> AttachmentType.builder(() -> BigInteger.ZERO).serialize(Codec.STRING.xmap(BigInteger::new, BigInteger::toString)).sync(ByteBufCodecs.fromCodec(Codec.STRING.xmap(BigInteger::new, BigInteger::toString))).build()
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

    public static final Supplier<AttachmentType<List<CraftorioShipmentContract>>> CONTRACTS = ATTACHMENT_TYPES.register(
            "contracts", () -> AttachmentType.<List<CraftorioShipmentContract>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(CraftorioShipmentContract.CODEC))
                    .sync(CraftorioShipmentContract.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<GlobalPos>> SPAWN_ORIGIN = ATTACHMENT_TYPES.register(
            "spawn_origin", () -> AttachmentType.builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
                    .serialize(GlobalPos.CODEC)
                    .sync(GlobalPos.STREAM_CODEC)
                    .build()
    );

}

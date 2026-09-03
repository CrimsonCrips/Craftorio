package org.crimsoncrips.craftorio.server;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

public class CraftorioDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftorio.MODID);


    public static final Supplier<AttachmentType<Long>> AMOUNT_OF_LAND = ATTACHMENT_TYPES.register(
            "amount_of_land", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).sync(ByteBufCodecs.VAR_LONG).build()
    );

    public static final Supplier<AttachmentType<Unit>> OWNED = ATTACHMENT_TYPES.register(
            "owned", () -> AttachmentType.builder(() -> Unit.INSTANCE).sync(StreamCodec.unit(Unit.INSTANCE)).build()
    );

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

    public static final Supplier<AttachmentType<Boolean>> FINALIZED = ATTACHMENT_TYPES.register(
            "finalized", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> GIVEN = ATTACHMENT_TYPES.register(
            "given", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

}

package org.crimsoncrips.craftorio.server.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.border.CraftorioBorder;
import org.crimsoncrips.craftorio.server.sacrifice.SavedRespawn;
import org.crimsoncrips.craftorio.server.sacrifice.WipeLogEntry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.crimsoncrips.craftorio.CraftorioMisc.BIGINT_CODEC;


public class CraftorioDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftorio.MODID);

    public static final Supplier<AttachmentType<Long>> AMOUNT_OF_LAND = ATTACHMENT_TYPES.register(
            "amount_of_land", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().sync(ByteBufCodecs.VAR_LONG).build()
    );

    public static final Supplier<AttachmentType<List<String>>> OWNED_BY = ATTACHMENT_TYPES.register(
            "owned_by", () -> AttachmentType.<List<String>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(Codec.STRING))
                    .sync(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<BigInteger>> HIGHEST_REACHED_POINTS = ATTACHMENT_TYPES.register(
            "highest_reached_points", () -> AttachmentType.builder(CraftorioMisc::startingValue).serialize(BIGINT_CODEC()).copyOnDeath().sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<BigInteger>> POINTS = ATTACHMENT_TYPES.register(
            "points", () -> AttachmentType.builder(CraftorioMisc::startingValue).serialize(BIGINT_CODEC()).copyOnDeath().sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<BigInteger>> TEMP_POINTS = ATTACHMENT_TYPES.register(
            "temp_points", () -> AttachmentType.builder(() -> BigInteger.ZERO).serialize(BIGINT_CODEC()).copyOnDeath().sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
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
            "given", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<List<TagMultiplierEffect>>> TAG_MULTIPLIER_EFFECTS = ATTACHMENT_TYPES.register(
            "tag_multiplier_effects", () -> AttachmentType.<List<TagMultiplierEffect>>builder((holder) -> new ArrayList<>())
                            .serialize(Codec.list(TagMultiplierEffect.CODEC))
                            .copyOnDeath()
                            .sync(TagMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()))
                            .build());

    public static final Supplier<AttachmentType<List<GeneralMultiplierEffect>>> GENERAL_MULTIPLIER_EFFECTS = ATTACHMENT_TYPES.register(
            "general_multiplier_effects", () -> AttachmentType.<List<GeneralMultiplierEffect>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(GeneralMultiplierEffect.CODEC))
                    .copyOnDeath()
                    .sync(GeneralMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<List<ShopMultiplierEffect>>> SHOP_MULTIPLIER_EFFECTS = ATTACHMENT_TYPES.register(
            "shop_multiplier_effects", () -> AttachmentType.<List<ShopMultiplierEffect>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(ShopMultiplierEffect.CODEC))
                    .copyOnDeath()
                    .sync(ShopMultiplierEffect.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<List<CraftorioContract>>> CONTRACTS = ATTACHMENT_TYPES.register(
            "contracts", () -> AttachmentType.<List<CraftorioContract>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(CraftorioContract.CODEC))
                    .copyOnDeath()
                    .sync(CraftorioContract.CODEC_STREAM.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<AttachmentType<GlobalPos>> SPAWN_ORIGIN = ATTACHMENT_TYPES.register(
            "spawn_origin", () -> AttachmentType.builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
                    .serialize(GlobalPos.CODEC)
                    .copyOnDeath()
                    .sync(GlobalPos.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<AttachmentType<GlobalPos>> HAVEN_RETURN_POS = ATTACHMENT_TYPES.register(
            "haven_return_pos", () -> AttachmentType.builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
                    .serialize(GlobalPos.CODEC)
                    .sync(GlobalPos.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> HAVEN_PLATFORM_PLACED = ATTACHMENT_TYPES.register(
            "haven_platform_placed", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<List<CraftorioBorder>>> PLAYER_BORDERS =
            ATTACHMENT_TYPES.register("player_borders", () ->
                    AttachmentType.<List<CraftorioBorder>>builder((holder) -> new ArrayList<>())
                            .serialize(Codec.list(CraftorioBorder.CODEC))
                            .copyOnDeath()
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
                            .copyOnDeath()
                            .sync(DIMENSION_STREAM_CODEC.apply(ByteBufCodecs.list()))
                            .build());


    public static final Supplier<AttachmentType<Integer>> RANDOM_EFFECT_TIME = ATTACHMENT_TYPES.register(
            "random_effect_time", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<List<ResourceLocation>>> CONTRACT_OFFER = ATTACHMENT_TYPES.register(
            "contract_offer", () -> AttachmentType.<List<ResourceLocation>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(ResourceLocation.CODEC))
                    .build());

    public static final Supplier<AttachmentType<Boolean>> CONTRACT_OFFER_CLAIMED = ATTACHMENT_TYPES.register(
            "contract_offer_claimed", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Integer>> CONTRACT_REFRESH_TIME = ATTACHMENT_TYPES.register(
            "contract_refresh_time", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<Boolean>> UNIVERSAL_PROGRESS_STARTED = ATTACHMENT_TYPES.register(
            "universal_progress_started", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    private static final Codec<Map<ResourceLocation, Long>> ITEMS_SINKED_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG);

    public static final Supplier<AttachmentType<Map<ResourceLocation, Long>>> ITEMS_SINKED = ATTACHMENT_TYPES.register(
            "items_sinked", () -> AttachmentType.<Map<ResourceLocation, Long>>builder((holder) -> new HashMap<>())
                    .serialize(ITEMS_SINKED_CODEC)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.fromCodec(ITEMS_SINKED_CODEC))
                    .build());

    private static final Codec<Map<ResourceLocation, Integer>> UNLOCKED_UPGRADES_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT);

    public static final Supplier<AttachmentType<Map<ResourceLocation, Integer>>> UNLOCKED_UPGRADES = ATTACHMENT_TYPES.register(
            "unlocked_upgrades", () -> AttachmentType.<Map<ResourceLocation, Integer>>builder((holder) -> new HashMap<>())
                    .serialize(UNLOCKED_UPGRADES_CODEC)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.fromCodec(UNLOCKED_UPGRADES_CODEC))
                    .build());

    public static final Supplier<AttachmentType<Double>> ADVANCEMENT_MULTIPLIER_BONUS = ATTACHMENT_TYPES.register(
            "advancement_multiplier_bonus", () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).copyOnDeath().sync(ByteBufCodecs.DOUBLE).build()
    );

    public static final Supplier<AttachmentType<Integer>> CONTRACTS_COMPLETED = ATTACHMENT_TYPES.register(
            "contracts_completed", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().sync(ByteBufCodecs.VAR_INT).build()
    );

    public static final Supplier<AttachmentType<Float>> HIGHEST_MULTIPLIER = ATTACHMENT_TYPES.register(
            "highest_multiplier", () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).copyOnDeath().sync(ByteBufCodecs.FLOAT).build()
    );

    public static final Supplier<AttachmentType<Integer>> LIFE = ATTACHMENT_TYPES.register(
            "life", () -> AttachmentType.builder(() -> 1).serialize(Codec.INT).copyOnDeath().sync(ByteBufCodecs.VAR_INT).build()
    );

    public static final Supplier<AttachmentType<BigInteger>> LIFE_POINTS = ATTACHMENT_TYPES.register(
            "life_points", () -> AttachmentType.builder(() -> BigInteger.ZERO).serialize(BIGINT_CODEC()).copyOnDeath().sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<Map<ResourceLocation, Integer>>> REBIRTH_UPGRADES_UNLOCKED = ATTACHMENT_TYPES.register(
            "rebirth_upgrades_unlocked", () -> AttachmentType.<Map<ResourceLocation, Integer>>builder((holder) -> new HashMap<>())
                    .serialize(UNLOCKED_UPGRADES_CODEC)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.fromCodec(UNLOCKED_UPGRADES_CODEC))
                    .build());

    public static final Supplier<AttachmentType<BigInteger>> OVERALL_HIGHEST_POINTS = ATTACHMENT_TYPES.register(
            "overall_highest_points", () -> AttachmentType.builder(() -> BigInteger.ZERO).serialize(BIGINT_CODEC()).copyOnDeath().sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<Integer>> OVERALL_CONTRACTS_COMPLETED = ATTACHMENT_TYPES.register(
            "overall_contracts_completed", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().sync(ByteBufCodecs.VAR_INT).build()
    );

    public static final Supplier<AttachmentType<Map<ResourceLocation, Long>>> OVERALL_ITEMS_SINKED = ATTACHMENT_TYPES.register(
            "overall_items_sinked", () -> AttachmentType.<Map<ResourceLocation, Long>>builder((holder) -> new HashMap<>())
                    .serialize(ITEMS_SINKED_CODEC)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.fromCodec(ITEMS_SINKED_CODEC))
                    .build());

    public static final Supplier<AttachmentType<Boolean>> SACRIFICE_PENDING = ATTACHMENT_TYPES.register(
            "sacrifice_pending", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<List<ItemStack>>> SACRIFICE_STORED_INVENTORY = ATTACHMENT_TYPES.register(
            "sacrifice_stored_inventory", () -> AttachmentType.<List<ItemStack>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(ItemStack.OPTIONAL_CODEC))
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<Integer>> SACRIFICE_COUNT = ATTACHMENT_TYPES.register(
            "sacrifice_count", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );

    public static final Supplier<AttachmentType<Integer>> SACRIFICES_APPLIED = ATTACHMENT_TYPES.register(
            "sacrifices_applied", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<Optional<SavedRespawn>>> SACRIFICE_SAVED_RESPAWN = ATTACHMENT_TYPES.register(
            "sacrifice_saved_respawn", () -> AttachmentType.<Optional<SavedRespawn>>builder(() -> Optional.empty())
                    .serialize(SavedRespawn.OPTIONAL_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<Map<ResourceLocation, Integer>>> SACRIFICE_UPGRADES_UNLOCKED = ATTACHMENT_TYPES.register(
            "sacrifice_upgrades_unlocked", () -> AttachmentType.<Map<ResourceLocation, Integer>>builder((holder) -> new HashMap<>())
                    .serialize(UNLOCKED_UPGRADES_CODEC)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.fromCodec(UNLOCKED_UPGRADES_CODEC))
                    .build());

    public static final Supplier<AttachmentType<BigInteger>> SACRIFICE_POINTS = ATTACHMENT_TYPES.register(
            "sacrifice_points", () -> AttachmentType.builder(() -> BigInteger.ZERO).serialize(BIGINT_CODEC()).copyOnDeath().sync(ByteBufCodecs.fromCodec(BIGINT_CODEC())).build()
    );

    public static final Supplier<AttachmentType<Boolean>> AUTO_CONSENT_REBIRTH = ATTACHMENT_TYPES.register(
            "auto_consent_rebirth", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Long>> SACRIFICE_DEADLINE = ATTACHMENT_TYPES.register(
            "sacrifice_deadline", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<Long>> SACRIFICE_COOLDOWN_UNTIL = ATTACHMENT_TYPES.register(
            "sacrifice_cooldown_until", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<Boolean>> SACRIFICE_WAITING = ATTACHMENT_TYPES.register(
            "sacrifice_waiting", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build()
    );

    private static final Codec<Set<Long>> CHUNK_SET_CODEC = Codec.LONG.listOf().xmap(list -> (Set<Long>) new HashSet<>(list), set -> new ArrayList<>(set));
    private static final Codec<Map<String, Map<String, Set<Long>>>> OWNED_CHUNK_INDEX_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, CHUNK_SET_CODEC));

    public static final Supplier<AttachmentType<Map<String, Map<String, Set<Long>>>>> OWNED_CHUNK_INDEX = ATTACHMENT_TYPES.register(
            "owned_chunk_index", () -> AttachmentType.<Map<String, Map<String, Set<Long>>>>builder((holder) -> new HashMap<>())
                    .serialize(OWNED_CHUNK_INDEX_CODEC)
                    .build());

    public static final Supplier<AttachmentType<List<WipeLogEntry>>> SACRIFICE_WIPE_LOG = ATTACHMENT_TYPES.register(
            "sacrifice_wipe_log", () -> AttachmentType.<List<WipeLogEntry>>builder((holder) -> new ArrayList<>())
                    .serialize(Codec.list(WipeLogEntry.CODEC))
                    .build());

}

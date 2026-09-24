package org.crimsoncrips.craftorio.server.unlocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.sink.ItemDiscoveredPacket;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public class CraftorioUnlockedItemsManager {

    //Thank you drullkus
    private static final Codec<Set<ResourceLocation>> UNLOCKS_CODEC =
            Codec.list(ResourceLocation.CODEC)
                    .fieldOf("unlocked")
                    .<Set<ResourceLocation>>xmap(HashSet::new, ArrayList::new)
                    .codec();

    private static final String FILE_EXT = ".nbt";
    private static final String SUFFIX_OLD = "_old";

    public static final LevelResource UNLOCKS_DIR = new LevelResource(Craftorio.MODID + "/unlocked_items");

    private static final UUID UNIVERSAL_KEY = new UUID(0L, 0L);

    private final Map<UUID, Set<ResourceLocation>> cache = new HashMap<>();

    private static UUID keyFor(ServerPlayer player) {
        return CraftorioMisc.universalBased(player.level()) ? UNIVERSAL_KEY : player.getUUID();
    }

    @SubscribeEvent
    public void serverAboutToStart(ServerAboutToStartEvent event) {
        getUnlocksDir(event.getServer()).toFile().mkdirs();
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {

        this.cache.remove(event.getEntity().getUUID());
    }

    public boolean isUnlocked(ServerPlayer player, Item item) {
        return this.getUnlocked(player).contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public boolean unlock(ServerPlayer player, Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);

        Set<ResourceLocation> unlocked = this.getUnlocked(player);
        if (!unlocked.add(id)) {
            return false;
        }

        try {
            this.save(player.getServer(), keyFor(player), unlocked);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to save unlocked items for {}", player.getGameProfile().getName(), e);
        }

        PacketDistributor.sendToPlayer(player, new ItemDiscoveredPacket(id));

        return true;
    }

    public Set<ResourceLocation> getUnlocked(ServerPlayer player) {
        return this.cache.computeIfAbsent(keyFor(player), id -> this.load(player.getServer(), id));
    }

    private static @NotNull Path getUnlocksDir(MinecraftServer server) {
        return server.getWorldPath(UNLOCKS_DIR);
    }

    private static @NotNull Path getUnlocksFilePath(MinecraftServer server, UUID playerId) {
        return getUnlocksDir(server).resolve(playerId + FILE_EXT);
    }

    private static @NotNull Path getUnlocksFileOldPath(MinecraftServer server, UUID playerId) {
        return getUnlocksDir(server).resolve(playerId + SUFFIX_OLD + FILE_EXT);
    }

    private Set<ResourceLocation> load(MinecraftServer server, UUID playerId) {
        Path unlocksFile = getUnlocksFilePath(server, playerId);
        Path unlocksFileOld = getUnlocksFileOldPath(server, playerId);

        Optional<CompoundTag> nbt = this.loadNbt(unlocksFile).or(() -> this.loadNbt(unlocksFileOld));
        return nbt.map(tag -> UNLOCKS_CODEC.parse(NbtOps.INSTANCE, tag))
                .flatMap(DataResult::result)
                .orElseGet(HashSet::new);
    }

    private Optional<CompoundTag> loadNbt(Path path) {
        File file = path.toFile();
        if (file.exists() && file.isFile()) {
            try {
                return Optional.of(NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()));
            } catch (Exception exception) {
                Craftorio.LOGGER.warn("Failed to load unlocked items from {}", file.getAbsolutePath(), exception);
            }
        }

        return Optional.empty();
    }

    private void save(MinecraftServer server, UUID playerId, Set<ResourceLocation> unlocked) throws IOException {
        Optional<CompoundTag> tag = this.serialize(unlocked);

        if (tag.isEmpty()) {
            throw new IOException("Could not serialize unlocked items for " + playerId);
        }

        this.saveNbt(server, playerId, tag.get());
    }

    private Optional<CompoundTag> serialize(Set<ResourceLocation> unlocked) {
        DataResult<Tag> encodeResult = UNLOCKS_CODEC.encodeStart(NbtOps.INSTANCE, unlocked);
        return encodeResult.resultOrPartial().flatMap(tag -> tag instanceof CompoundTag cT ? Optional.of(cT) : Optional.empty());
    }

    private void saveNbt(MinecraftServer server, UUID playerId, CompoundTag tag) throws IOException {
        Path tempPath = Files.createTempFile(getUnlocksDir(server), playerId.toString(), "_temp" + FILE_EXT);

        NbtIo.writeCompressed(tag, tempPath);

        Path unlocksFilePath = getUnlocksFilePath(server, playerId);
        Path unlocksFileOldPath = getUnlocksFileOldPath(server, playerId);

        Util.safeReplaceFile(unlocksFilePath, tempPath, unlocksFileOldPath);
    }

}

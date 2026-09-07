package org.crimsoncrips.craftorio.server.unlocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public class CraftorioUnlockedItemsIO {

    private static final Codec<Set<ResourceLocation>> UNLOCKS_CODEC =
            Codec.list(ResourceLocation.CODEC)
                    .fieldOf("unlocked")
                    .<Set<ResourceLocation>>xmap(HashSet::new, ArrayList::new)
                    .codec();

    private static final String FILE_EXT = ".nbt";
    private static final String SUFFIX_OLD = "_old";

    public static final LevelResource UNLOCKS_DIR = new LevelResource(Craftorio.MODID + "/unlocked_items");

    public void mkDirs(ServerAboutToStartEvent event) {
        getUnlocksDir(event.getServer()).toFile().mkdirs();
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

    public Set<ResourceLocation> load(MinecraftServer server, UUID playerId) {
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

    public void save(MinecraftServer server, UUID playerId, Set<ResourceLocation> unlocked) throws IOException {
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

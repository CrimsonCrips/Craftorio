package org.crimsoncrips.craftorio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;


public class PollIO {

	private static final Codec<HashMap<UUID, List<String>>> POLL_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING.listOf()).xmap(HashMap::new, Function.identity());

	private static final String FILE_EXT = ".nbt";
	private static final String CSV_EXT = ".csv";
	private static final String SUFFIX_OLD = "_old";

	public static final LevelResource POLLS_DIR = new LevelResource(Craftorio.MODID);
	public static final LevelResource CSV_DIR = new LevelResource(Craftorio.MODID + "/csv-import");

	public void mkDirs(ServerAboutToStartEvent event) {
		getPollsDir(event.getServer()).toFile().mkdirs();
	}

	private static @NotNull Path getPollsDir(MinecraftServer server) {
		return server.getWorldPath(POLLS_DIR);
	}

	private static @NotNull Path getPollsFilePath(MinecraftServer server, String pollName) {
		return getPollsDir(server).resolve(pollName + FILE_EXT);
	}

	private static @NotNull Path getPollsFileOldPath(MinecraftServer server, String pollName) {
		return getPollsDir(server).resolve(pollName + SUFFIX_OLD + FILE_EXT);
	}

	public HashMap<UUID, List<String>> loadPoll(MinecraftServer server, String pollName) {
		this.guardPollName(pollName);

		Path pollsFile = getPollsFilePath(server, pollName);
		Path pollsFileOld = getPollsFileOldPath(server, pollName);

		Optional<CompoundTag> pollNbt = this.loadNbt(pollsFile).or(() -> this.loadNbt(pollsFileOld));
		return pollNbt.map(nbt -> POLL_CODEC.parse(NbtOps.INSTANCE, nbt))
				.flatMap(DataResult::result)
				.orElseGet(HashMap::new);
	}

	private Optional<CompoundTag> loadNbt(Path pollPath) {
		File pollFile = pollPath.toFile();
		if (pollFile.exists() && pollFile.isFile()) {
			try {
				return Optional.of(NbtIo.readCompressed(pollFile.toPath(), NbtAccounter.unlimitedHeap()));
			} catch (Exception exception) {
				Craftorio.LOGGER.warn("Failed to load poll data in {}", pollFile.getAbsolutePath(), exception);
			}
		}

		return Optional.empty();
	}

	public void savePoll(MinecraftServer server, String pollName, HashMap<UUID, List<String>> pollVotes) throws IOException {
		this.guardPollName(pollName);

		Optional<CompoundTag> tag = this.serializePoll(pollVotes);

		if (tag.isEmpty()) {
			throw new IOException("Could not serialize poll data for " + pollName);
		}

		this.saveNbt(server, pollName, tag.get());
	}

	private Optional<CompoundTag> serializePoll(HashMap<UUID, List<String>> pollVotes) {
		DataResult<Tag> encodeResult = POLL_CODEC.encodeStart(NbtOps.INSTANCE, pollVotes);
		return encodeResult.resultOrPartial().flatMap(tag -> tag instanceof CompoundTag cT ? Optional.of(cT) : Optional.empty());
	}

	private void saveNbt(MinecraftServer server, String pollName, CompoundTag tag) throws IOException {
		Path tempPath = Files.createTempFile(getPollsDir(server), pollName, "_temp" + FILE_EXT);

		NbtIo.writeCompressed(tag, tempPath);

		Path pollFilePath = getPollsFilePath(server, pollName);
		Path pollFileOldPath = getPollsFileOldPath(server, pollName);

		Util.safeReplaceFile(pollFilePath, tempPath, pollFileOldPath);
	}

	private void guardPollName(String pollName) {

	}

	private Path getPollMetaPath(MinecraftServer server, String suffix) {
		return getPollsDir(server).resolve("_meta" + suffix + FILE_EXT);
	}

	public PollMetaData loadPollMetaData(MinecraftServer server) {
		Path pollMetaPath = this.getPollMetaPath(server, "");
		Path pollMetaOldPath = this.getPollMetaPath(server, SUFFIX_OLD);

		Optional<CompoundTag> metaNbt = this.loadNbt(pollMetaPath).or(() -> this.loadNbt(pollMetaOldPath));
		return metaNbt.map(nbt -> PollMetaData.CODEC.parse(NbtOps.INSTANCE, nbt))
				.flatMap(DataResult::resultOrPartial)
				.orElseThrow();
	}



}
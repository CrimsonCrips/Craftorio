package org.crimsoncrips.craftorio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;


import java.io.IOException;
import java.util.*;


public class PollManager {

	
	private final PollIO pollIO;

	private PollMetaData unlockedMetaData;
	private HashMap<UUID, List<String>> unlockedCache;

	public PollManager(PollIO pollIO) {
		this.pollIO = pollIO;
	}

	public void setUnlockedItems(MinecraftServer server, UUID player, List<String> unlockingItems)  {
		List<String> choices = new ArrayList<>(new HashSet<>(unlockingItems));

		HashMap<UUID, List<String>> poll = this.getUnlockedItems(server);
		poll.put(player, choices);
		try {
			this.saveUnlockedItems(server, poll);
		} catch (IOException e) {
			Craftorio.LOGGER.error("Failed to save unlocked items", e);
		}
	}


	public void addItems(MinecraftServer server, UUID player, List<String> list)  {
		List<String> chosen = this.getUnlockedItems(server).get(player);

		ArrayList<String> copied = new ArrayList<>(chosen == null ? new ArrayList<>() : chosen);
		copied.addAll(list);

		this.setUnlockedItems(server, player, copied);
	}

	public void removeItems(MinecraftServer server, UUID player, List<String> list)  {
		List<String> chosen = this.getUnlockedItems(server).get(player);

		ArrayList<String> copied = new ArrayList<>(chosen == null ? new ArrayList<>() : chosen);
		copied.removeAll(list);

		this.setUnlockedItems(server, player, copied);
	}

	public PollMetaData getUnlockedItemsMetaData(MinecraftServer server) {
		if (this.unlockedMetaData == null) {
			this.unlockedMetaData = this.pollIO.loadPollMetaData(server);
		}
		return this.unlockedMetaData;
	}

	private HashMap<UUID, List<String>> getUnlockedItems(MinecraftServer server) {
		if (this.unlockedCache != null) {
			return this.unlockedCache;
		}

		String name = this.getUnlockedItemsMetaData(server).name();
		this.unlockedCache = this.pollIO.loadPoll(server, name);
		return this.unlockedCache;
	}
	
	private void saveUnlockedItems(MinecraftServer server, HashMap<UUID, List<String>> unlockedItems) throws IOException {
		this.pollIO.savePoll(server, this.getUnlockedItemsMetaData(server).name(), unlockedItems);
	}

}
package org.crimsoncrips.craftorio.server.unlocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.ItemDiscoveredPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class CraftorioUnlockedItemsManager {

    private static final UUID UNIVERSAL_KEY = new UUID(0L, 0L);

    private final CraftorioUnlockedItemsIO io = new CraftorioUnlockedItemsIO();
    private final Map<UUID, Set<ResourceLocation>> cache = new HashMap<>();

    private static UUID keyFor(ServerPlayer player) {
        return CraftorioMisc.universalBased(player.level()) ? UNIVERSAL_KEY : player.getUUID();
    }

    @SubscribeEvent
    public void serverAboutToStart(ServerAboutToStartEvent event) {
        this.io.mkDirs(event);
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
            this.io.save(player.getServer(), keyFor(player), unlocked);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to save unlocked items for {}", player.getGameProfile().getName(), e);
        }

        PacketDistributor.sendToPlayer(player, new ItemDiscoveredPacket(id));

        return true;
    }

    public Set<ResourceLocation> getUnlocked(ServerPlayer player) {
        return this.cache.computeIfAbsent(keyFor(player), id -> this.io.load(player.getServer(), id));
    }

}

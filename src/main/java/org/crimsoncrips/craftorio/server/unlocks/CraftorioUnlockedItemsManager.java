package org.crimsoncrips.craftorio.server.unlocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.crimsoncrips.craftorio.Craftorio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class CraftorioUnlockedItemsManager {

    private final CraftorioUnlockedItemsIO io = new CraftorioUnlockedItemsIO();
    private final Map<UUID, Set<ResourceLocation>> cache = new HashMap<>();

    @SubscribeEvent
    public void serverAboutToStart(ServerAboutToStartEvent event) {
        this.io.mkDirs(event);
    }

    @SubscribeEvent
    public void itemPickedUp(ItemEntityPickupEvent.Post event) {
        tryUnlock(event.getPlayer(), event.getOriginalStack());
    }

    @SubscribeEvent
    public void itemCrafted(PlayerEvent.ItemCraftedEvent event) {
        tryUnlock(event.getEntity(), event.getCrafting());
    }

    @SubscribeEvent
    public void itemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        tryUnlock(event.getEntity(), event.getSmelting());
    }

    @SubscribeEvent
    public void tradedWithVillager(TradeWithVillagerEvent event) {
        tryUnlock(event.getEntity(), event.getMerchantOffer().getResult());
    }

    @SubscribeEvent
    public void itemFished(ItemFishedEvent event) {
        for (ItemStack stack : event.getDrops()) {
            tryUnlock(event.getEntity(), stack);
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {

        this.cache.remove(event.getEntity().getUUID());
    }

    private void tryUnlock(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (stack.isEmpty()) return;

        this.unlock(serverPlayer, stack.getItem());
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
            this.io.save(player.getServer(), player.getUUID(), unlocked);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to save unlocked items for {}", player.getGameProfile().getName(), e);
        }

        return true;
    }

    public Set<ResourceLocation> getUnlocked(ServerPlayer player) {
        return this.cache.computeIfAbsent(player.getUUID(), id -> this.io.load(player.getServer(), id));
    }

}

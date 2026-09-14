package org.crimsoncrips.craftorio.server;

import net.minecraft.world.SimpleContainer;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CraftorioContractDraftStore {

    private static final Map<UUID, SimpleContainer> DRAFTS = new ConcurrentHashMap<>();

    public static SimpleContainer getOrCreateDraft(UUID playerId) {
        return DRAFTS.computeIfAbsent(playerId, id -> new SimpleContainer(ContractCreatorMenu.TOTAL_SLOTS));
    }

    public static void clearAll() {
        DRAFTS.clear();
    }
}

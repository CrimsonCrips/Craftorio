package org.crimsoncrips.craftorio.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class ClientUnlockedItemsState {

    private static Set<ResourceLocation> unlocked = null;

    private ClientUnlockedItemsState() {}

    public static void set(List<ResourceLocation> items) {
        unlocked = new HashSet<>(items);
    }

    public static void add(ResourceLocation item) {
        if (unlocked == null) {
            unlocked = new HashSet<>();
        }
        unlocked.add(item);
    }

    public static void clear() {
        unlocked = null;
    }

    public static boolean isAvailable() {
        return unlocked != null;
    }

    public static boolean isUnlocked(ResourceLocation item) {
        return unlocked != null && unlocked.contains(item);
    }
}

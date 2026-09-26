package org.crimsoncrips.craftorio.skill_tree;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public enum UpgradeTree {
    BASIC(CraftorioUpgrade.REGISTRY_KEY, "save", "misc.craftorio.dev_tools_tree_basic"),
    REBIRTH(CraftorioUpgrade.REBIRTH_REGISTRY_KEY, "saveRebirth", "misc.craftorio.dev_tools_tree_rebirth"),
    SACRIFICE(CraftorioUpgrade.SACRIFICE_REGISTRY_KEY, "saveSacrifice", "misc.craftorio.dev_tools_tree_sacrifice");

    private final ResourceKey<Registry<CraftorioUpgrade>> registryKey;
    private final String saveMethod;
    private final String translationKey;

    UpgradeTree(ResourceKey<Registry<CraftorioUpgrade>> registryKey, String saveMethod, String translationKey) {
        this.registryKey = registryKey;
        this.saveMethod = saveMethod;
        this.translationKey = translationKey;
    }

    public ResourceKey<Registry<CraftorioUpgrade>> registryKey() {
        return registryKey;
    }

    public String saveMethod() {
        return saveMethod;
    }

    public String translationKey() {
        return translationKey;
    }

    public static UpgradeTree byOrdinal(int ordinal) {
        UpgradeTree[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, ordinal))];
    }
}

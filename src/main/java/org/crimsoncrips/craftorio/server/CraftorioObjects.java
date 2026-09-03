package org.crimsoncrips.craftorio.server;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;


import java.util.Locale;

public class CraftorioObjects {
    public static final class Keys {
        public static final String REGISTRY_NAMESPACE = "craftorio";

       public static final ResourceKey<Registry<ItemTagBuffs>> BLOCK_TAG_BUFFS = ResourceKey.createRegistryKey(namedRegistry("block_tag_buffs"));

        public static ResourceLocation namedRegistry(String name) {
            return ResourceLocation.fromNamespaceAndPath(REGISTRY_NAMESPACE, name.toLowerCase(Locale.ROOT));
        }
    }
}

package org.crimsoncrips.craftorio.server;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftorioShopCatalog {

    private static final String ENCHANT_BOOK_PREFIX = "enchant_book/";
    private static final String POTION_PREFIX = "potion/";
    private static final String TIPPED_ARROW_PREFIX = "tipped_arrow/";

    public static ResourceLocation enchantBookKey(ResourceLocation enchantmentId) {
        return ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, ENCHANT_BOOK_PREFIX + enchantmentId.getNamespace() + "/" + enchantmentId.getPath());
    }

    public static ResourceLocation potionKey(ResourceLocation potionId) {
        return ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, POTION_PREFIX + potionId.getNamespace() + "/" + potionId.getPath());
    }

    public static ResourceLocation tippedArrowKey(ResourceLocation potionId) {
        return ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, TIPPED_ARROW_PREFIX + potionId.getNamespace() + "/" + potionId.getPath());
    }

    public static Optional<ItemStack> resolve(RegistryAccess registryAccess, ResourceLocation key) {
        if (key.getNamespace().equals(Craftorio.MODID) && key.getPath().startsWith(ENCHANT_BOOK_PREFIX)) {
            ResourceLocation enchantmentId = parseEmbeddedId(key.getPath().substring(ENCHANT_BOOK_PREFIX.length()));
            return registryAccess.registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantmentId)
                    .map(holder -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, holder.value().getMaxLevel())));
        }

        if (key.getNamespace().equals(Craftorio.MODID) && key.getPath().startsWith(POTION_PREFIX)) {
            ResourceLocation potionId = parseEmbeddedId(key.getPath().substring(POTION_PREFIX.length()));
            return registryAccess.registryOrThrow(Registries.POTION).getHolder(potionId)
                    .map(holder -> PotionContents.createItemStack(Items.POTION, holder));
        }

        if (key.getNamespace().equals(Craftorio.MODID) && key.getPath().startsWith(TIPPED_ARROW_PREFIX)) {
            ResourceLocation potionId = parseEmbeddedId(key.getPath().substring(TIPPED_ARROW_PREFIX.length()));
            return registryAccess.registryOrThrow(Registries.POTION).getHolder(potionId)
                    .map(holder -> PotionContents.createItemStack(Items.TIPPED_ARROW, holder));
        }

        return BuiltInRegistries.ITEM.getOptional(key).map(ItemStack::new);
    }

    public static boolean isVariantKey(ResourceLocation key) {
        return key.getNamespace().equals(Craftorio.MODID)
                && (key.getPath().startsWith(ENCHANT_BOOK_PREFIX) || key.getPath().startsWith(POTION_PREFIX) || key.getPath().startsWith(TIPPED_ARROW_PREFIX));
    }

    private static ResourceLocation parseEmbeddedId(String remainder) {
        int slash = remainder.indexOf('/');
        return ResourceLocation.fromNamespaceAndPath(remainder.substring(0, slash), remainder.substring(slash + 1));
    }

    public static List<ResourceLocation> allEnchantBookKeys(RegistryAccess registryAccess) {
        List<ResourceLocation> keys = new ArrayList<>();
        for (ResourceLocation id : registryAccess.registryOrThrow(Registries.ENCHANTMENT).keySet()) {
            keys.add(enchantBookKey(id));
        }
        return keys;
    }

    public static List<ResourceLocation> allPotionKeys(RegistryAccess registryAccess) {
        List<ResourceLocation> keys = new ArrayList<>();
        for (ResourceLocation id : registryAccess.registryOrThrow(Registries.POTION).keySet()) {
            keys.add(potionKey(id));
        }
        return keys;
    }

    public static List<ResourceLocation> allTippedArrowKeys(RegistryAccess registryAccess) {
        List<ResourceLocation> keys = new ArrayList<>();
        for (ResourceLocation id : registryAccess.registryOrThrow(Registries.POTION).keySet()) {
            keys.add(tippedArrowKey(id));
        }
        return keys;
    }

    public record CatalogEntry(ResourceLocation key, ItemStack stack) {}

    public static List<CatalogEntry> buildFullCatalog(RegistryAccess registryAccess) {
        List<CatalogEntry> entries = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (item.builtInRegistryHolder().is(org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen.SHOP_BLACKLIST)) continue;
            entries.add(new CatalogEntry(BuiltInRegistries.ITEM.getKey(item), new ItemStack(item)));
        }

        for (ResourceLocation key : allEnchantBookKeys(registryAccess)) {
            resolve(registryAccess, key).ifPresent(stack -> entries.add(new CatalogEntry(key, stack)));
        }
        for (ResourceLocation key : allPotionKeys(registryAccess)) {
            resolve(registryAccess, key).ifPresent(stack -> entries.add(new CatalogEntry(key, stack)));
        }
        for (ResourceLocation key : allTippedArrowKeys(registryAccess)) {
            resolve(registryAccess, key).ifPresent(stack -> entries.add(new CatalogEntry(key, stack)));
        }

        return entries;
    }
}

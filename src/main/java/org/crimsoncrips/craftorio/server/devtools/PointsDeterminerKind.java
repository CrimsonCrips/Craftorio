package org.crimsoncrips.craftorio.server.devtools;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;

import java.math.BigDecimal;

public enum PointsDeterminerKind {
    ITEM("item", "points_determiner_tab_items", Registries.ITEM, CraftorioDataMaps.POINT_VALUE, "Item", "POINT_VALUE", true),
    ENCHANTMENT("enchantment", "points_determiner_tab_enchantments", Registries.ENCHANTMENT, CraftorioDataMaps.ENCHANTMENT_POINT_VALUE, "Enchantment", "ENCHANTMENT_POINT_VALUE", false),
    EFFECT("effect", "points_determiner_tab_effects", Registries.MOB_EFFECT, CraftorioDataMaps.EFFECT_POINT_VALUE, "MobEffect", "EFFECT_POINT_VALUE", false),
    ADVANCEMENT("advancement", "points_determiner_tab_advancements", Registries.ADVANCEMENT, CraftorioDataMaps.ADVANCEMENT_POINT_VALUE, "Advancement", "ADVANCEMENT_POINT_VALUE", false),
    ADVANCEMENT_MULTIPLIER("advancement_multiplier", "points_determiner_tab_advancement_multipliers", Registries.ADVANCEMENT, CraftorioDataMaps.ADVANCEMENT_MULTIPLIER_VALUE, "Advancement", "ADVANCEMENT_MULTIPLIER_VALUE", true);

    public static final StreamCodec<ByteBuf, PointsDeterminerKind> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], Enum::ordinal);

    private final String id;
    private final String langKey;
    private final ResourceKey<? extends Registry<?>> registry;
    private final DataMapType<?, ?> dataMap;
    private final String typeName;
    private final String fieldName;
    private final boolean decimals;

    PointsDeterminerKind(String id, String langKey, ResourceKey<? extends Registry<?>> registry, DataMapType<?, ?> dataMap, String typeName, String fieldName, boolean decimals) {
        this.id = id;
        this.langKey = langKey;
        this.registry = registry;
        this.dataMap = dataMap;
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.decimals = decimals;
    }

    public String id() {
        return this.id;
    }

    public String langKey() {
        return this.langKey;
    }

    public ResourceKey<? extends Registry<?>> registry() {
        return this.registry;
    }

    public String typeName() {
        return this.typeName;
    }

    public String fieldName() {
        return this.fieldName;
    }

    public boolean isAdvancement() {
        return this.registry == Registries.ADVANCEMENT;
    }

    public ResourceLocation dataMapFile() {
        ResourceLocation registryId = this.registry.location();
        String folder = registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
                ? registryId.getPath()
                : registryId.getNamespace() + "/" + registryId.getPath();
        ResourceLocation mapId = this.dataMap.id();
        return ResourceLocation.fromNamespaceAndPath(mapId.getNamespace(), "data_maps/" + folder + "/" + mapId.getPath() + ".json");
    }

    public boolean isValidValue(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            if (parsed.signum() < 0) return false;
            return this.decimals || parsed.stripTrailingZeros().scale() <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

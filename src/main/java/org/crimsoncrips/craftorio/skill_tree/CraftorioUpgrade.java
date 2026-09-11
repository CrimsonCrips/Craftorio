package org.crimsoncrips.craftorio.skill_tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.CraftorioRegistries;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

public abstract class CraftorioUpgrade {

    private final String name;
    private final ResourceLocation icon;
    private final ResourceLocation parent;
    private final String description;
    private final BigInteger cost;

    public static final ResourceKey<Registry<MapCodec<? extends CraftorioUpgrade>>> TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "craftorio_upgrade_type"));

    public static final ResourceKey<Registry<CraftorioUpgrade>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "upgrade"));

    private static Codec<CraftorioUpgrade> codecInstance;

    public static Codec<CraftorioUpgrade> dispatchCodec() {
        if (codecInstance == null) {
            codecInstance = CraftorioRegistries.UPGRADE_TYPE_REGISTRY.byNameCodec()
                    .dispatch(CraftorioUpgrade::codec, mapCodec -> mapCodec);
        }
        return codecInstance;
    }

    public abstract MapCodec<? extends CraftorioUpgrade> codec();

    protected CraftorioUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        this.name = name;
        this.icon = icon;
        this.parent = parent;
        this.description = description;
        this.cost = cost;
    }

    public String getNameKey() {
        return name;
    }

    public String getActualName() {
        return Component.translatable(name).getString();
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public Optional<ResourceLocation> getParent() {
        return Optional.ofNullable(parent);
    }

    public String getDescriptionKey() {
        return description;
    }

    public String getActualDescription() {
        return Component.translatable(description).getString();
    }

    public BigInteger getCost() {
        return cost;
    }

    public void onUnlock(ServerPlayer player, ResourceLocation id) {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private ResourceLocation icon;
        private ResourceLocation parent;
        private String description;
        private BigInteger cost = BigInteger.ZERO;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder cost(BigInteger cost) {
            this.cost = cost;
            return this;
        }

        public Builder cost(long cost) {
            this.cost = BigInteger.valueOf(cost);
            return this;
        }

        public Builder parent(Holder.Reference<CraftorioUpgrade> parent) {
            this.parent = parent.key().location();
            return this;
        }

        public Builder parent(ResourceLocation parent) {
            this.parent = parent;
            return this;
        }

        public String getName() {
            return name;
        }

        public ResourceLocation getIcon() {
            return icon;
        }

        public ResourceLocation getParent() {
            return parent;
        }

        public String getDescription() {
            return description;
        }

        public BigInteger getCost() {
            return cost;
        }

        public Holder.Reference<CraftorioUpgrade> save(BootstrapContext<CraftorioUpgrade> context, ResourceLocation id, Function<Builder, ? extends CraftorioUpgrade> factory) {
            ResourceKey<CraftorioUpgrade> key = ResourceKey.create(REGISTRY_KEY, id);
            return context.register(key, factory.apply(this));
        }
    }
}

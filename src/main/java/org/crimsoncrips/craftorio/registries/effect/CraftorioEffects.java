package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioRegistries;

import java.util.ArrayList;
import java.util.List;

public abstract class CraftorioEffects {

    private String name;
    private int time;

    public static final ResourceKey<Registry<MapCodec<? extends CraftorioEffects>>> TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "craftorio_effect_type"));

    public static final ResourceKey<Registry<CraftorioEffects>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "effect"));

    private static Codec<CraftorioEffects> codecInstance;

    public static Codec<CraftorioEffects> dispatchCodec() {
        if (codecInstance == null) {
            codecInstance = CraftorioRegistries.TYPE_REGISTRY.byNameCodec()
                    .dispatch(CraftorioEffects::codec, mapCodec -> mapCodec);
        }
        return codecInstance;
    }

    public abstract MapCodec<? extends CraftorioEffects> codec();

    public abstract CraftorioEffects copy();

    public CraftorioEffects(String name, int time){
        this.name = name;
        this.time = time;
    }

    public String getNameKey(){
        return name;
    }

    public String getActualName(){
        return Component.translatable(name).getString();
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getTime() {
        return time;
    }
    public void setTime(int time) {
        this.time = time;
    }

    public void tick(Player player){
        setTime(time - 1);

        if (shouldEnd()) {
            Level level = player.level();
            if (this instanceof TagMultiplierEffect) {
                List<TagMultiplierEffect> effects = new ArrayList<>(CraftorioMisc.getTagEffects(player));
                effects.remove(this);
                CraftorioMisc.setTagEffects(player,effects);
            }
            if (this instanceof GeneralMultiplierEffect) {
                List<GeneralMultiplierEffect> effects = new ArrayList<>(CraftorioMisc.getGeneralEffects(player));
                effects.remove(this);
                CraftorioMisc.setGeneralEffects(player,effects);
            }
            if (this instanceof ShopMultiplierEffect) {
                List<ShopMultiplierEffect> effects = new ArrayList<>(CraftorioMisc.getShopEffects(player));
                effects.remove(this);
                CraftorioMisc.setShopEffects(player,effects);
            }
        }
    }

    public boolean shouldEnd(){
        return time <= 0;
    }

}
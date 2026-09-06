package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioRegistries;

import java.util.List;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.GENERAL_MULTIPLIER_EFFECTS;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.TAG_MULTIPLIER_EFFECTS;

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

    public CraftorioEffects(String name, int time){
        this.name = name;
        this.time = time;
    }

    public String getName(){
        return name;
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
            if (this instanceof TagMultiplierEffect) {
                List<TagMultiplierEffect> effects = CraftorioMisc.getTagEffects(player);
                effects.remove(this);
                player.setData(TAG_MULTIPLIER_EFFECTS, effects);
            }
            if (this instanceof GeneralMultiplierEffect) {
                List<GeneralMultiplierEffect> effects = CraftorioMisc.getGeneralEffects(player);
                effects.remove(this);
                player.setData(GENERAL_MULTIPLIER_EFFECTS, CraftorioMisc.getGeneralEffects(player));
            }
        }
    }
    public boolean shouldEnd(){ return time <= 0; }
}
package org.crimsoncrips.craftorio.registries.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioRegistries;


public abstract class CraftorioEffects {

    private String name;
    private int time;
    private ResourceLocation icon;
    private int weight;
    private boolean unobtainable;
    private boolean loanMarked;
    private EffectOperation operation = EffectOperation.ADD;

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

    public abstract float getMultiplier();

    public EffectOperation getOperation() {
        return operation;
    }

    public void setOperation(EffectOperation operation) {
        this.operation = operation == null ? EffectOperation.ADD : operation;
    }

    public CraftorioEffects withOperation(EffectOperation operation) {
        setOperation(operation);
        return this;
    }

    public double additiveContribution() {
        return operation.additive(getMultiplier());
    }

    public double factorContribution() {
        return operation.factor(getMultiplier());
    }

    public boolean isNegativeEffect() {
        return operation.isNegative(getMultiplier());
    }

    public CraftorioEffects(String name, int time, ResourceLocation icon, int weight, boolean unobtainable){
        this.name = name;
        this.time = time;
        this.icon = icon;
        this.weight = weight;
        this.unobtainable = unobtainable;
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

    public ResourceLocation getIcon() {
        return icon;
    }

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isUnobtainable() {
        return unobtainable;
    }

    public void setUnobtainable(boolean unobtainable) {
        this.unobtainable = unobtainable;
    }

    public boolean isLoanMarked() {
        return loanMarked;
    }

    public void setLoanMarked(boolean loanMarked) {
        this.loanMarked = loanMarked;
    }

    public void tick(Player player){
        if (loanMarked) return;
        setTime(time - 1);

        if (shouldEnd()) {
            CraftorioMisc.removeEffectsIf(player, effect -> effect == this);
        }
    }

    public boolean shouldEnd(){
        return time <= 0;
    }

}
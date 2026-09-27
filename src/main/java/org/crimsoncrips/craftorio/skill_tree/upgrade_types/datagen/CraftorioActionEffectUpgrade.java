package org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.crimsoncrips.craftorio.skill_tree.target.PlayerActionTarget;

import java.math.BigInteger;

public class CraftorioActionEffectUpgrade extends CraftorioUpgrade {

    private final PlayerActionTarget target;
    private final ResourceLocation effect;
    private final double chancePerPurchase;

    public static final Codec<PlayerActionTarget> TARGET_CODEC = Codec.STRING.xmap(
            s -> PlayerActionTarget.valueOf(s.toUpperCase()), Enum::name);

    public static final MapCodec<CraftorioActionEffectUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CraftorioActionEffectUpgrade::getNameKey),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(CraftorioActionEffectUpgrade::getIcon),
                    ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(CraftorioActionEffectUpgrade::getParent),
                    Codec.STRING.fieldOf("description").forGetter(CraftorioActionEffectUpgrade::getDescriptionKey),
                    CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(CraftorioActionEffectUpgrade::getCost),
                    TARGET_CODEC.fieldOf("target").forGetter(CraftorioActionEffectUpgrade::getTarget),
                    ResourceLocation.CODEC.fieldOf("effect").forGetter(CraftorioActionEffectUpgrade::getEffect),
                    Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(CraftorioActionEffectUpgrade::getChancePerPurchase),
                    Codec.INT.optionalFieldOf("max_purchases", 1).forGetter(CraftorioActionEffectUpgrade::getMaxPurchases),
                    Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(CraftorioActionEffectUpgrade::getX),
                    Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(CraftorioActionEffectUpgrade::getY)
            ).apply(instance, (name, icon, parent, description, cost, target, effect, chance, maxPurchases, x, y) -> {
                    CraftorioActionEffectUpgrade upgrade = new CraftorioActionEffectUpgrade(name, icon, parent.orElse(null), description, cost, target, effect, chance, maxPurchases);
                    upgrade.setPosition(x, y);
                    return upgrade;
            })
    );

    public CraftorioActionEffectUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost,
                                         PlayerActionTarget target, ResourceLocation effect, double chancePerPurchase, int maxPurchases) {
        super(name, icon, parent, description, cost, maxPurchases);
        this.target = target;
        this.effect = effect;
        this.chancePerPurchase = chancePerPurchase;
    }

    public PlayerActionTarget getTarget() {
        return target;
    }

    public ResourceLocation getEffect() {
        return effect;
    }

    public double getChancePerPurchase() {
        return chancePerPurchase;
    }

    public double chanceFor(int purchaseCount) {
        return Math.min(1.0, Math.max(0.0, chancePerPurchase * purchaseCount));
    }

    public static CraftorioActionEffectUpgrade of(CraftorioUpgrade.Builder builder, PlayerActionTarget target, ResourceLocation effect, double chancePerPurchase) {
        return new CraftorioActionEffectUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost(),
                target, effect, chancePerPurchase, builder.getMaxPurchases());
    }

    public static CraftorioActionEffectUpgrade of(CraftorioUpgrade.Builder builder, PlayerActionTarget target, ResourceLocation effect) {
        return of(builder, target, effect, 1.0);
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.ACTION_EFFECT.get();
    }
}

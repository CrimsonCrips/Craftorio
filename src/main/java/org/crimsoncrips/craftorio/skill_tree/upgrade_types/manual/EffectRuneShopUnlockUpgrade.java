package org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;

import java.math.BigInteger;

public class EffectRuneShopUnlockUpgrade extends ActionUpgrade {

    public static final MapCodec<EffectRuneShopUnlockUpgrade> CODEC = Common.CODEC.xmap(
            c -> {
                EffectRuneShopUnlockUpgrade upgrade = new EffectRuneShopUnlockUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost());
                upgrade.setPosition(c.x(), c.y());
                return upgrade;
            },
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost(), u.getX(), u.getY())
    );

    public EffectRuneShopUnlockUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static EffectRuneShopUnlockUpgrade of(CraftorioUpgrade.Builder builder) {
        return new EffectRuneShopUnlockUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.EFFECT_RUNE_SHOP_UNLOCK.get();
    }
}

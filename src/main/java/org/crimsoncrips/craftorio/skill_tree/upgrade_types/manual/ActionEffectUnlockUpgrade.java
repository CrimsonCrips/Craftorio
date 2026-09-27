package org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;

import java.math.BigInteger;

public class ActionEffectUnlockUpgrade extends ActionUpgrade {

    public static final MapCodec<ActionEffectUnlockUpgrade> CODEC = Common.CODEC.xmap(
            c -> {
                ActionEffectUnlockUpgrade upgrade = new ActionEffectUnlockUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost());
                upgrade.setPosition(c.x(), c.y());
                return upgrade;
            },
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost(), u.getX(), u.getY())
    );

    public ActionEffectUnlockUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static ActionEffectUnlockUpgrade of(CraftorioUpgrade.Builder builder) {
        return new ActionEffectUnlockUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.ACTION_EFFECT_UNLOCK.get();
    }
}

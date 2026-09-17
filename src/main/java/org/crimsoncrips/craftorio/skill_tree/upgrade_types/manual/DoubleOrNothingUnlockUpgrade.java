package org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;

import java.math.BigInteger;

public class DoubleOrNothingUnlockUpgrade extends ActionUpgrade {

    public static final MapCodec<DoubleOrNothingUnlockUpgrade> CODEC = Common.CODEC.xmap(
            c -> {
                DoubleOrNothingUnlockUpgrade upgrade = new DoubleOrNothingUnlockUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost());
                upgrade.setPosition(c.x(), c.y());
                return upgrade;
            },
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost(), u.getX(), u.getY())
    );

    public DoubleOrNothingUnlockUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static DoubleOrNothingUnlockUpgrade of(CraftorioUpgrade.Builder builder) {
        return new DoubleOrNothingUnlockUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.DOUBLE_OR_NOTHING_UNLOCK.get();
    }
}

package org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;

import java.math.BigInteger;

public class SinkValueScalingUpgrade extends ActionUpgrade {

    public static final MapCodec<SinkValueScalingUpgrade> CODEC = Common.CODEC.xmap(
            c -> new SinkValueScalingUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost()),
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost())
    );

    public SinkValueScalingUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static SinkValueScalingUpgrade of(CraftorioUpgrade.Builder builder) {
        return new SinkValueScalingUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.SINK_VALUE_SCALING.get();
    }
}

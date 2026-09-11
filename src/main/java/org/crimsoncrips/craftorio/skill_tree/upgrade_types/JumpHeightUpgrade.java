package org.crimsoncrips.craftorio.skill_tree.upgrade_types;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;

import java.math.BigInteger;

public class JumpHeightUpgrade extends CraftorioAttributeUpgrade {

    public static final MapCodec<JumpHeightUpgrade> CODEC = Common.CODEC.xmap(
            c -> new JumpHeightUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost(), c.operation(), c.value()),
            h -> new Common(h.getNameKey(), h.getIcon(), h.getParent(), h.getDescriptionKey(), h.getCost(), h.getOperation(), h.getValue())
    );

    public JumpHeightUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost, UpgradeOperation operation, double value) {
        super(name, icon, parent, description, cost, operation, value);
    }

    public static JumpHeightUpgrade of(CraftorioUpgrade.Builder builder, UpgradeOperation operation, double value) {
        return of(builder, operation, value, JumpHeightUpgrade::new);
    }

    @Override
    protected Holder<Attribute> getAttribute() {
        return Attributes.JUMP_STRENGTH;
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.JUMP_HEIGHT.get();
    }
}

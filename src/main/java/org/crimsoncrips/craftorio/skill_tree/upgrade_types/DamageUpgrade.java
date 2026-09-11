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

public class DamageUpgrade extends CraftorioAttributeUpgrade {

    public static final MapCodec<DamageUpgrade> CODEC = Common.CODEC.xmap(
            c -> new DamageUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost(), c.operation(), c.value()),
            h -> new Common(h.getNameKey(), h.getIcon(), h.getParent(), h.getDescriptionKey(), h.getCost(), h.getOperation(), h.getValue())
    );

    public DamageUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost, UpgradeOperation operation, double value) {
        super(name, icon, parent, description, cost, operation, value);
    }

    public static DamageUpgrade of(CraftorioUpgrade.Builder builder, UpgradeOperation operation, double value) {
        return of(builder, operation, value, DamageUpgrade::new);
    }

    @Override
    protected Holder<Attribute> getAttribute() {
        return Attributes.ATTACK_DAMAGE;
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.DAMAGE.get();
    }
}

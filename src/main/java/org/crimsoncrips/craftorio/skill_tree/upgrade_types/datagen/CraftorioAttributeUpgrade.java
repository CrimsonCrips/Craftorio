package org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.AttributeTarget;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;

import java.math.BigInteger;

public class CraftorioAttributeUpgrade extends CraftorioUpgrade {

    private final AttributeTarget target;
    private final UpgradeOperation operation;
    private final double value;

    public static final Codec<AttributeTarget> TARGET_CODEC = Codec.STRING.xmap(
            s -> AttributeTarget.valueOf(s.toUpperCase()), Enum::name);

    public static final MapCodec<CraftorioAttributeUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CraftorioAttributeUpgrade::getNameKey),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(CraftorioAttributeUpgrade::getIcon),
                    ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(CraftorioAttributeUpgrade::getParent),
                    Codec.STRING.fieldOf("description").forGetter(CraftorioAttributeUpgrade::getDescriptionKey),
                    CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(CraftorioAttributeUpgrade::getCost),
                    TARGET_CODEC.fieldOf("target").forGetter(CraftorioAttributeUpgrade::getTarget),
                    CraftorioModifierUpgrade.OPERATION_CODEC.fieldOf("operation").forGetter(CraftorioAttributeUpgrade::getOperation),
                    Codec.DOUBLE.fieldOf("value").forGetter(CraftorioAttributeUpgrade::getValue),
                    Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(CraftorioAttributeUpgrade::getX),
                    Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(CraftorioAttributeUpgrade::getY)
            ).apply(instance, (name, icon, parent, description, cost, target, operation, value, x, y) -> {
                    CraftorioAttributeUpgrade upgrade = new CraftorioAttributeUpgrade(name, icon, parent.orElse(null), description, cost, target, operation, value);
                    upgrade.setPosition(x, y);
                    return upgrade;
            })
    );

    public CraftorioAttributeUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost,
                                      AttributeTarget target, UpgradeOperation operation, double value) {
        super(name, icon, parent, description, cost);
        this.target = target;
        this.operation = operation;
        this.value = value;
    }

    public AttributeTarget getTarget() {
        return target;
    }

    public UpgradeOperation getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    public static CraftorioAttributeUpgrade of(CraftorioUpgrade.Builder builder, AttributeTarget target, UpgradeOperation operation, double value) {
        return new CraftorioAttributeUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost(),
                target, operation, value);
    }

    @Override
    public void onUnlock(ServerPlayer player, ResourceLocation id) {
        Holder<Attribute> attribute = target.getAttribute();
        if (attribute == null) return;

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        AttributeModifier.Operation op = operation == UpgradeOperation.ADD
                ? AttributeModifier.Operation.ADD_VALUE
                : AttributeModifier.Operation.ADD_MULTIPLIED_BASE;

        instance.addOrReplacePermanentModifier(new AttributeModifier(id, value, op));
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.ATTRIBUTE.get();
    }
}

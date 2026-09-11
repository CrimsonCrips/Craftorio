package org.crimsoncrips.craftorio.skill_tree.upgrade_types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;

import java.math.BigInteger;

public class XpGainUpgrade extends CraftorioUpgrade {

    private final UpgradeOperation operation;
    private final double value;

    public static final MapCodec<XpGainUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(XpGainUpgrade::getNameKey),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(XpGainUpgrade::getIcon),
                    ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(XpGainUpgrade::getParent),
                    Codec.STRING.fieldOf("description").forGetter(XpGainUpgrade::getDescriptionKey),
                    CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(XpGainUpgrade::getCost),
                    CraftorioModifierUpgrade.OPERATION_CODEC.fieldOf("operation").forGetter(XpGainUpgrade::getOperation),
                    Codec.DOUBLE.fieldOf("value").forGetter(XpGainUpgrade::getValue)
            ).apply(instance, (name, icon, parent, description, cost, operation, value) ->
                    new XpGainUpgrade(name, icon, parent.orElse(null), description, cost, operation, value))
    );

    public XpGainUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost, UpgradeOperation operation, double value) {
        super(name, icon, parent, description, cost);
        this.operation = operation;
        this.value = value;
    }

    public static XpGainUpgrade of(CraftorioUpgrade.Builder builder, UpgradeOperation operation, double value) {
        return new XpGainUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost(), operation, value);
    }

    public UpgradeOperation getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.XP_GAIN.get();
    }
}

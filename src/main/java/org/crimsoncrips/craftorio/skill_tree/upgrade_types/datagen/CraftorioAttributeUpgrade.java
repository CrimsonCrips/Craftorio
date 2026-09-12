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
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;

import java.math.BigInteger;
import java.util.Optional;

public abstract class CraftorioAttributeUpgrade extends CraftorioUpgrade {

    private final UpgradeOperation operation;
    private final double value;

    protected CraftorioAttributeUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost,
                                         UpgradeOperation operation, double value) {
        super(name, icon, parent, description, cost);
        this.operation = operation;
        this.value = value;
    }

    public UpgradeOperation getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    protected abstract Holder<Attribute> getAttribute();

    @Override
    public void onUnlock(ServerPlayer player, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(getAttribute());
        if (instance == null) return;

        AttributeModifier.Operation op = operation == UpgradeOperation.ADD
                ? AttributeModifier.Operation.ADD_VALUE
                : AttributeModifier.Operation.ADD_MULTIPLIED_BASE;

        instance.addOrReplacePermanentModifier(new AttributeModifier(id, value, op));
    }

    @FunctionalInterface
    public interface Factory<T extends CraftorioAttributeUpgrade> {
        T create(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost, UpgradeOperation operation, double value);
    }

    protected static <T extends CraftorioAttributeUpgrade> T of(CraftorioUpgrade.Builder builder, UpgradeOperation operation, double value, Factory<T> factory) {
        return factory.create(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost(), operation, value);
    }

    public record Common(String name, ResourceLocation icon, Optional<ResourceLocation> parent, String description,
                          BigInteger cost, UpgradeOperation operation, double value) {

        public static final MapCodec<Common> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.fieldOf("name").forGetter(Common::name),
                        ResourceLocation.CODEC.fieldOf("icon").forGetter(Common::icon),
                        ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Common::parent),
                        Codec.STRING.fieldOf("description").forGetter(Common::description),
                        CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(Common::cost),
                        CraftorioModifierUpgrade.OPERATION_CODEC.fieldOf("operation").forGetter(Common::operation),
                        Codec.DOUBLE.fieldOf("value").forGetter(Common::value)
                ).apply(instance, Common::new)
        );
    }
}

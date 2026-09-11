package org.crimsoncrips.craftorio.skill_tree.upgrade_types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;

import java.math.BigInteger;
import java.util.Optional;

public class CraftorioModifierUpgrade extends CraftorioUpgrade {

    private final ModifierTarget target;
    private final UpgradeOperation operation;
    private final double value;
    private final TagKey<Item> itemTag;

    public static final Codec<ModifierTarget> TARGET_CODEC = Codec.STRING.xmap(
            s -> ModifierTarget.valueOf(s.toUpperCase()), Enum::name);

    public static final Codec<UpgradeOperation> OPERATION_CODEC = Codec.STRING.xmap(
            s -> UpgradeOperation.valueOf(s.toUpperCase()), Enum::name);

    public static final MapCodec<CraftorioModifierUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CraftorioModifierUpgrade::getNameKey),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(CraftorioModifierUpgrade::getIcon),
                    ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(CraftorioModifierUpgrade::getParent),
                    Codec.STRING.fieldOf("description").forGetter(CraftorioModifierUpgrade::getDescriptionKey),
                    CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(CraftorioModifierUpgrade::getCost),
                    TARGET_CODEC.fieldOf("target").forGetter(CraftorioModifierUpgrade::getTarget),
                    OPERATION_CODEC.fieldOf("operation").forGetter(CraftorioModifierUpgrade::getOperation),
                    Codec.DOUBLE.fieldOf("value").forGetter(CraftorioModifierUpgrade::getValue),
                    TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("item_tag").forGetter(CraftorioModifierUpgrade::getItemTag)
            ).apply(instance, (name, icon, parent, description, cost, target, operation, value, itemTag) ->
                    new CraftorioModifierUpgrade(name, icon, parent.orElse(null), description, cost, target, operation, value, itemTag.orElse(null)))
    );

    public CraftorioModifierUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost,
                                     ModifierTarget target, UpgradeOperation operation, double value, TagKey<Item> itemTag) {
        super(name, icon, parent, description, cost);
        this.target = target;
        this.operation = operation;
        this.value = value;
        this.itemTag = itemTag;
    }

    public ModifierTarget getTarget() {
        return target;
    }

    public UpgradeOperation getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    public Optional<TagKey<Item>> getItemTag() {
        return Optional.ofNullable(itemTag);
    }

    public static CraftorioModifierUpgrade of(CraftorioUpgrade.Builder builder, ModifierTarget target, UpgradeOperation operation, double value) {
        return of(builder, target, operation, value, null);
    }

    public static CraftorioModifierUpgrade of(CraftorioUpgrade.Builder builder, ModifierTarget target, UpgradeOperation operation, double value, TagKey<Item> itemTag) {
        return new CraftorioModifierUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost(),
                target, operation, value, itemTag);
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes.MODIFIER.get();
    }
}

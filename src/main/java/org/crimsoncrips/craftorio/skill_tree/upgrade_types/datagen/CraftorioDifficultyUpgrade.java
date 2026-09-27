package org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;

import java.math.BigInteger;

public class CraftorioDifficultyUpgrade extends CraftorioUpgrade {

    private final ModifierTarget target;
    private final UpgradeOperation operation;
    private final double peaceful;
    private final double easy;
    private final double normal;
    private final double hard;

    public static final MapCodec<CraftorioDifficultyUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CraftorioDifficultyUpgrade::getNameKey),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(CraftorioDifficultyUpgrade::getIcon),
                    ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(CraftorioDifficultyUpgrade::getParent),
                    Codec.STRING.fieldOf("description").forGetter(CraftorioDifficultyUpgrade::getDescriptionKey),
                    CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(CraftorioDifficultyUpgrade::getCost),
                    CraftorioModifierUpgrade.TARGET_CODEC.fieldOf("target").forGetter(CraftorioDifficultyUpgrade::getTarget),
                    CraftorioModifierUpgrade.OPERATION_CODEC.fieldOf("operation").forGetter(CraftorioDifficultyUpgrade::getOperation),
                    Codec.DOUBLE.optionalFieldOf("peaceful", 0.0).forGetter(upgrade -> upgrade.peaceful),
                    Codec.DOUBLE.optionalFieldOf("easy", 0.0).forGetter(upgrade -> upgrade.easy),
                    Codec.DOUBLE.optionalFieldOf("normal", 0.0).forGetter(upgrade -> upgrade.normal),
                    Codec.DOUBLE.optionalFieldOf("hard", 0.0).forGetter(upgrade -> upgrade.hard),
                    Codec.INT.optionalFieldOf("max_purchases", 1).forGetter(CraftorioDifficultyUpgrade::getMaxPurchases),
                    Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(CraftorioDifficultyUpgrade::getX),
                    Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(CraftorioDifficultyUpgrade::getY)
            ).apply(instance, (name, icon, parent, description, cost, target, operation, peaceful, easy, normal, hard, maxPurchases, x, y) -> {
                    CraftorioDifficultyUpgrade upgrade = new CraftorioDifficultyUpgrade(name, icon, parent.orElse(null), description, cost, target, operation, peaceful, easy, normal, hard, maxPurchases);
                    upgrade.setPosition(x, y);
                    return upgrade;
            })
    );

    public CraftorioDifficultyUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost,
                                      ModifierTarget target, UpgradeOperation operation, double peaceful, double easy, double normal, double hard, int maxPurchases) {
        super(name, icon, parent, description, cost, maxPurchases);
        this.target = target;
        this.operation = operation;
        this.peaceful = peaceful;
        this.easy = easy;
        this.normal = normal;
        this.hard = hard;
    }

    public ModifierTarget getTarget() {
        return target;
    }

    public UpgradeOperation getOperation() {
        return operation;
    }

    public double valueFor(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> peaceful;
            case EASY -> easy;
            case NORMAL -> normal;
            case HARD -> hard;
        };
    }

    public static CraftorioDifficultyUpgrade of(CraftorioUpgrade.Builder builder, ModifierTarget target, UpgradeOperation operation,
                                                double peaceful, double easy, double normal, double hard) {
        return new CraftorioDifficultyUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost(),
                target, operation, peaceful, easy, normal, hard, builder.getMaxPurchases());
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.DIFFICULTY.get();
    }
}

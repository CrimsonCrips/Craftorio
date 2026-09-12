package org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementMultipliers;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;

import java.math.BigInteger;

public class AdvancementMultiplierUpgrade extends ActionUpgrade {

    public static final MapCodec<AdvancementMultiplierUpgrade> CODEC = Common.CODEC.xmap(
            c -> new AdvancementMultiplierUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost()),
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost())
    );

    public AdvancementMultiplierUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static AdvancementMultiplierUpgrade of(CraftorioUpgrade.Builder builder) {
        return new AdvancementMultiplierUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public void onUnlock(ServerPlayer player, ResourceLocation id) {
        activateFunction();

        for (AdvancementHolder advancement : player.server.getAdvancements().getAllAdvancements()) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) continue;

            double multiplierBonus = CraftorioAdvancementMultipliers.getMultiplier(advancement.id().toString());
            CraftorioMisc.addAdvancementMultiplierBonus(player, multiplierBonus);
        }
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioUpgradeTypes.ADVANCEMENT_MULTIPLIER.get();
    }
}

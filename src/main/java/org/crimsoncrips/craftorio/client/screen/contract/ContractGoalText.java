package org.crimsoncrips.craftorio.client.screen.contract;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.registries.contract.ContractGoal;
import org.crimsoncrips.craftorio.registries.contract.ContractProgress;
import org.crimsoncrips.craftorio.registries.contract.ContractType;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class ContractGoalText {

    private ContractGoalText() {}

    public static Optional<Component> line(CraftorioContract contract) {
        ContractGoal goal = contract.getGoal();
        ContractProgress progress = contract.getProgress();
        return switch (goal.type()) {
            case SINK -> Optional.empty();
            case BUILDING -> progress.blocksTotal() > 0
                    ? Optional.of(Component.translatable("misc.craftorio.contract_goal_building", progress.blocksPlaced(), progress.blocksTotal()))
                    : Optional.of(Component.translatable("misc.craftorio.contract_goal_building_unplaced",
                            goal.structure().map(Object::toString).orElse("?")));
        };
    }

    public static final int TYPE_COLOR = 0x9A9A9A;

    public static Component typeLine(CraftorioContract contract) {
        return Component.translatable(contract.getType() == ContractType.BUILDING
                ? "misc.craftorio.contract_type_build"
                : "misc.craftorio.contract_type_item");
    }

    public static Component bountyHeader(CraftorioContract contract) {
        return switch (contract.getType()) {
            case BUILDING -> Component.translatable("misc.craftorio.contract_bounty_building");
            default -> Component.translatable("misc.craftorio.contract_bounty");
        };
    }
}

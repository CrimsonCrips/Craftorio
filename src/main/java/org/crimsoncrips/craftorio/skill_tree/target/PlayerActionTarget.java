package org.crimsoncrips.craftorio.skill_tree.target;

import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;

public enum PlayerActionTarget {
    WAKE_UP("productive"),
    TRADE("economic_boom"),
    CONTRACT_COMPLETE("contract_momentum"),
    RAID_WIN("spoils_of_war");

    private final ResourceLocation baseEffect;

    PlayerActionTarget(String baseEffect) {
        this.baseEffect = Craftorio.prefix(baseEffect);
    }

    public ResourceLocation baseEffect() {
        return baseEffect;
    }
}

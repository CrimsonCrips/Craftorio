package org.crimsoncrips.craftorio.client.schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPlayerAccess {

    private ClientPlayerAccess() {}

    public static Player player() {
        return Minecraft.getInstance().player;
    }
}

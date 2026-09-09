package org.crimsoncrips.craftorio.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.crimsoncrips.craftorio.client.screen.CraftorioHubScreen;

public class CraftorioKeyMappings {

    public static final KeyMapping OPEN_HUB = new KeyMapping(
            "key.craftorio.open_hub",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            "key.categories.craftorio"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_HUB.consumeClick()) {
            Minecraft.getInstance().setScreen(new CraftorioHubScreen());
        }
    }
}

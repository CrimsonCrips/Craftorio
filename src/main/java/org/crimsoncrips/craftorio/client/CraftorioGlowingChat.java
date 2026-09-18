package org.crimsoncrips.craftorio.client;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class CraftorioGlowingChat {

    public static final GuiMessageTag GLOW_TAG = new GuiMessageTag(0xFFFFFF, null, null, "CraftorioGlow");

    private CraftorioGlowingChat() {}

    public static void sendGlowingMessage(Component message) {
        Minecraft.getInstance().gui.getChat().addMessage(message, null, GLOW_TAG);
    }
}

package org.crimsoncrips.craftorio.client.compat;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.CraftorioGlowingChat;

public final class IrisCompat {

    private static boolean wasInHaven;
    private static boolean shadersWereEnabledBeforeHaven;
    private static boolean suppressingShaders;

    private IrisCompat() {}

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean inHaven = minecraft.level != null && CraftorioMisc.isInHavenDimension(minecraft.level);
        boolean shadersEnabled = IrisApi.getInstance().getConfig().areShadersEnabled();

        if (inHaven) {
            if (shadersEnabled) {
                if (!suppressingShaders) {
                    suppressingShaders = true;
                    shadersWereEnabledBeforeHaven = true;
                    IrisApi.getInstance().getConfig().setShadersEnabledAndApply(false);
                    CraftorioGlowingChat.sendGlowingMessage(Component.translatable("misc.craftorio.haven_shaders_disabled").withStyle(ChatFormatting.ITALIC));
                }
            } else {
                suppressingShaders = false;
            }
        } else if (wasInHaven && shadersWereEnabledBeforeHaven) {
            IrisApi.getInstance().getConfig().setShadersEnabledAndApply(true);
            shadersWereEnabledBeforeHaven = false;
            suppressingShaders = false;
        }

        wasInHaven = inHaven;
    }
}

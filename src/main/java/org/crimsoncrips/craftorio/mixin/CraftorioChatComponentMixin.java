package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.crimsoncrips.craftorio.client.hud.CraftorioGlowingChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public abstract class CraftorioChatComponentMixin {

    private static final long GLOW_PERIOD_MS = 1500L;
    private static final int GLOW_MIN_BRIGHTNESS = 190;
    private static final int GLOW_MAX_BRIGHTNESS = 255;
    private static final float GLOW_HALO_ALPHA_FRACTION = 0.35F;

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int craftorio$glowHavenChatMessage(GuiGraphics instance, Font font, FormattedCharSequence text, int x, int y, int color,
                                                Operation<Integer> original, @Local GuiMessage.Line line) {
        if (line.tag() != CraftorioGlowingChat.GLOW_TAG) {
            return original.call(instance, font, text, x, y, color);
        }

        int alpha = color >>> 24;
        double phase = (System.currentTimeMillis() % GLOW_PERIOD_MS) / (double) GLOW_PERIOD_MS;
        float pulse = (float) (0.5 - 0.5 * Math.cos(phase * Math.PI * 2));
        int brightness = GLOW_MIN_BRIGHTNESS + Math.round((GLOW_MAX_BRIGHTNESS - GLOW_MIN_BRIGHTNESS) * pulse);
        int glowColor = (alpha << 24) | (brightness << 16) | (brightness << 8) | brightness;

        int haloAlpha = Math.round(alpha * GLOW_HALO_ALPHA_FRACTION);
        int haloColor = (haloAlpha << 24) | 0xFFFFFF;
        original.call(instance, font, text, x - 1, y, haloColor);
        original.call(instance, font, text, x + 1, y, haloColor);
        original.call(instance, font, text, x, y - 1, haloColor);
        original.call(instance, font, text, x, y + 1, haloColor);

        return original.call(instance, font, text, x, y, glowColor);
    }
}

package org.crimsoncrips.craftorio.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.crimsoncrips.craftorio.client.screen.CraftorioScreenScroll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiGraphics.class)
public abstract class CraftorioGuiGraphicsMixin {

    @ModifyArg(method = "enableScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/navigation/ScreenRectangle;<init>(IIII)V"), index = 1)
    private int craftorio$enableScissor(int y) {
        return y - (int) Math.round(CraftorioScreenScroll.activeRenderOffset());
    }
}

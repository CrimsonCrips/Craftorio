package org.crimsoncrips.craftorio.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.crimsoncrips.craftorio.client.screen.CraftorioScreenScroll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class CraftorioScreenMixin {

    @Unique
    private boolean craftorio$scrollPushed;

    @Unique
    private boolean craftorio$isScrollingScreen() {
        Screen self = (Screen) (Object) this;
        return CraftorioScreenScroll.isScrollable(self) && Minecraft.getInstance().screen == self;
    }

    @ModifyVariable(method = "renderWithTooltip", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int craftorio$renderWithTooltip(int mouseY) {
        if (!craftorio$isScrollingScreen() || mouseY == Integer.MAX_VALUE) return mouseY;

        Screen self = (Screen) (Object) this;
        CraftorioScreenScroll.update(self);
        return mouseY + (int) Math.round(CraftorioScreenScroll.offset(self));
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void craftorio$renderWithTooltipBegin(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        this.craftorio$scrollPushed = false;
        if (!craftorio$isScrollingScreen()) return;

        Screen self = (Screen) (Object) this;
        CraftorioScreenScroll.update(self);
        double offset = CraftorioScreenScroll.offset(self);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, -offset, 0.0);
        CraftorioScreenScroll.beginRender(offset);
        this.craftorio$scrollPushed = true;
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void craftorio$renderWithTooltipEnd(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.craftorio$scrollPushed) return;

        this.craftorio$scrollPushed = false;
        guiGraphics.pose().popPose();
        CraftorioScreenScroll.endRender();
        CraftorioScreenScroll.drawScrollbar(guiGraphics, (Screen) (Object) this);
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void craftorio$renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, CraftorioScreenScroll.activeRenderOffset(), 0.0);
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void craftorio$renderBackgroundEnd(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        guiGraphics.pose().popPose();
    }
}

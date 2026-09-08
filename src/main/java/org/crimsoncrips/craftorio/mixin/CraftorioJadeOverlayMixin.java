package org.crimsoncrips.craftorio.mixin;

import net.minecraft.client.renderer.Rect2i;
import org.crimsoncrips.craftorio.client.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.impl.ui.BoxElement;

@Mixin(value = BoxElement.class, remap = false)
public abstract class CraftorioJadeOverlayMixin {

    @Inject(method = "updateExpectedRect", at = @At("TAIL"))
    private void craftorioPushBelowPointsBar(TooltipRect rect, CallbackInfo ci) {
        Rect2i pointsBarRect = ClientEvents.getPointsBarScreenRect();
        if (pointsBarRect == null) {
            return;
        }

        Rect2i expected = rect.expectedRect;
        if (overlaps(expected, pointsBarRect)) {
            expected.setY(pointsBarRect.getY() + pointsBarRect.getHeight());
        }
    }

    private static boolean overlaps(Rect2i a, Rect2i b) {
        return a.getX() < b.getX() + b.getWidth()
                && a.getX() + a.getWidth() > b.getX()
                && a.getY() < b.getY() + b.getHeight()
                && a.getY() + a.getHeight() > b.getY();
    }
}

package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.renderer.Rect2i;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BossHealthOverlay.class)
public abstract class CraftorioBossHealthOverlayMixin {

    private static final int VANILLA_TOP = 12;
    private static final int PUSH_DOWN_PADDING = 4;

    @WrapMethod(method = "render")
    private void craftorio$render(GuiGraphics guiGraphics, Operation<Void> original) {
        Rect2i pointsBarRect = ClientEvents.getPointsBarScreenRect();
        int shift = pointsBarRect == null ? 0 : pointsBarRect.getY() + pointsBarRect.getHeight() + PUSH_DOWN_PADDING - VANILLA_TOP;
        if (shift <= 0) {
            original.call(guiGraphics);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, shift, 0.0F);
        original.call(guiGraphics);
        guiGraphics.pose().popPose();
    }

}

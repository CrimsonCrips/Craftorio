package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.crimsoncrips.craftorio.client.screen.CraftorioScreenScroll;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public abstract class CraftorioMouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private double craftorio$scrolledYpos(double ypos) {
        double offset = CraftorioScreenScroll.currentScreenOffset();
        if (offset == 0.0) return ypos;

        Window window = this.minecraft.getWindow();
        return ypos + offset * window.getScreenHeight() / window.getGuiScaledHeight();
    }

    @ModifyExpressionValue(method = "onPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D", opcode = Opcodes.GETFIELD))
    private double craftorio$onPress(double ypos) {
        return craftorio$scrolledYpos(ypos);
    }

    @ModifyExpressionValue(method = "onScroll", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D", opcode = Opcodes.GETFIELD))
    private double craftorio$onScroll(double ypos) {
        return craftorio$scrolledYpos(ypos);
    }

    @ModifyExpressionValue(method = "handleAccumulatedMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D", opcode = Opcodes.GETFIELD))
    private double craftorio$handleAccumulatedMovement(double ypos) {
        return craftorio$scrolledYpos(ypos);
    }
}

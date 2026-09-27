package org.crimsoncrips.craftorio.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.crimsoncrips.craftorio.client.render.CraftorioPlayerDissolve;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.opengl.GL13;

@Mixin(OverlayTexture.class)
public abstract class CraftorioOverlayTextureMixin {

    @Shadow
    @Final
    private DynamicTexture texture;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void craftorio$init(CallbackInfo ci) {
        NativeImage pixels = this.texture.getPixels();
        if (pixels == null) return;

        for (int u = 0; u < 16; u++) {
            int alpha = Math.round((1f - u / 15f) * 255f);
            pixels.setPixelRGBA(u, CraftorioPlayerDissolve.FULL_WHITE_OVERLAY_V, alpha << 24 | 0xFFFFFF);
        }

        RenderSystem.activeTexture(GL13.GL_TEXTURE1);
        this.texture.bind();
        pixels.upload(0, 0, 0, 0, 0, pixels.getWidth(), pixels.getHeight(), false, true, false, false);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
    }
}

package org.crimsoncrips.craftorio.mixin;



import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.crimsoncrips.craftorio.ClaimsHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.WorldMapSession;
import xaero.map.highlight.HighlighterRegistry;

@Mixin(WorldMapSession.class)

public abstract class CraftWorldMapSessionMixin {

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lxaero/map/highlight/HighlighterRegistry;end()V"))
    private void Craft_init(ClientPacketListener connection, long biomeZoomSeed, CallbackInfo ci, @Local HighlighterRegistry highlighterRegistry) {
        highlighterRegistry.register(new ClaimsHighlighter());
    }



}

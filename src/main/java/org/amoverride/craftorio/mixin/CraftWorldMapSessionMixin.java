package org.amoverride.craftorio.mixin;



import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.amoverride.craftorio.ClaimsHighlighter;
import org.amoverride.craftorio.Craft_Misc;
import org.amoverride.craftorio.networking.OwnLandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.WorldMapSession;
import xaero.map.gui.GuiMap;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.highlight.HighlighterRegistry;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldMapSession.class)

public abstract class CraftWorldMapSessionMixin {

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lxaero/map/highlight/HighlighterRegistry;end()V"))
    private void Craft_init(ClientPacketListener connection, long biomeZoomSeed, CallbackInfo ci, @Local HighlighterRegistry highlighterRegistry) {
        highlighterRegistry.register(new ClaimsHighlighter());
    }



}

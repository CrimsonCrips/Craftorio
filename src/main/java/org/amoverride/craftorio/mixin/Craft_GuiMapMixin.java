package org.amoverride.craftorio.mixin;



import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.amoverride.craftorio.Craft_Misc;
import org.amoverride.craftorio.networking.OwnLandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.WorldMap;
import xaero.map.controls.ControlsRegister;
import xaero.map.gui.GuiMap;
import xaero.map.gui.GuiTexturedButton;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiMap.class)

public abstract class Craft_GuiMapMixin {

    @Shadow
    private MapTileSelection mapTileSelection;

    @Inject(method = "getRightClickOptions", at = @At("TAIL"), remap = false)
    private void Craft_getRightClickOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir, @Local ArrayList<RightClickOption> options) {

        if (mapTileSelection != null) {

            GuiMap guiMap = (GuiMap)(Object)this;
            List<ChunkPos> chunks = Craft_Misc.generateSelectionChunks(mapTileSelection.getStartX(),mapTileSelection.getStartZ(),mapTileSelection.getEndX(), mapTileSelection.getEndZ());

            options.add(new RightClickOption("misc.craftorio.claim_land", options.size(), guiMap) {
                public void onAction(Screen screen) {
                    PacketDistributor.sendToServer(new OwnLandPacket(chunks,true));
                }
            });
            options.add(new RightClickOption("misc.craftorio.unclaim_land", options.size(), guiMap) {
                public void onAction(Screen screen) {
                    PacketDistributor.sendToServer(new OwnLandPacket(chunks,false));
                }
            });
        }

    }
}

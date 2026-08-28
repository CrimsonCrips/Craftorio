package org.crimsoncrips.craftorio.mixin;



import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.OwnLandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Mixin(GuiMap.class)

public abstract class CraftGuiMapMixin {

//    @Shadow
//    private MapTileSelection mapTileSelection;
//
//    @Inject(method = "getRightClickOptions", at = @At("TAIL"), remap = false)
//    private void Craft_getRightClickOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir, @Local ArrayList<RightClickOption> options) {
//
//        if (mapTileSelection != null) {
//
//
//            GuiMap guiMap = (GuiMap)(Object)this;
//            List<ChunkPos> chunks = CraftorioMisc.generateSelectionChunks(mapTileSelection.getStartX(),mapTileSelection.getStartZ(),mapTileSelection.getEndX(), mapTileSelection.getEndZ());
//            if (guiMap.getMinecraft().level == null) return;
//            long claimed_amount = CraftorioMisc.getLandAmount(guiMap.getMinecraft().level);
//            BigInteger amountToClaim = CraftorioMisc.pointsToExpand(chunks.size(),claimed_amount);
//
//            String string = Component.translatable("misc.craftorio.claim_land").getString();
//
//            options.add(new RightClickOption(string + " : " + amountToClaim, options.size(), guiMap) {
//                public void onAction(Screen screen) {
//                    PacketDistributor.sendToServer(new OwnLandPacket(chunks,true));
//                }
//            });
//            options.add(new RightClickOption("misc.craftorio.unclaim_land", options.size(), guiMap) {
//                public void onAction(Screen screen) {
//                    PacketDistributor.sendToServer(new OwnLandPacket(chunks,false));
//                }
//            });
//        }
//
//    }

//    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V",ordinal = 1))
//    private boolean test(GuiGraphics instance, Font font, String text, int x, int y, int color) {
//        return false;
//    }
}

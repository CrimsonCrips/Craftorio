package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
public abstract class CraftorioPlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    @WrapWithCondition(method = "lambda$award$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private boolean craftorio$award(PlayerList playerList, Component message, boolean overlay) {
        return !CraftorioMisc.universalBased(this.player.level());
    }
}

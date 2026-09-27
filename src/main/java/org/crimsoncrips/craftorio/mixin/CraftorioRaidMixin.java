package org.crimsoncrips.craftorio.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.skill_tree.target.PlayerActionTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Raid.class)
public abstract class CraftorioRaidMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"))
    private void craftorio$tick(ServerPlayer player, ResourceLocation stat, Operation<Void> original) {
        original.call(player, stat);
        ServerEvents.grantActionEffects(player, PlayerActionTarget.RAID_WIN);
    }

}

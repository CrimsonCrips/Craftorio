package org.amoverride.craftorio.mixin;



import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.amoverride.craftorio.Craft_Misc;
import org.amoverride.craftorio.networking.OwnLandPacket;
import org.amoverride.craftorio.server.ChunkCollisionHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


@Mixin(CollisionGetter.class)
public interface Craft_CollisionGetter {

    @WrapMethod(method = "borderCollision")
    default VoxelShape gatedBorderCollision(Entity entity, AABB box, Operation<VoxelShape> original) {
        @Nullable VoxelShape borderCollision = original.call(entity, box);

        // Level does in fact implement CollisionGetter
        //noinspection ConstantValue
        if ((Object) this instanceof Level level && ChunkCollisionHooks.levelHasUnlockableChunks(level)) {
            return ChunkCollisionHooks.combineWorldAndChunkBorders(level, entity, borderCollision);
        }

        return borderCollision;
    }

}
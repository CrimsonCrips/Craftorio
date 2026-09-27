package org.crimsoncrips.craftorio.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.compat.CreateSchematicCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.schematics.table.SchematicTableMenu$1")
public abstract class CraftorioSchematicTableInputSlotMixin {

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean craftorio$mayPlace(boolean original, ItemStack stack) {
        return original || CreateSchematicCompat.isSchematic(stack);
    }

}

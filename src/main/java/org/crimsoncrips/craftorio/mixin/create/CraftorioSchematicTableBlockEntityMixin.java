package org.crimsoncrips.craftorio.mixin.create;

import com.simibubi.create.content.schematics.table.SchematicTableBlockEntity;
import org.crimsoncrips.craftorio.compat.CreateSchematicCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SchematicTableBlockEntity.class)
public abstract class CraftorioSchematicTableBlockEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void craftorio$tick(CallbackInfo ci) {
        CreateSchematicCompat.tickTable((SchematicTableBlockEntity) (Object) this);
    }

}

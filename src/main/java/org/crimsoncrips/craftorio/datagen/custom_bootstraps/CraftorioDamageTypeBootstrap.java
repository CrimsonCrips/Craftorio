package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import org.crimsoncrips.craftorio.registries.CraftorioDamageTypes;

public final class CraftorioDamageTypeBootstrap {

    private CraftorioDamageTypeBootstrap() {}

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(CraftorioDamageTypes.CHRONOSPHERE,
                new DamageType("craftorio.chronosphere", DamageScaling.NEVER, 0.0F, DamageEffects.HURT, DeathMessageType.DEFAULT));
    }
}

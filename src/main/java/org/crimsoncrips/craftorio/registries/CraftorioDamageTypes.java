package org.crimsoncrips.craftorio.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import org.crimsoncrips.craftorio.Craftorio;

public final class CraftorioDamageTypes {

    public static final ResourceKey<DamageType> CHRONOSPHERE = ResourceKey.create(Registries.DAMAGE_TYPE, Craftorio.prefix("chronosphere"));

    private CraftorioDamageTypes() {}
}

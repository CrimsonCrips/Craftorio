package org.crimsoncrips.craftorio.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.CraftorioDamageTypes;

import java.util.concurrent.CompletableFuture;

public class CraftorioDamageTypeTagGen extends DamageTypeTagsProvider {

    public CraftorioDamageTypeTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
        super(output, future, Craftorio.MODID, helper);
    }

    @Override
    public String getName() {
        return "Craftorio Damage Type Tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(DamageTypeTags.BYPASSES_INVULNERABILITY).add(CraftorioDamageTypes.CHRONOSPHERE);
    }
}

package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractTexture;

public class CraftorioContractTextureBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioContractTexture> context) {
        context.register(CraftorioContractTexture.DEFAULT,
                new CraftorioContractTexture(Craftorio.getGuiTexture("contract_textures/contract.png")));
    }
}

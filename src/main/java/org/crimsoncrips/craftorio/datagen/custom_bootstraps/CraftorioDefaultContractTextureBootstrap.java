package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.contract.CraftorioDefaultContractTexture;

public class CraftorioDefaultContractTextureBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioDefaultContractTexture> context) {
        context.register(CraftorioDefaultContractTexture.DEFAULT,
                new CraftorioDefaultContractTexture(Craftorio.getGuiTexture("contract_textures/default_contract.png")));
    }
}

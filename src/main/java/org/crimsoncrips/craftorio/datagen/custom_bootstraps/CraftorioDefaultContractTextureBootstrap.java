package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractTexture;

public class CraftorioDefaultContractTextureBootstrap {

    public static void bootstrap(BootstrapContext<CraftorioContractTexture> context) {
        context.register(CraftorioContractTexture.DEFAULT,
                new CraftorioContractTexture(Craftorio.getGuiTexture("contract_textures/default_contract.png")));
        context.register(CraftorioContractTexture.TRAZYN,
                new CraftorioContractTexture(Craftorio.getGuiTexture("contract_textures/custom/trazyn.png")));
    }
}

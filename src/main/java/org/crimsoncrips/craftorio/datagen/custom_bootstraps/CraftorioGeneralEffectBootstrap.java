package org.crimsoncrips.craftorio.datagen.custom_bootstraps;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;

public class CraftorioGeneralEffectBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");
    private static final int DURATION_SECONDS = 30;
    private static final float[] MULTIPLIERS = {1.5F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F, 10F};

    public static void buffBootstrap(BootstrapContext<CraftorioEffects> context) {
        for (float multiplier : MULTIPLIERS) {
            context.register(
                    key("general/multiplier_" + pathSuffix(multiplier)),
                    new GeneralMultiplierEffect(multiplier, "registry.general_multiplier_" + pathSuffix(multiplier), DURATION_SECONDS, DEFAULT_ICON, 1, true)
            );
        }
    }

    public static void debuffBootstrap(BootstrapContext<CraftorioEffects> context) {
        for (float multiplier : MULTIPLIERS) {
            context.register(
                    key("general/multiplier_neg_" + pathSuffix(multiplier)),
                    new GeneralMultiplierEffect(-multiplier, "registry.general_multiplier_neg_" + pathSuffix(multiplier), DURATION_SECONDS, DEFAULT_ICON, 1, true)
            );
        }
    }

    private static String pathSuffix(float multiplier) {
        String text = multiplier == Math.floor(multiplier)
                ? String.valueOf((int) multiplier)
                : String.valueOf(multiplier);
        return text.replace('.', '_');
    }

    private static ResourceKey<CraftorioEffects> key(String path) {
        return ResourceKey.create(CraftorioEffects.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, path));
    }
}

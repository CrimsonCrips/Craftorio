package org.crimsoncrips.craftorio.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioClientConfig {

    public final ModConfigSpec.IntValue POINT_FORMATTING;
    public final ModConfigSpec.BooleanValue SKIP_CONTRACT_CLAIM_ANIMATION;

    public CraftorioClientConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
        this.POINT_FORMATTING = buildInt(builder, "POINT_FORMATTING", 2,0,3, "Point formatting (0 = 100000,1 = 1e5,2 = 100k,3 = 100 Thousand)");
        this.SKIP_CONTRACT_CLAIM_ANIMATION = buildBoolean(builder, "SKIP_CONTRACT_CLAIM_ANIMATION", false, "Skip the spin/reveal animation on the contract claiming screen and show contracts already settled");

        builder.pop();


    }


    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

}
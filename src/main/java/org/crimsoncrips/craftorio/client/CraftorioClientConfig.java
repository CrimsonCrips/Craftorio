package org.crimsoncrips.craftorio.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioClientConfig {

//    public final ModConfigSpec.BooleanValue CHUNK_BASED_EXPANSION;
    public final ModConfigSpec.IntValue POINT_FORMATTING;

    public CraftorioClientConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
        this.POINT_FORMATTING = buildInt(builder, "POINT_FORMATTING", 0,0,3, "Point formatting (0 = 100000,1 = 1e5,2 = 100k,3 = 100 Thousand)");

        builder.pop();


    }


    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

}
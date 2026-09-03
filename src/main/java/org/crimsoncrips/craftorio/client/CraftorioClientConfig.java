package org.crimsoncrips.craftorio.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioClientConfig {

    public final ModConfigSpec.IntValue POINT_FORMATTING;
    public final ModConfigSpec.BooleanValue POINT_BAR_LOCATION;

    public CraftorioClientConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
        this.POINT_FORMATTING = buildInt(builder, "POINT_FORMATTING", 0,0,3, "Point formatting (0 = 100000,1 = 1e5,2 = 100k,3 = 100 Thousand)");
        this.POINT_BAR_LOCATION = buildBoolean(builder, "POINT_BAR_LOCATION", true, "true = Right, false = Left");

        builder.pop();


    }


    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

}
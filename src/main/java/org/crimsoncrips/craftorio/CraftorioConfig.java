package org.crimsoncrips.craftorio;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioConfig {

//    public final ModConfigSpec.BooleanValue CHUNK_BASED_EXPANSION;
    public final ModConfigSpec.IntValue STARTING_LAND_SIZE;
    public CraftorioConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
//        this.CHUNK_BASED_EXPANSION = buildBoolean(builder, "CHUNK_BASED_EXPANSION", true, "Mode of expansion is through chunks");
        this.STARTING_LAND_SIZE = buildInt(builder, "STARTING_LAND_SIZE", 5,1,Integer.MAX_VALUE, "Starting size for claimed land");

        builder.pop();


    }

    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.DoubleValue buildDouble(ModConfigSpec.Builder builder, String name, double defaultValue) {
        return builder.comment(".").translation(name).defineInRange(name, defaultValue, 0, Double.MAX_VALUE);
    }

    private static ModConfigSpec.DoubleValue buildDouble(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }
}
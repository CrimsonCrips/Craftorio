package org.crimsoncrips.craftorio.server;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioServerConfig {

//    public final ModConfigSpec.BooleanValue CHUNK_BASED_EXPANSION;
    public final ModConfigSpec.IntValue STARTING_LAND_SIZE;
    public final ModConfigSpec.DoubleValue COST_MULTIPLIER;
    public final ModConfigSpec.IntValue COST_BASE;
    public final ModConfigSpec.IntValue EXPANSION_AMOUNT;

    public CraftorioServerConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
//        this.CHUNK_BASED_EXPANSION = buildBoolean(builder, "CHUNK_BASED_EXPANSION", true, "Mode of expansion is through chunks");
        this.STARTING_LAND_SIZE = buildInt(builder, "STARTING_LAND_SIZE", 5,1,Integer.MAX_VALUE, "Starting size for claimed land");
        this.COST_MULTIPLIER = buildDouble(builder, "COST_MULTIPLIER", 0.05F,0,Double.MAX_VALUE, "Cost Multiplier (ex. 0.05F = 5%)");
        this.COST_BASE = buildInt(builder, "COST_BASE", 10,1,Integer.MAX_VALUE, "Base Cost of Land");

        builder.push("Border Based");
        this.EXPANSION_AMOUNT = buildInt(builder, "EXPANSION_AMOUNT", 1,1,Integer.MAX_VALUE, "Amount of expansion per purchase");

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
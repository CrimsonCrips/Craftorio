package org.crimsoncrips.craftorio.server;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioServerConfig {

    public final ModConfigSpec.BooleanValue CHUNK_BASED_EXPANSION;
    public final ModConfigSpec.BooleanValue UNIVERSAL_PROGRESSION;
    public final ModConfigSpec.BooleanValue NO_BORDERS;
    public final ModConfigSpec.IntValue STARTING_LAND_SIZE;
    public final ModConfigSpec.DoubleValue COST_MULTIPLIER;
    public final ModConfigSpec.IntValue BASE_COST;
    public final ModConfigSpec.IntValue EXPANSION_AMOUNT;
    public final ModConfigSpec.ConfigValue<String> STARTING_POINTS;

    public final ModConfigSpec.DoubleValue MIN_SPAWN_DISTANCE;
    public final ModConfigSpec.DoubleValue MAX_SPAWN_DISTANCE;


    public CraftorioServerConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
        this.CHUNK_BASED_EXPANSION = buildBoolean(builder, "CHUNK_BASED_EXPANSION", false, "Mode of expansion is through chunks (atm this doesnt function therefore false by default)");
        this.UNIVERSAL_PROGRESSION = buildBoolean(builder, "UNIVERSAL_PROGRESSION", true, "Whether progress is universal or solo");
        this.STARTING_LAND_SIZE = buildInt(builder, "STARTING_LAND_SIZE", 5,1,Integer.MAX_VALUE, "Starting size for claimed land");
        this.COST_MULTIPLIER = buildDouble(builder, "COST_MULTIPLIER", 0.05F,0,Double.MAX_VALUE, "Cost Multiplier (ex. 0.05F = 5%)");
        this.BASE_COST = buildInt(builder, "BASE_COST", 10,1,Integer.MAX_VALUE, "Base Cost of Land");
        this.STARTING_POINTS = buildString(builder, "STARTING_POINTS",  "100", "Starting points (exponents work like 1e2)");

        MIN_SPAWN_DISTANCE = builder.defineInRange("min_spawn_distance", 500.0, 0.0, 100000.0);
        MAX_SPAWN_DISTANCE = builder.defineInRange("max_spawn_distance", 2000.0, 0.0, 1000000.0);

        builder.push("Border Based");
        this.EXPANSION_AMOUNT = buildInt(builder, "EXPANSION_AMOUNT", 1,1,Integer.MAX_VALUE, "Amount of expansion per purchase");
        builder.pop();

        builder.push("Chunk Based");
        this.NO_BORDERS = buildBoolean(builder, "NO_BORDERS", false, "Whether another player can lay claim to a already claimed chunk to be able to access as well");

        builder.pop();


    }

    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.ConfigValue<String> buildString(ModConfigSpec.Builder builder, String name, String defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

    private static ModConfigSpec.DoubleValue buildDouble(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }
}
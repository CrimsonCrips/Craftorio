package org.crimsoncrips.craftorio.server.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.crimsoncrips.craftorio.server.shop.CraftorioShopMode;

public class CraftorioServerConfig {

    public final ModConfigSpec.IntValue STARTING_LAND_SIZE;
    public final ModConfigSpec.DoubleValue COST_MULTIPLIER;
    public final ModConfigSpec.IntValue SHOP_COST_MULTIPLIER;
    public final ModConfigSpec.IntValue BASE_COST;
    public final ModConfigSpec.IntValue EXPANSION_AMOUNT;
    public final ModConfigSpec.ConfigValue<String> STARTING_POINTS;
    public final ModConfigSpec.EnumValue<CraftorioShopMode> SHOP_MODE;

    public final ModConfigSpec.DoubleValue MIN_SPAWN_DISTANCE;
    public final ModConfigSpec.DoubleValue MAX_SPAWN_DISTANCE;

    public final ModConfigSpec.DoubleValue CHUNK_OUT_OF_BOUNDS_DAMAGE;

    public final ModConfigSpec.BooleanValue RANDOM_EFFECTS_ENABLED;
    public final ModConfigSpec.IntValue RANDOM_EFFECT_INTERVAL;

    public final ModConfigSpec.IntValue MAX_OFFERED_CONTRACTS;
    public final ModConfigSpec.IntValue CONTRACT_REFRESH_SECONDS;
    public final ModConfigSpec.DoubleValue CONTRACT_REFRESH_COST_PERCENT;
    public final ModConfigSpec.ConfigValue<String> CONTRACT_REFRESH_MIN_COST;

    public final ModConfigSpec.IntValue SINK_VALUE_BONUS_AMOUNT;
    public final ModConfigSpec.IntValue SINK_VALUE_BONUS_THRESHOLD;

    public final ModConfigSpec.DoubleValue MULT_PER_CONTRACT_DONE;

    public final ModConfigSpec.ConfigValue<String> VALUE_CONDENSER_CAP;
    public final ModConfigSpec.DoubleValue LOAN_INTEREST_PERCENT;
    public final ModConfigSpec.IntValue SACRIFICE_REQUIRED_LIFE;
    public final ModConfigSpec.IntValue SACRIFICE_TIME_LIMIT_MINUTES;
    public final ModConfigSpec.IntValue SACRIFICE_COOLDOWN_MINUTES;
    public final ModConfigSpec.ConfigValue<String> REBIRTH_BASE_COST;
    public final ModConfigSpec.IntValue REBIRTH_BASE_LIFE_POINTS;
    public final ModConfigSpec.DoubleValue REBIRTH_SKIP_BONUS_PERCENT;
    public final ModConfigSpec.IntValue REBIRTH_MAX_SKIP;

    public final ModConfigSpec.ConfigValue<String> EFFECT_RUNE_BASE_PRICE;
    public final ModConfigSpec.IntValue EFFECT_RUNE_PRICE_MULTIPLIER;
    public final ModConfigSpec.IntValue EFFECT_RUNE_MAX_EFFECTS;

    public final ModConfigSpec.IntValue SELECTION_MAX_VOLUME;

    public final ModConfigSpec.DoubleValue BUILD_BLITZ_PERCENT;
    public final ModConfigSpec.IntValue BUILD_BLITZ_COOLDOWN_MINUTES;


    public CraftorioServerConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
        this.STARTING_POINTS = buildString(builder, "STARTING_POINTS",  "100", "Starting points (exponents work like 1e2)");
        this.MIN_SPAWN_DISTANCE = builder.defineInRange("MIN_SPAWN_DISTANCE", 500.0, 0.0, 100000.0);
        this.MAX_SPAWN_DISTANCE = builder.defineInRange("MAX_SPAWN_DISTANCE", 2000.0, 0.0, 1000000.0);

        builder.push("Expansion");
        this.STARTING_LAND_SIZE = buildInt(builder, "STARTING_LAND_SIZE", 1,1,Integer.MAX_VALUE, "Starting size for claimed land");
        this.COST_MULTIPLIER = buildDouble(builder, "COST_MULTIPLIER", 0.05F,0,Double.MAX_VALUE, "Cost Multiplier to claim land (ex. 0.05F = 5%)");
        this.BASE_COST = buildInt(builder, "BASE_COST", 10,1,Integer.MAX_VALUE, "Base Cost of Land");

        builder.push("Border Based");
        this.EXPANSION_AMOUNT = buildInt(builder, "EXPANSION_AMOUNT", 1,1,Integer.MAX_VALUE, "Amount of expansion per purchase");
        builder.pop();

        builder.push("Chunk Based");
        this.CHUNK_OUT_OF_BOUNDS_DAMAGE = buildDouble(builder, "CHUNK_OUT_OF_BOUNDS_DAMAGE", 2.0, 0, Double.MAX_VALUE, "Flat damage dealt per second while standing outside a chunk you own");
        builder.pop();
        builder.pop();

        builder.push("Random Effects");
        this.RANDOM_EFFECTS_ENABLED = buildBoolean(builder, "RANDOM_EFFECTS_ENABLED", true, "Whether registered effects can randomly be granted, similar to weather");
        this.RANDOM_EFFECT_INTERVAL = buildInt(builder, "RANDOM_EFFECT_INTERVAL", 1500, 1, Integer.MAX_VALUE, "Exact number of seconds between random effect grants");
        builder.pop();

        builder.push("Shop");
        this.SHOP_MODE = builder.comment("Shop screen access: DISABLED (cant be opened), LOCKED (only items the player has picked up at least once can be bought), OPEN (every priced item is buyable immediately)").translation("SHOP_MODE").defineEnum("SHOP_MODE", CraftorioShopMode.LOCKED);
        this.SHOP_COST_MULTIPLIER = buildInt(builder, "SHOP_COST_MULTIPLIER", 10,1,Integer.MAX_VALUE, "Multiplier cost of buying items from shop");
        builder.pop();

        builder.push("Contracts");
        this.MAX_OFFERED_CONTRACTS = buildInt(builder, "MAX_OFFERED_CONTRACTS", 3,1,5, "Maximum number of contracts offered at once on the contract offer screen");
        this.CONTRACT_REFRESH_SECONDS = buildInt(builder, "CONTRACT_REFRESH_SECONDS", 1200, 1, Integer.MAX_VALUE, "Exact number of seconds between contract offer refreshes");
        this.CONTRACT_REFRESH_COST_PERCENT = buildDouble(builder, "CONTRACT_REFRESH_COST_PERCENT", 5.0, 0, 100, "Percent of a player's highest points charged to manually refresh the contract offer early");
        this.CONTRACT_REFRESH_MIN_COST = buildString(builder, "CONTRACT_REFRESH_MIN_COST", "100", "Lowest possible price for manually refreshing the contract offer early (exponents work like 1e2)");
        this.MULT_PER_CONTRACT_DONE = buildDouble(builder, "MULT_PER_CONTRACT_DONE", 0.01, 0, Double.MAX_VALUE, "Multiplier bonus granted per contract completed (requires the matching skill tree upgrade). Ex. 0.01 = +0.01x mult per contract completed");
        builder.pop();

        builder.push("Sink Value");
        this.SINK_VALUE_BONUS_AMOUNT = buildInt(builder, "SINK_VALUE_BONUS_AMOUNT", 10, 0, Integer.MAX_VALUE, "Bonus base value granted per SINK_VALUE_BONUS_THRESHOLD times an item has been sinked (requires the matching skill tree upgrade)");
        this.SINK_VALUE_BONUS_THRESHOLD = buildInt(builder, "SINK_VALUE_BONUS_THRESHOLD", 10000, 1, Integer.MAX_VALUE, "Number of times an item must be sinked to grant one SINK_VALUE_BONUS_AMOUNT (requires the matching skill tree upgrade)");
        builder.pop();


        builder.push("Value Condenser");
        this.VALUE_CONDENSER_CAP = buildString(builder, "VALUE_CONDENSER_CAP", "1e12", "Maximum total value a condensed item (the carrier) can hold, accepts whole numbers and exponents (ex. 1e12 = one trillion), raised or lowered by upgrades");
        builder.pop();

        builder.push("Loan Shark");
        this.LOAN_INTEREST_PERCENT = buildDouble(builder, "LOAN_INTEREST_PERCENT", 20.0, 0, 1000, "Percent added on top of a loan's principal when it is taken (ex. 20 = the player owes 120% of what they were given)");
        builder.pop();

        builder.push("Sacrifice");
        this.SACRIFICE_REQUIRED_LIFE = buildInt(builder, "SACRIFICE_REQUIRED_LIFE", 1000, 1, Integer.MAX_VALUE, "Life (rebirth count) a player must have reached before they can sacrifice");
        this.SACRIFICE_TIME_LIMIT_MINUTES = buildInt(builder, "SACRIFICE_TIME_LIMIT_MINUTES", 10, 1, Integer.MAX_VALUE, "Minutes a player may stay in the Haven deciding a sacrifice before they are sent out");
        this.SACRIFICE_COOLDOWN_MINUTES = buildInt(builder, "SACRIFICE_COOLDOWN_MINUTES", 30, 0, Integer.MAX_VALUE, "Minutes a player must wait before sacrificing again after running out of time in the Haven");
        builder.pop();

        builder.push("Rebirth");
        this.REBIRTH_BASE_COST = buildString(builder, "REBIRTH_BASE_COST", "2.5e19", "Base points cost of a rebirth, all later lives scale from it (exponents work like 1e2, default 2.5e19 = 25 Qn)");
        this.REBIRTH_BASE_LIFE_POINTS = buildInt(builder, "REBIRTH_BASE_LIFE_POINTS", 10, 0, Integer.MAX_VALUE, "Baseline rebirth crystals granted per life gained");
        this.REBIRTH_SKIP_BONUS_PERCENT = buildDouble(builder, "REBIRTH_SKIP_BONUS_PERCENT", 0.20, 0, Double.MAX_VALUE, "Extra percent of REBIRTH_BASE_LIFE_POINTS (rebirth crystals) granted per additional life skipped in a single rebirth (ex. 0.20 = +20% per life skipped)");
        this.REBIRTH_MAX_SKIP = buildInt(builder, "REBIRTH_MAX_SKIP", 100, 0, Integer.MAX_VALUE, "Maximum number of extra lives that can be skipped in a single rebirth");
        builder.pop();

        builder.push("Effect Rune Shop");
        this.EFFECT_RUNE_BASE_PRICE = buildString(builder, "EFFECT_RUNE_BASE_PRICE", "1e18", "Price of an Effect Rune with 1 random effect (exponents work like 1e18 = 1 Qn), each extra effect multiplies the price by EFFECT_RUNE_PRICE_MULTIPLIER");
        this.EFFECT_RUNE_PRICE_MULTIPLIER = buildInt(builder, "EFFECT_RUNE_PRICE_MULTIPLIER", 1000, 2, Integer.MAX_VALUE, "Price multiplier applied per extra effect on an Effect Rune");
        this.EFFECT_RUNE_MAX_EFFECTS = buildInt(builder, "EFFECT_RUNE_MAX_EFFECTS", 10, 1, 100, "Maximum number of random effects that can be put on a single Effect Rune");
        builder.pop();

        builder.push("Build Blitz");
        this.BUILD_BLITZ_PERCENT = buildDouble(builder, "BUILD_BLITZ_PERCENT", 20.0, 0.0, 100.0, "Percent of a schematic's missing blocks the Build Blitz (sacrifice upgrade) fires at once, using blocks from the player's inventory");
        this.BUILD_BLITZ_COOLDOWN_MINUTES = buildInt(builder, "BUILD_BLITZ_COOLDOWN_MINUTES", 120, 0, Integer.MAX_VALUE, "Minutes the Build Blitz takes to recharge after firing, before upgrades. Blocks that fail to be placed refund their share of this time");
        builder.pop();

        builder.push("Dev Tools");
        this.SELECTION_MAX_VOLUME = buildInt(builder, "SELECTION_MAX_VOLUME", 262144, 1, Integer.MAX_VALUE, "Maximum number of blocks the Scanner Stick (warns), Chronosphere Stick and Structure Wand can select at once");
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
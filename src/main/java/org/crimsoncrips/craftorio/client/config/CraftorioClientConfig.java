package org.crimsoncrips.craftorio.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CraftorioClientConfig {

    public final ModConfigSpec.IntValue POINT_FORMATTING;
    public final ModConfigSpec.BooleanValue SKIP_CONTRACT_CLAIM_ANIMATION;
    public final ModConfigSpec.IntValue GOLD_RAIN_INTENSITY;
    public final ModConfigSpec.IntValue WELCOME_TOAST_SECONDS;
    public final ModConfigSpec.BooleanValue SKILL_TREE_EFFECTS;
    public final ModConfigSpec.BooleanValue PHOTOSENSITIVE_MODE;
    public final ModConfigSpec.BooleanValue WARNING_ENABLED;
    public final ModConfigSpec.ConfigValue<String> DEV_TOOLS_MOD_ID;

    public CraftorioClientConfig(final ModConfigSpec.Builder builder) {

        builder.push("General");
        this.POINT_FORMATTING = buildInt(builder, "POINT_FORMATTING", 2,0,3, "Point formatting (0 = 100000,1 = 1e5,2 = 100k,3 = 100 Thousand)");
        this.SKIP_CONTRACT_CLAIM_ANIMATION = buildBoolean(builder, "SKIP_CONTRACT_CLAIM_ANIMATION", false, "Skip the spin/reveal animation on the contract claiming screen and show contracts already settled");
        this.GOLD_RAIN_INTENSITY = buildInt(builder, "GOLD_RAIN_INTENSITY", 100, 0, 300, "Scales how many gold ingots rain down when winning a Double Or Nothing bet, as a percentage (0 = disabled, 100 = default, 300 = triple)");
        this.WELCOME_TOAST_SECONDS = buildInt(builder, "WELCOME_TOAST_SECONDS", 10, 1, Integer.MAX_VALUE, "How many seconds the welcome toast stays on screen before sliding out");
        this.SKILL_TREE_EFFECTS = buildBoolean(builder, "SKILL_TREE_EFFECTS", true, "Whether the animated space background renders behind the skill trees");

        builder.pop();

        builder.push("Dev Tools");
        this.DEV_TOOLS_MOD_ID = builder.comment("Mod id filled in automatically by the dev tools (leave empty for none)").translation("DEV_TOOLS_MOD_ID").define("DEV_TOOLS_MOD_ID", "");
        builder.pop();

        builder.push("Accessibility");
        this.PHOTOSENSITIVE_MODE = buildBoolean(builder, "PHOTOSENSITIVE_MODE", false, "Photosensitivity warning: replaces the flashing static, glitch bars and eye shader behind the sacrifice screen shatter with solid black");
        this.WARNING_ENABLED = buildBoolean(builder, "WARNING_ENABLED", true, "Shows a photosensitivity warning toast when joining a world");
        builder.pop();
    }


    public String devToolsModId() {
        return DEV_TOOLS_MOD_ID.get().trim();
    }

    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

}
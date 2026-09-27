package org.crimsoncrips.craftorio.skill_tree.target;

public record ActionEffectValue(String effect, double chance) {

    public static final String DEFAULT_EFFECT = "craftorio:productive";
    public static final double DEFAULT_CHANCE = 0.2;

    public static ActionEffectValue parse(String raw) {
        String text = raw == null ? "" : raw.trim();
        int separator = text.indexOf('|');
        String effect = (separator >= 0 ? text.substring(0, separator) : text).trim();
        double chance = DEFAULT_CHANCE;
        if (separator >= 0) {
            try {
                chance = Double.parseDouble(text.substring(separator + 1).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return new ActionEffectValue(effect.isEmpty() ? DEFAULT_EFFECT : effect, chance);
    }

    public String format() {
        return effect + "|" + chance;
    }
}

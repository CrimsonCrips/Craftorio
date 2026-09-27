package org.crimsoncrips.craftorio.server.shop;

import net.minecraft.network.chat.Component;

public final class ShopTooltipOverride {

    private static Component line;

    private ShopTooltipOverride() {}

    public static void set(Component override) {
        line = override;
    }

    public static Component get() {
        return line;
    }

    public static void clear() {
        line = null;
    }
}

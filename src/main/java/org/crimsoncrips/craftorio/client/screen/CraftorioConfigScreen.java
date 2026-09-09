package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.Craftorio;

@OnlyIn(Dist.CLIENT)
public class CraftorioConfigScreen extends Screen {

    private static final String[] FORMAT_KEYS = {"format_raw", "format_scientific", "format_short_suffix", "format_worded"};

    private final Screen parent;

    public CraftorioConfigScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.config_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(formatButtonLabel(), this::cycleFormat)
                .bounds(centerX - 100, centerY - 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - 100, centerY + 10, 200, 20).build());
    }

    private void cycleFormat(Button button) {
        int next = (Craftorio.CLIENT_CONFIG.POINT_FORMATTING.get() + 1) % FORMAT_KEYS.length;
        Craftorio.CLIENT_CONFIG.POINT_FORMATTING.set(next);
        button.setMessage(formatButtonLabel());
    }

    private Component formatButtonLabel() {
        String formatName = Component.translatable("misc.craftorio." + FORMAT_KEYS[Craftorio.CLIENT_CONFIG.POINT_FORMATTING.get()]).getString();
        return Component.translatable("misc.craftorio.point_formatting_label", formatName);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}

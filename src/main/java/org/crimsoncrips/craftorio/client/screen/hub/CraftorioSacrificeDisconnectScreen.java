package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioSacrificeDisconnectScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int MAX_TEXT_WIDTH = 300;

    private List<FormattedCharSequence> lines = List.of();
    private int titleTop;
    private int textTop;

    public CraftorioSacrificeDisconnectScreen() {
        super(Component.translatable("disconnect.lost"));
    }

    @Override
    protected void init() {
        this.lines = this.font.split(Component.translatable("misc.craftorio.sacrifice_disconnect"), MAX_TEXT_WIDTH);

        int textHeight = this.lines.size() * (this.font.lineHeight + 1);
        int top = (this.height - (this.font.lineHeight + 12 + textHeight + 20 + BUTTON_HEIGHT)) / 2;
        this.titleTop = top;
        this.textTop = top + this.font.lineHeight + 12;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.toTitle"), b -> this.minecraft.setScreen(new TitleScreen()))
                .bounds((this.width - BUTTON_WIDTH) / 2, this.textTop + textHeight + 20, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleTop, 0xAAAAAA);

        int y = this.textTop;
        for (FormattedCharSequence line : this.lines) {
            guiGraphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFFFFF);
            y += this.font.lineHeight + 1;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

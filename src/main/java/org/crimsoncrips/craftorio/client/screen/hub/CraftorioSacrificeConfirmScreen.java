package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.sacrifice.SacrificeAnswerPacket;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioSacrificeConfirmScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 190;
    private static final int PADDING = 12;

    private int panelLeft;
    private int panelTop;
    private int panelHeight = PANEL_HEIGHT;
    private boolean universal = true;
    private List<FormattedCharSequence> lines = List.of();
    private boolean newSeed = false;

    public CraftorioSacrificeConfirmScreen() {
        super(Component.translatable("misc.craftorio.sacrifice_confirm_title"));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.universal = this.minecraft.level == null || CraftorioMisc.universalBased(this.minecraft.level);
        this.panelHeight = this.universal ? PANEL_HEIGHT : PANEL_HEIGHT - 26;
        this.panelTop = (this.height - this.panelHeight) / 2;
        this.lines = this.font.split(Component.translatable(this.universal ? "misc.craftorio.sacrifice_confirm_description" : "misc.craftorio.sacrifice_confirm_description_individual"), PANEL_WIDTH - PADDING * 2);

        int buttonWidth = (PANEL_WIDTH - PADDING * 2 - 6) / 2;
        int buttonY = this.panelTop + this.panelHeight - PADDING - 20;

        if (this.universal) {
            this.addRenderableWidget(Button.builder(newSeedLabel(), b -> {
                this.newSeed = !this.newSeed;
                b.setMessage(newSeedLabel());
            }).bounds(this.panelLeft + PADDING, buttonY - 26, PANEL_WIDTH - PADDING * 2, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.sacrifice_confirm_proceed").withStyle(ChatFormatting.RED), b -> answer(true))
                .bounds(this.panelLeft + PADDING, buttonY, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.sacrifice_confirm_refuse"), b -> answer(false))
                .bounds(this.panelLeft + PADDING + buttonWidth + 6, buttonY, buttonWidth, 20).build());
    }

    private Component newSeedLabel() {
        return Component.translatable("misc.craftorio.sacrifice_confirm_new_seed", this.newSeed ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private void answer(boolean accept) {
        PacketDistributor.sendToServer(new SacrificeAnswerPacket(accept, this.newSeed));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + this.panelHeight, 0xE0202020);
        guiGraphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, this.panelHeight, 0xFF808080);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 10, 0xFF5555);

        int y = this.panelTop + 28;
        for (FormattedCharSequence line : this.lines) {
            guiGraphics.drawString(this.font, line, this.panelLeft + PADDING, y, 0xFFFFFF, false);
            y += this.font.lineHeight + 2;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

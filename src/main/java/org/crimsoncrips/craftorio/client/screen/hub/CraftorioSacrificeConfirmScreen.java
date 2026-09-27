package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Difficulty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.client.screen.consent.ClientConsentState;
import org.crimsoncrips.craftorio.client.screen.consent.CraftorioConsentWaitScreen;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.networking.sacrifice.SacrificeAnswerPacket;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioSacrificeConfirmScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 190;
    private static final int PADDING = 12;

    private int panelLeft;
    private int panelTop;
    private int panelHeight = PANEL_HEIGHT;
    private boolean universal = true;
    private List<FormattedCharSequence> lines = List.of();
    private EditBox seedBox;
    private String seedText = "";
    private int difficulty = CraftorioSacrifice.KEEP_DIFFICULTY;

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
        this.panelHeight = this.universal ? PANEL_HEIGHT + 26 : PANEL_HEIGHT - 26;
        this.panelTop = (this.height - this.panelHeight) / 2;
        this.lines = this.font.split(Component.translatable(this.universal ? "misc.craftorio.sacrifice_confirm_description" : "misc.craftorio.sacrifice_confirm_description_individual"), PANEL_WIDTH - PADDING * 2);

        int buttonWidth = (PANEL_WIDTH - PADDING * 2 - 6) / 2;
        int buttonY = this.panelTop + this.panelHeight - PADDING - 20;

        if (this.universal) {
            this.seedBox = new EditBox(this.font, this.panelLeft + PADDING + 1, buttonY - 51, PANEL_WIDTH - PADDING * 2 - 2, 18,
                    Component.translatable("misc.craftorio.sacrifice_confirm_seed_hint"));
            this.seedBox.setMaxLength(SacrificeAnswerPacket.MAX_SEED_LENGTH);
            this.seedBox.setHint(Component.translatable("misc.craftorio.sacrifice_confirm_seed_hint").withStyle(ChatFormatting.DARK_GRAY));
            this.seedBox.setValue(this.seedText);
            this.seedBox.setResponder(value -> this.seedText = value);
            this.addRenderableWidget(this.seedBox);

            this.addRenderableWidget(Button.builder(difficultyLabel(), b -> {
                this.difficulty = this.difficulty >= Difficulty.HARD.getId() ? CraftorioSacrifice.KEEP_DIFFICULTY : this.difficulty + 1;
                b.setMessage(difficultyLabel());
            }).bounds(this.panelLeft + PADDING, buttonY - 26, PANEL_WIDTH - PADDING * 2, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.sacrifice_confirm_proceed").withStyle(ChatFormatting.RED), b -> answer(true))
                .bounds(this.panelLeft + PADDING, buttonY, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.sacrifice_confirm_refuse"), b -> answer(false))
                .bounds(this.panelLeft + PADDING + buttonWidth + 6, buttonY, buttonWidth, 20).build());
    }

    private Component difficultyLabel() {
        Component value = this.difficulty == CraftorioSacrifice.KEEP_DIFFICULTY
                ? Component.translatable("misc.craftorio.sacrifice_difficulty_keep")
                : Difficulty.byId(this.difficulty).getDisplayName();
        return Component.translatable("misc.craftorio.sacrifice_difficulty", value);
    }

    private void answer(boolean accept) {
        PacketDistributor.sendToServer(new SacrificeAnswerPacket(accept, this.universal ? this.seedText : "", this.difficulty));
        if (accept && this.universal && ClientConsentState.othersOnline()) {
            this.minecraft.setScreen(new CraftorioConsentWaitScreen(ConsentKind.SACRIFICE, true));
        } else {
            this.onClose();
        }
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

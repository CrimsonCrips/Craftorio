package org.crimsoncrips.craftorio.client.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.server.config.CraftorioWorldCreationOverrides;

@OnlyIn(Dist.CLIENT)
public class CraftorioWorldCreationScreen extends Screen {

    private final Screen parent;

    private boolean universalProgression;
    private boolean chunkBasedExpansion;
    private boolean noBorders;

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 240;
    private final int panelHeight = 130;

    private Button universalButton;
    private Button chunkBasedButton;
    private Button noBordersButton;

    public CraftorioWorldCreationScreen(Screen parent, boolean universalProgression, boolean chunkBasedExpansion, boolean noBorders) {
        super(Component.translatable("misc.craftorio.world_creation_settings_title"));
        this.parent = parent;
        this.universalProgression = universalProgression;
        this.chunkBasedExpansion = chunkBasedExpansion;
        this.noBorders = noBorders;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        int x = panelLeft + 10;
        int width = panelWidth - 20;
        int y = panelTop + 24;
        int rowHeight = 22;

        this.universalButton = Button.builder(universalProgressionLabel(), b -> {
            universalProgression = !universalProgression;
            b.setMessage(universalProgressionLabel());
        }).bounds(x, y, width, 16).build();
        this.addRenderableWidget(this.universalButton);
        y += rowHeight;

        this.chunkBasedButton = Button.builder(chunkBasedLabel(), b -> {
            chunkBasedExpansion = !chunkBasedExpansion;
            b.setMessage(chunkBasedLabel());
        }).bounds(x, y, width, 16).build();
        this.addRenderableWidget(this.chunkBasedButton);
        y += rowHeight;

        this.noBordersButton = Button.builder(noBordersLabel(), b -> {
            noBorders = !noBorders;
            b.setMessage(noBordersLabel());
        }).bounds(x, y, width, 16).build();
        this.addRenderableWidget(this.noBordersButton);
        y += rowHeight + 8;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> {
            CraftorioWorldCreationOverrides.set(universalProgression, chunkBasedExpansion, noBorders);
            this.minecraft.setScreen(this.parent);
        }).bounds(x, y, width, 20).build());
    }

    private Component universalProgressionLabel() {
        return Component.translatable("misc.craftorio.world_creation_universal_progression", universalProgression);
    }

    private Component chunkBasedLabel() {
        return Component.translatable("misc.craftorio.world_creation_chunk_based", chunkBasedExpansion);
    }

    private Component noBordersLabel() {
        return Component.translatable("misc.craftorio.world_creation_no_borders", noBorders);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xE0202020);
        guiGraphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF808080);
        guiGraphics.drawCenteredString(this.font, this.title, panelLeft + panelWidth / 2, panelTop + 8, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioBasicSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioRebirthSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioSacrificeSkillTreeScreen;

@OnlyIn(Dist.CLIENT)
public class CraftorioSkillTreeMenuScreen extends Screen {

    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 136;
    private static final int PADDING = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;

    private final Screen parent;
    private int panelLeft;
    private int panelTop;

    public CraftorioSkillTreeMenuScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.skill_trees_title"));
        this.parent = parent;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.panelTop = (this.height - PANEL_HEIGHT) / 2;

        int buttonWidth = PANEL_WIDTH - PADDING * 2;
        int x = this.panelLeft + PADDING;
        int y = this.panelTop + 26;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.skill_tree_title"), b -> this.minecraft.setScreen(new CraftorioBasicSkillTreeScreen("misc.craftorio.skill_tree_title")))
                .bounds(x, y, buttonWidth, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.rebirth_skill_tree_title"), b -> this.minecraft.setScreen(new CraftorioRebirthSkillTreeScreen()))
                .bounds(x, y, buttonWidth, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.sacrifice_skill_tree_title"), b -> this.minecraft.setScreen(new CraftorioSacrificeSkillTreeScreen()))
                .bounds(x, y, buttonWidth, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE + 4;
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(x, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + PANEL_HEIGHT, 0xE0202020);
        guiGraphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF808080);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 10, 0xFFFFFF);
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

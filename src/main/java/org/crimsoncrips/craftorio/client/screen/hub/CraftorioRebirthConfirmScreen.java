package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.skill_tree.RequestRebirthPacket;
import org.crimsoncrips.craftorio.server.rebirth.CraftorioRebirth;

import java.math.BigInteger;

@OnlyIn(Dist.CLIENT)
public class CraftorioRebirthConfirmScreen extends Screen {

    private final Screen parent;
    private final int currentLife;
    private final int maxSkip;

    private boolean skipEnabled = false;
    private Button skipToggleButton;
    private Button confirmButton;

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 260;
    private final int panelHeight = 160;

    public CraftorioRebirthConfirmScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.rebirth_confirm_title"));
        this.parent = parent;
        var player = Minecraft.getInstance().player;
        this.currentLife = player != null ? CraftorioMisc.getLife(player) : 1;
        BigInteger points = player != null ? CraftorioMisc.getPoints(player) : BigInteger.ZERO;
        this.maxSkip = Math.max(0, CraftorioRebirth.maxAffordableSkip(this.currentLife, points));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        int centerX = panelLeft + panelWidth / 2;
        int y = panelTop + 70;

        this.skipToggleButton = Button.builder(skipLabel(), b -> {
            skipEnabled = !skipEnabled;
            b.setMessage(skipLabel());
            refresh();
        }).bounds(centerX - 90, y, 180, 20).build();
        this.skipToggleButton.active = maxSkip > 0;
        this.addRenderableWidget(this.skipToggleButton);

        y += 34;

        this.confirmButton = Button.builder(Component.translatable("misc.craftorio.rebirth_confirm_button"), b -> {
            PacketDistributor.sendToServer(new RequestRebirthPacket(skipCount()));
            this.minecraft.setScreen(null);
        }).bounds(centerX - 90, y, 180, 20).build();
        this.addRenderableWidget(this.confirmButton);

        y += 24;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.cancel"), b -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 90, y, 180, 20).build());

        refresh();
    }

    private int skipCount() {
        return skipEnabled ? maxSkip : 0;
    }

    private Component skipLabel() {
        return Component.translatable(skipEnabled ? "misc.craftorio.rebirth_skip_on" : "misc.craftorio.rebirth_skip_off", maxSkip);
    }

    private void refresh() {
        BigInteger points = this.minecraft.player != null ? CraftorioMisc.getPoints(this.minecraft.player) : BigInteger.ZERO;
        BigInteger cost = CraftorioRebirth.skipCost(currentLife, skipCount());
        this.confirmButton.active = points.compareTo(cost) >= 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xE0202020);
        guiGraphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF808080);

        int centerX = panelLeft + panelWidth / 2;
        guiGraphics.drawCenteredString(this.font, this.title, centerX, panelTop + 8, 0xFFFFFF);

        BigInteger cost = CraftorioRebirth.skipCost(currentLife, skipCount());
        BigInteger earned = CraftorioRebirth.lifePointsEarned(skipCount());

        int y = panelTop + 24;
        guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.rebirth_current_life", currentLife), centerX, y, 0xAAAAAA);
        y += 14;
        guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.rebirth_cost_label",
                CraftorioMisc.bigIntFormat(cost)), centerX, y, 0xFFFF55);
        y += 14;
        guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.rebirth_life_points_label", earned.toString()), centerX, y, 0xFFFF55);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

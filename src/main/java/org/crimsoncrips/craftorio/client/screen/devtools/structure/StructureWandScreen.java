package org.crimsoncrips.craftorio.client.screen.devtools.structure;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.item.structure.StructureWandActions;
import org.crimsoncrips.craftorio.item.structure.StructureWandSettings;
import org.crimsoncrips.craftorio.networking.devtools.StructureWandActionPacket;

@OnlyIn(Dist.CLIENT)
public class StructureWandScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 316;
    private static final int PANEL_HEIGHT = 112;
    private static final int PADDING = 8;

    private final InteractionHand hand;
    private String name;

    private int panelLeft;
    private int panelTop;
    private EditBox nameBox;

    public StructureWandScreen(InteractionHand hand, StructureWandSettings settings) {
        super(Component.translatable("item.craftorio.structure_wand"));
        this.hand = hand;
        this.name = settings.name();
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.panelTop = (this.height - PANEL_HEIGHT) / 2;

        int left = this.panelLeft + PADDING;
        int innerWidth = PANEL_WIDTH - PADDING * 2;
        int y = this.panelTop + 52;

        this.nameBox = new EditBox(this.font, left, y, innerWidth, 18, Component.translatable("structure_block.structure_name"));
        this.nameBox.setMaxLength(128);
        this.nameBox.setValue(this.name);
        this.nameBox.setResponder(value -> this.name = value);
        this.addRenderableWidget(this.nameBox);
        y += 32;

        this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), b -> send(StructureWandActions.SAVE))
                .bounds(left, y, innerWidth, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.panelLeft + PANEL_WIDTH / 2 - 50, this.panelTop + PANEL_HEIGHT + 6, 100, 20).build());
    }

    private void send(int action) {
        if (this.nameBox != null) this.name = this.nameBox.getValue();
        PacketDistributor.sendToServer(new StructureWandActionPacket(this.hand, action, new StructureWandSettings(this.name.trim())));
    }

    @Override
    public void onClose() {
        send(StructureWandActions.STORE);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + PANEL_HEIGHT, 0xE0202020);
        graphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF808080);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 8, 0xFFFFFF);

        int left = this.panelLeft + PADDING;
        graphics.drawString(this.font, Component.translatable("structure_block.mode_info.save"), left, this.panelTop + 24, 0xA0A0A0, false);
        graphics.drawString(this.font, Component.translatable("structure_block.structure_name"), left, this.panelTop + 42, 0xA0A0A0, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

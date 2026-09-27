package org.crimsoncrips.craftorio.client.screen.devtools.structure;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsDropdown;
import org.crimsoncrips.craftorio.item.structure.StructureWandActions;
import org.crimsoncrips.craftorio.item.structure.StructureWandSettings;
import org.crimsoncrips.craftorio.networking.devtools.StructureWandActionPacket;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StructureWandScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 316;
    private static final int PADDING = 8;
    private static final Component[] ROTATION_LABELS = {
            Component.literal("0"), Component.literal("90"), Component.literal("180"), Component.literal("270")
    };

    private final InteractionHand hand;
    private boolean loadMode;
    private String name;
    private Rotation rotation;
    private Mirror mirror;
    private String integrity;
    private String seed;

    private int panelLeft;
    private int panelTop;
    private int panelHeight;
    private DevToolsDropdown mirrorDropdown;
    private EditBox nameBox;
    private EditBox integrityBox;
    private EditBox seedBox;

    public StructureWandScreen(InteractionHand hand, StructureWandSettings settings) {
        super(Component.translatable("item.craftorio.structure_wand"));
        this.hand = hand;
        this.loadMode = settings.loadMode();
        this.name = settings.name();
        this.rotation = settings.rotation();
        this.mirror = settings.mirror();
        this.integrity = String.valueOf(settings.integrity());
        this.seed = String.valueOf(settings.seed());
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelHeight = this.loadMode ? 202 : 132;
        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        int left = this.panelLeft + PADDING;
        int innerWidth = PANEL_WIDTH - PADDING * 2;
        int y = this.panelTop + 22;

        this.addRenderableWidget(Button.builder(modeLabel(), b -> {
            captureFields();
            this.loadMode = !this.loadMode;
            this.rebuildWidgets();
        }).bounds(left, y, innerWidth, 20).build());
        y += 50;

        this.nameBox = new EditBox(this.font, left, y, innerWidth, 18, Component.translatable("structure_block.structure_name"));
        this.nameBox.setMaxLength(128);
        this.nameBox.setValue(this.name);
        this.nameBox.setResponder(value -> this.name = value);
        this.addRenderableWidget(this.nameBox);
        y += 32;


        if (this.loadMode) {
            int half = (innerWidth - 4) / 2;
            this.integrityBox = new EditBox(this.font, left, y + 10, half, 18, Component.translatable("structure_block.integrity.integrity"));
            this.integrityBox.setValue(this.integrity);
            this.integrityBox.setResponder(value -> this.integrity = value);
            this.addRenderableWidget(this.integrityBox);
            this.seedBox = new EditBox(this.font, left + half + 4, y + 10, half, 18, Component.translatable("structure_block.integrity.seed"));
            this.seedBox.setMaxLength(20);
            this.seedBox.setValue(this.seed);
            this.seedBox.setResponder(value -> this.seed = value);
            this.addRenderableWidget(this.seedBox);
            y += 36;

            int rotationWidth = 40;
            for (int i = 0; i < Rotation.values().length; i++) {
                Rotation value = Rotation.values()[i];
                Button button = Button.builder(ROTATION_LABELS[i], b -> {
                    this.rotation = value;
                    this.rebuildWidgets();
                }).bounds(left + i * (rotationWidth + 4), y, rotationWidth, 20).build();
                button.active = this.rotation != value;
                this.addRenderableWidget(button);
            }
            List<Component> mirrorLabels = new ArrayList<>();
            for (Mirror option : Mirror.values()) {
                mirrorLabels.add(option.symbol());
            }
            this.mirrorDropdown = new DevToolsDropdown(this.font, left + innerWidth - 90, y, 90, 20, mirrorLabels, this.mirror.ordinal(), index -> this.mirror = Mirror.values()[index]);
            this.addRenderableWidget(this.mirrorDropdown);
            y += 28;

            this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), b -> send(StructureWandActions.LOAD))
                    .bounds(left, y, innerWidth, 20).build());
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), b -> send(StructureWandActions.SAVE))
                    .bounds(left, y, innerWidth, 20).build());
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.panelLeft + PANEL_WIDTH / 2 - 50, this.panelTop + this.panelHeight + 6, 100, 20).build());
    }

    private void captureFields() {
        if (this.nameBox != null) this.name = this.nameBox.getValue();
        if (this.integrityBox != null) this.integrity = this.integrityBox.getValue();
        if (this.seedBox != null) this.seed = this.seedBox.getValue();
    }

    private Component modeLabel() {
        return Component.translatable(this.loadMode ? "structure_block.mode.load" : "structure_block.mode.save");
    }

    private StructureWandSettings settings() {
        float parsedIntegrity;
        try {
            parsedIntegrity = Float.parseFloat(this.integrity.trim());
        } catch (NumberFormatException e) {
            parsedIntegrity = 1.0F;
        }
        long parsedSeed;
        try {
            parsedSeed = Long.parseLong(this.seed.trim());
        } catch (NumberFormatException e) {
            parsedSeed = 0L;
        }
        return new StructureWandSettings(this.loadMode, this.name.trim(), this.rotation, this.mirror, parsedIntegrity, parsedSeed);
    }

    private void send(int action) {
        captureFields();
        PacketDistributor.sendToServer(new StructureWandActionPacket(this.hand, action, settings()));
    }

    @Override
    public void onClose() {
        send(StructureWandActions.STORE);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + this.panelHeight, 0xE0202020);
        graphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, this.panelHeight, 0xFF808080);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 8, 0xFFFFFF);

        int left = this.panelLeft + PADDING;
        int y = this.panelTop + 22;
        graphics.drawString(this.font, Component.translatable(this.loadMode ? "structure_block.mode_info.load" : "structure_block.mode_info.save"), left, y + 25, 0xA0A0A0, false);
        y += 50;
        graphics.drawString(this.font, Component.translatable("structure_block.structure_name"), left, y - 10, 0xA0A0A0, false);
        y += 32;
        if (this.loadMode) {
            graphics.drawString(this.font, Component.translatable("structure_block.integrity"), left, y, 0xA0A0A0, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.mirrorDropdown != null) {
            DevToolsDropdown.renderAll(List.of(this.mirrorDropdown), graphics, mouseX, mouseY, this.width, this.height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.mirrorDropdown != null && DevToolsDropdown.handleClicks(List.of(this.mirrorDropdown), mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

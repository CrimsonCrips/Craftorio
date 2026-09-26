package org.crimsoncrips.craftorio.client.screen.devtools;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.client.screen.devtools.creator.EffectCreatorScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.creator.SkillTreeCreatorScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.creator.UpgradeCreatorScreen;
import org.crimsoncrips.craftorio.networking.devtools.GiveScannerStickPacket;
import org.crimsoncrips.craftorio.networking.devtools.OpenContractCreatorPacket;

@OnlyIn(Dist.CLIENT)
public class DevToolsScreen extends Screen {

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;
    private static final int BUTTON_COUNT = 6;
    private static final int DONE_EXTRA_GAP = 16;

    private final Screen parent;

    public DevToolsScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.dev_tools_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int blockHeight = (BUTTON_COUNT - 1) * BUTTON_STRIDE + BUTTON_HEIGHT + DONE_EXTRA_GAP + BUTTON_STRIDE;
        int y = (this.height - blockHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_create_contract"), b -> PacketDistributor.sendToServer(new OpenContractCreatorPacket()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_create_effect"), b -> this.minecraft.setScreen(new EffectCreatorScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_create_upgrade"), b -> this.minecraft.setScreen(new UpgradeCreatorScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_create_skill_tree"), b -> this.minecraft.setScreen(new SkillTreeCreatorScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_edit_skill_tree"), b ->
                DevToolsUpgradeTrees.openTreePicker(this.minecraft, this, pickedTree -> {
                    SkillTreeCreatorScreen screen = new SkillTreeCreatorScreen(this);
                    screen.loadFromRegistry(this.minecraft, pickedTree);
                    this.minecraft.setScreen(screen);
                })).bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_get_scanner_stick"), b -> PacketDistributor.sendToServer(new GiveScannerStickPacket()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE + DONE_EXTRA_GAP;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

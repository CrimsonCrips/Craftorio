package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.GenerateEffectCodePacket;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EffectCreatorScreen extends Screen {

    private static final String[] TYPES = {"general", "shop", "tag"};

    private final Screen parent;
    private final DevToolsHelpPanel helpPanel = new DevToolsHelpPanel();
    private DevToolsTimeConverterPanel timeConverterPanel;

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 260;
    private final int panelHeight = 344;

    private int typeIndex = 0;
    private Button typeButton;

    private EditBox idBox;
    private EditBox modIdBox;
    private EditBox multiplierBox;
    private EditBox secondsBox;
    private EditBox weightBox;
    private EditBox itemTagBox;
    private boolean unobtainable = false;
    private Button unobtainableButton;
    private boolean includeLang = false;
    private Button includeLangButton;
    private EditBox nameBox;
    private boolean jsonExport = false;
    private Button exportButton;

    public EffectCreatorScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.effect_creator_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        int fieldX = panelLeft + 90;
        int fieldWidth = panelWidth - 100;
        int y = panelTop + 24;
        int rowHeight = 22;

        this.typeButton = Button.builder(Component.literal(TYPES[typeIndex]), b -> {
            typeIndex = (typeIndex + 1) % TYPES.length;
            typeButton.setMessage(Component.literal(TYPES[typeIndex]));
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.typeButton);
        y += rowHeight;

        this.idBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("id"));
        this.idBox.setMaxLength(256);
        this.addRenderableWidget(this.idBox);
        y += rowHeight;

        this.modIdBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("mod id"));
        this.modIdBox.setMaxLength(256);
        this.addRenderableWidget(this.modIdBox);
        y += rowHeight;

        this.multiplierBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("multiplier"));
        this.multiplierBox.setMaxLength(256);
        this.addRenderableWidget(this.multiplierBox);
        y += rowHeight;

        this.secondsBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("seconds"));
        this.secondsBox.setMaxLength(256);
        this.addRenderableWidget(this.secondsBox);
        y += rowHeight;

        this.timeConverterPanel = new DevToolsTimeConverterPanel(this.font, this.secondsBox);
        for (var widget : this.timeConverterPanel.widgets()) {
            this.addRenderableWidget(widget);
        }

        this.weightBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("weight"));
        this.weightBox.setMaxLength(256);
        this.addRenderableWidget(this.weightBox);
        y += rowHeight;

        this.unobtainableButton = Button.builder(Component.literal(String.valueOf(unobtainable)), b -> {
            unobtainable = !unobtainable;
            unobtainableButton.setMessage(Component.literal(String.valueOf(unobtainable)));
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.unobtainableButton);
        y += rowHeight;

        this.itemTagBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("item tag"));
        this.itemTagBox.setMaxLength(256);
        this.addRenderableWidget(this.itemTagBox);
        y += rowHeight;

        this.includeLangButton = Button.builder(Component.literal(String.valueOf(includeLang)), b -> {
            includeLang = !includeLang;
            includeLangButton.setMessage(Component.literal(String.valueOf(includeLang)));
            refreshLangVisibility();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.includeLangButton);
        y += rowHeight;

        this.nameBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("name"));
        this.nameBox.setMaxLength(256);
        this.addRenderableWidget(this.nameBox);
        y += rowHeight + 8;

        refreshLangVisibility();

        this.exportButton = Button.builder(exportLabel(), b -> {
            jsonExport = !jsonExport;
            exportButton.setMessage(exportLabel());
        }).bounds(panelLeft + panelWidth / 2 - 90, y, 180, 20).build();
        this.addRenderableWidget(this.exportButton);
        y += 26;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_generate"), b -> generate())
                .bounds(panelLeft + panelWidth / 2 - 90, y, 180, 20).build());
        y += 26;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(panelLeft + panelWidth / 2 - 90, y, 180, 20).build());

        this.addRenderableWidget(this.helpPanel.createButton(this.width, 6, this.timeConverterPanel::closeIfOpen));
        this.addRenderableWidget(this.timeConverterPanel.createToggleButton(this.width, 30, this.helpPanel::closeIfOpen));
    }

    private Component exportLabel() {
        return Component.translatable(jsonExport ? "misc.craftorio.dev_tools_export_json" : "misc.craftorio.dev_tools_export_code");
    }

    private void refreshLangVisibility() {
        this.nameBox.visible = includeLang;
        this.nameBox.active = includeLang;
    }

    private void generate() {
        PacketDistributor.sendToServer(new GenerateEffectCodePacket(
                TYPES[typeIndex],
                idBox.getValue(),
                modIdBox.getValue(),
                multiplierBox.getValue(),
                secondsBox.getValue(),
                weightBox.getValue(),
                unobtainable,
                itemTagBox.getValue(),
                includeLang,
                nameBox.getValue(),
                jsonExport
        ));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xE0202020);
        guiGraphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF808080);

        guiGraphics.drawCenteredString(this.font, this.title, panelLeft + panelWidth / 2, panelTop + 8, 0xFFFFFF);

        int labelX = panelLeft + 8;
        int y = panelTop + 24;
        int rowHeight = 22;
        String[] labelKeys = {
                "dev_tools_label_type", "dev_tools_label_id", "dev_tools_label_mod_id", "dev_tools_label_multiplier", "dev_tools_label_seconds",
                "dev_tools_label_weight", "dev_tools_label_unobtainable", "dev_tools_label_item_tag", "dev_tools_label_include_lang"
        };
        for (String key : labelKeys) {
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio." + key), labelX, y + 4, 0xAAAAAA, false);
            y += rowHeight;
        }
        if (includeLang) {
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_name"), labelX, y + 4, 0xAAAAAA, false);
        }
        y += rowHeight;

        this.timeConverterPanel.updateAndRender(guiGraphics, this.width, 54);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.idBox, "e.g. my_effect");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.modIdBox, "e.g. yourmodid");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.multiplierBox, "e.g. 2.0");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.secondsBox, "e.g. 60");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.weightBox, "e.g. 10");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.itemTagBox, "e.g. craftorio:copper");
        if (includeLang) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.nameBox, "e.g. My Effect");
        }
        this.timeConverterPanel.renderHints(guiGraphics);

        this.helpPanel.render(guiGraphics, this.font, this.width, 54, List.of(
                Component.translatable("misc.craftorio.dev_tools_help_location"),
                Component.translatable("misc.craftorio.dev_tools_help_export")
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

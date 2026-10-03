package org.crimsoncrips.craftorio.client.screen.devtools.creator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.registries.effect.EffectOperation;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsTagPicker;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsDropdown;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsHelpPanel;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsPickerScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsTimeConverterPanel;
import org.crimsoncrips.craftorio.networking.devtools.GenerateEffectCodePacket;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EffectCreatorScreen extends Screen implements ScrollableScreen {

    private static final String[] TYPES = {"general", "shop", "tag"};

    private final Screen parent;
    private final DevToolsHelpPanel helpPanel = new DevToolsHelpPanel();
    private DevToolsTimeConverterPanel timeConverterPanel;

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 260;
    private final int panelHeight = 366;

    private int typeIndex = 0;
    private int operationIndex = 0;
    private DevToolsDropdown typeDropdown;
    private final List<DevToolsDropdown> dropdowns = new ArrayList<>();

    private EditBox idBox;
    private EditBox modIdBox;
    private EditBox multiplierBox;
    private EditBox secondsBox;
    private EditBox weightBox;
    private EditBox itemTagBox;
    private Button selectTagButton;
    private boolean unobtainable = false;
    private Button unobtainableButton;
    private boolean includeLang = false;
    private Button includeLangButton;
    private EditBox nameBox;
    private boolean jsonExport = false;
    private Button exportButton;

    private String prefillId = "";
    private String prefillModId = "";
    private String prefillMultiplier = "";
    private String prefillSeconds = "";
    private String prefillWeight = "";
    private String prefillItemTag = "";
    private String prefillName = "";

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

        this.dropdowns.clear();
        List<Component> typeLabels = new ArrayList<>();
        for (String type : TYPES) {
            typeLabels.add(Component.literal(type));
        }
        this.typeDropdown = new DevToolsDropdown(this.font, fieldX, y, fieldWidth, 16, typeLabels, typeIndex, index -> {
            typeIndex = index;
            refreshItemTagVisibility();
        });
        this.dropdowns.add(this.typeDropdown);
        this.addRenderableWidget(this.typeDropdown);
        y += rowHeight;

        this.idBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("id"));
        this.idBox.setMaxLength(256);
        this.addRenderableWidget(this.idBox);
        y += rowHeight;

        this.modIdBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("mod id"));
        this.modIdBox.setMaxLength(256);
        this.addRenderableWidget(this.modIdBox);
        y += rowHeight;

        List<Component> operationLabels = new ArrayList<>();
        for (EffectOperation operation : EffectOperation.values()) {
            operationLabels.add(Component.translatable("misc.craftorio.dev_tools_operation_" + operation.getSerializedName()));
        }
        DevToolsDropdown operationDropdown = new DevToolsDropdown(this.font, fieldX, y, fieldWidth, 16, operationLabels, operationIndex, index -> operationIndex = index);
        this.dropdowns.add(operationDropdown);
        this.addRenderableWidget(operationDropdown);
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

        this.unobtainableButton = Button.builder(Component.literal(String.valueOf(unobtainable)), b -> {
            unobtainable = !unobtainable;
            unobtainableButton.setMessage(Component.literal(String.valueOf(unobtainable)));
            refreshWeightVisibility();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.unobtainableButton);
        y += rowHeight;

        this.weightBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("weight"));
        this.weightBox.setMaxLength(256);
        this.addRenderableWidget(this.weightBox);
        y += rowHeight;

        this.itemTagBox = new EditBox(this.font, fieldX, y, fieldWidth - 64, 16, Component.literal("item tag"));
        this.itemTagBox.setMaxLength(256);
        this.addRenderableWidget(this.itemTagBox);
        this.selectTagButton = Button.builder(Component.translatable("misc.craftorio.dev_tools_select_tag"), b -> {
            captureFields();
            DevToolsTagPicker.open(this.minecraft, this, tag -> this.prefillItemTag = tag);
        })
                .bounds(fieldX + fieldWidth - 60, y, 60, 16).build();
        this.addRenderableWidget(this.selectTagButton);
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

        this.idBox.setValue(prefillId);
        this.modIdBox.setValue(prefillModId.isBlank() ? Craftorio.CLIENT_CONFIG.devToolsModId() : prefillModId);
        this.multiplierBox.setValue(prefillMultiplier);
        this.secondsBox.setValue(prefillSeconds);
        this.weightBox.setValue(prefillWeight);
        this.itemTagBox.setValue(prefillItemTag);
        this.nameBox.setValue(prefillName);

        refreshLangVisibility();
        refreshItemTagVisibility();
        refreshWeightVisibility();

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

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_edit_effects"), b -> openEditPicker())
                .bounds(this.width - 108, this.height - 28, 100, 20).build());

        this.addRenderableWidget(this.helpPanel.createButton(this.width, 6, this.timeConverterPanel::closeIfOpen));
        this.addRenderableWidget(this.timeConverterPanel.createToggleButton(this.width, 30, this.helpPanel::closeIfOpen));
    }

    private void captureFields() {
        this.prefillId = this.idBox.getValue();
        this.prefillModId = this.modIdBox.getValue();
        this.prefillMultiplier = this.multiplierBox.getValue();
        this.prefillSeconds = this.secondsBox.getValue();
        this.prefillWeight = this.weightBox.getValue();
        this.prefillItemTag = this.itemTagBox.getValue();
        this.prefillName = this.nameBox.getValue();
    }

    private void openEditPicker() {
        if (this.minecraft.level == null) return;

        Registry<CraftorioEffects> registry = this.minecraft.level.registryAccess().registryOrThrow(CraftorioEffects.REGISTRY_KEY);
        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        for (Holder.Reference<CraftorioEffects> holder : registry.holders().toList()) {
            ResourceLocation id = holder.key().location();
            CraftorioEffects effect = holder.value();
            options.add(new DevToolsPickerScreen.Option(Component.literal(effect.getActualName()), id.toString(), 0, true, () -> {
                loadEffect(id, effect);
                this.minecraft.setScreen(this);
            }));
        }

        this.minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_effect"), this, options));
    }

    private void loadEffect(ResourceLocation id, CraftorioEffects effect) {
        this.prefillId = id.getPath();
        this.prefillModId = id.getNamespace();
        this.prefillSeconds = String.valueOf(effect.getTime() / CraftorioMisc.SECONDS_TO_TICKS);
        this.prefillWeight = String.valueOf(effect.getWeight());
        this.unobtainable = effect.isUnobtainable();
        this.operationIndex = effect.getOperation().ordinal();
        this.prefillItemTag = "";

        if (effect instanceof TagMultiplierEffect tagEffect) {
            this.typeIndex = 2;
            this.prefillMultiplier = String.valueOf(tagEffect.getMultiplier());
            this.prefillItemTag = tagEffect.getItemTag().location().toString();
        } else if (effect instanceof ShopMultiplierEffect shopEffect) {
            this.typeIndex = 1;
            this.prefillMultiplier = String.valueOf(shopEffect.getMultiplier());
        } else if (effect instanceof GeneralMultiplierEffect generalEffect) {
            this.typeIndex = 0;
            this.prefillMultiplier = String.valueOf(generalEffect.getMultiplier());
        }

        String translated = Component.translatable(effect.getNameKey()).getString();
        boolean hasTranslation = !translated.equals(effect.getNameKey());
        this.includeLang = hasTranslation;
        this.prefillName = hasTranslation ? translated : "";
    }

    private Component exportLabel() {
        return Component.translatable(jsonExport ? "misc.craftorio.dev_tools_export_json" : "misc.craftorio.dev_tools_export_code");
    }

    private boolean usesItemTag() {
        return TYPES[typeIndex].equals("tag");
    }

    private void refreshItemTagVisibility() {
        boolean uses = usesItemTag();
        this.itemTagBox.visible = uses;
        this.itemTagBox.active = uses;
        this.selectTagButton.visible = uses;
        this.selectTagButton.active = uses;
    }

    private void refreshWeightVisibility() {
        this.weightBox.visible = !unobtainable;
        this.weightBox.active = !unobtainable;
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
                jsonExport,
                EffectOperation.values()[operationIndex].name()
        ));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (DevToolsDropdown.handleClicks(this.dropdowns, mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
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
                "dev_tools_label_type", "dev_tools_label_id", "dev_tools_label_mod_id", "dev_tools_label_operation", "dev_tools_label_value_" + EffectOperation.values()[operationIndex].getSerializedName(), "dev_tools_label_seconds",
                "dev_tools_label_unobtainable", "dev_tools_label_weight", "dev_tools_label_item_tag", "dev_tools_label_include_lang"
        };
        for (String key : labelKeys) {
            boolean show = switch (key) {
                case "dev_tools_label_item_tag" -> usesItemTag();
                case "dev_tools_label_weight" -> !unobtainable;
                default -> true;
            };
            if (show) {
                guiGraphics.drawString(this.font, Component.translatable("misc.craftorio." + key), labelX, y + 4, 0xAAAAAA, false);
            }
            y += rowHeight;
        }
        if (includeLang) {
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_name"), labelX, y + 4, 0xAAAAAA, false);
        }
        y += rowHeight;

        this.timeConverterPanel.updateAndRender(guiGraphics, this.width, 54);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        DevToolsDropdown.renderAll(this.dropdowns, guiGraphics, mouseX, mouseY, this.width, this.height);

        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.idBox, "e.g. my_effect");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.modIdBox, "e.g. yourmodid");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.multiplierBox, "e.g. 2.0");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.secondsBox, "e.g. 60");
        if (!unobtainable) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.weightBox, "e.g. 10");
        }
        if (usesItemTag()) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.itemTagBox, "e.g. craftorio:copper");
        }
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

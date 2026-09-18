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
import org.crimsoncrips.craftorio.networking.GenerateUpgradeCodePacket;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class UpgradeCreatorScreen extends Screen {

    private static final String[] CATEGORIES = {"modifier", "attribute", "action_effect"};
    private static final String[] MODIFIER_TARGETS = {
            "MULTIPLIER", "ITEM_BASE_VALUE", "ITEM_TAG_BASE_VALUE", "CONTRACT_REFRESH_SPEED", "EFFECT_TIMER_SPEED",
            "PUNISHMENT_DURATION", "EFFECT_DURATION", "EXPANSION_COST",
            "RARER_CONTRACT_CHANCE", "RARER_EFFECT_CHANCE", "SHOP_COST", "CONTRACT_REFRESH_COST",
            "LOST_BET_REFUND", "MULT_PER_CONTRACT_DONE", "BET_ODDS", "BET_BONUS", "MANUAL_SINK_VALUE"
    };
    private static final String[] ATTRIBUTE_TARGETS = {
            "HEALTH", "SPEED", "DEFENSE", "DAMAGE", "BLOCK_REACH", "JUMP_HEIGHT", "XP_GAIN", "RESISTANCE"
    };
    private static final String[] PLAYER_ACTION_TARGETS = {"WAKE_UP", "TRADE"};
    private static final String[] OPERATIONS = {"ADD", "MULTIPLY"};

    private final Screen parent;
    private final DevToolsHelpPanel helpPanel = new DevToolsHelpPanel();

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 280;
    private final int panelHeight = 390;

    private int categoryIndex = 0;
    private int modifierTargetIndex = 0;
    private int attributeTargetIndex = 0;
    private int playerActionTargetIndex = 0;
    private int operationIndex = 0;
    private Button categoryButton;
    private Button modifierTargetButton;
    private Button attributeTargetButton;
    private Button playerActionTargetButton;
    private Button operationButton;

    private EditBox idBox;
    private EditBox modIdBox;
    private EditBox descriptionBox;
    private EditBox costBox;
    private EditBox maxPurchasesBox;
    private EditBox parentBox;
    private EditBox valueBox;
    private EditBox itemTagBox;
    private boolean includeLang = false;
    private Button includeLangButton;
    private EditBox nameBox;
    private boolean jsonExport = false;
    private Button exportButton;

    public UpgradeCreatorScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.upgrade_creator_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        int fieldX = panelLeft + 100;
        int fieldWidth = panelWidth - 110;
        int y = panelTop + 24;
        int rowHeight = 22;

        this.categoryButton = Button.builder(Component.literal(CATEGORIES[categoryIndex]), b -> {
            categoryIndex = (categoryIndex + 1) % CATEGORIES.length;
            categoryButton.setMessage(Component.literal(CATEGORIES[categoryIndex]));
            updateTargetVisibility();
            updateItemTagVisibility();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.categoryButton);
        y += rowHeight;

        this.idBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("id"));
        this.idBox.setMaxLength(256);
        this.addRenderableWidget(this.idBox);
        y += rowHeight;

        this.modIdBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("mod id"));
        this.modIdBox.setMaxLength(256);
        this.addRenderableWidget(this.modIdBox);
        y += rowHeight;

        this.descriptionBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("description"));
        this.descriptionBox.setMaxLength(256);
        this.addRenderableWidget(this.descriptionBox);
        y += rowHeight;

        this.costBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("cost"));
        this.costBox.setMaxLength(256);
        this.addRenderableWidget(this.costBox);
        y += rowHeight;

        this.maxPurchasesBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("max purchases"));
        this.maxPurchasesBox.setMaxLength(256);
        this.maxPurchasesBox.setValue("1");
        this.addRenderableWidget(this.maxPurchasesBox);
        y += rowHeight;

        this.parentBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("parent"));
        this.parentBox.setMaxLength(256);
        this.addRenderableWidget(this.parentBox);
        y += rowHeight;

        this.modifierTargetButton = Button.builder(Component.literal(MODIFIER_TARGETS[modifierTargetIndex]), b -> {
            modifierTargetIndex = (modifierTargetIndex + 1) % MODIFIER_TARGETS.length;
            modifierTargetButton.setMessage(Component.literal(MODIFIER_TARGETS[modifierTargetIndex]));
            updateItemTagVisibility();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.modifierTargetButton);

        this.attributeTargetButton = Button.builder(Component.literal(ATTRIBUTE_TARGETS[attributeTargetIndex]), b -> {
            attributeTargetIndex = (attributeTargetIndex + 1) % ATTRIBUTE_TARGETS.length;
            attributeTargetButton.setMessage(Component.literal(ATTRIBUTE_TARGETS[attributeTargetIndex]));
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.attributeTargetButton);

        this.playerActionTargetButton = Button.builder(Component.literal(PLAYER_ACTION_TARGETS[playerActionTargetIndex]), b -> {
            playerActionTargetIndex = (playerActionTargetIndex + 1) % PLAYER_ACTION_TARGETS.length;
            playerActionTargetButton.setMessage(Component.literal(PLAYER_ACTION_TARGETS[playerActionTargetIndex]));
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.playerActionTargetButton);

        updateTargetVisibility();
        y += rowHeight;

        this.operationButton = Button.builder(Component.literal(OPERATIONS[operationIndex]), b -> {
            operationIndex = (operationIndex + 1) % OPERATIONS.length;
            operationButton.setMessage(Component.literal(OPERATIONS[operationIndex]));
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.operationButton);
        y += rowHeight;

        this.valueBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("value"));
        this.valueBox.setMaxLength(256);
        this.addRenderableWidget(this.valueBox);
        y += rowHeight;

        this.itemTagBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("item tag"));
        this.itemTagBox.setMaxLength(256);
        this.addRenderableWidget(this.itemTagBox);
        updateItemTagVisibility();
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

        this.addRenderableWidget(this.helpPanel.createButton(this.width, 6, () -> {}));
    }

    private Component exportLabel() {
        return Component.translatable(jsonExport ? "misc.craftorio.dev_tools_export_json" : "misc.craftorio.dev_tools_export_code");
    }

    private boolean isModifierCategory() {
        return CATEGORIES[categoryIndex].equals("modifier");
    }

    private boolean isActionEffectCategory() {
        return CATEGORIES[categoryIndex].equals("action_effect");
    }

    private void updateTargetVisibility() {
        boolean isModifier = isModifierCategory();
        boolean isActionEffect = isActionEffectCategory();
        boolean isAttribute = !isModifier && !isActionEffect;

        this.modifierTargetButton.visible = isModifier;
        this.modifierTargetButton.active = isModifier;
        this.attributeTargetButton.visible = isAttribute;
        this.attributeTargetButton.active = isAttribute;
        this.playerActionTargetButton.visible = isActionEffect;
        this.playerActionTargetButton.active = isActionEffect;

        if (this.operationButton != null) {
            this.operationButton.visible = !isActionEffect;
            this.operationButton.active = !isActionEffect;
        }
    }

    private boolean usesItemTag() {
        return isModifierCategory() && MODIFIER_TARGETS[modifierTargetIndex].equals("ITEM_TAG_BASE_VALUE");
    }

    private boolean isTickDurationTarget() {
        if (!isModifierCategory()) return false;
        String target = MODIFIER_TARGETS[modifierTargetIndex];
        return target.equals("CONTRACT_REFRESH_SPEED") || target.equals("EFFECT_TIMER_SPEED")
                || target.equals("PUNISHMENT_DURATION") || target.equals("EFFECT_DURATION");
    }

    private String valueHint() {
        if (isActionEffectCategory()) {
            return "e.g. craftorio:productive";
        }
        if (isTickDurationTarget() && OPERATIONS[operationIndex].equals("ADD")) {
            return "e.g. 10 (seconds)";
        }
        return "e.g. 0.1";
    }

    private void updateItemTagVisibility() {
        boolean uses = usesItemTag();
        this.itemTagBox.visible = uses;
        this.itemTagBox.active = uses;
    }

    private void refreshLangVisibility() {
        this.nameBox.visible = includeLang;
        this.nameBox.active = includeLang;
        this.descriptionBox.visible = includeLang;
        this.descriptionBox.active = includeLang;
    }

    private void generate() {
        String selectedTarget = isModifierCategory() ? MODIFIER_TARGETS[modifierTargetIndex]
                : isActionEffectCategory() ? PLAYER_ACTION_TARGETS[playerActionTargetIndex]
                : ATTRIBUTE_TARGETS[attributeTargetIndex];
        PacketDistributor.sendToServer(new GenerateUpgradeCodePacket(
                CATEGORIES[categoryIndex],
                idBox.getValue(),
                modIdBox.getValue(),
                descriptionBox.getValue(),
                costBox.getValue(),
                maxPurchasesBox.getValue(),
                parentBox.getValue(),
                selectedTarget,
                OPERATIONS[operationIndex],
                valueBox.getValue(),
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
        String targetLabelKey = isModifierCategory() ? "dev_tools_label_target"
                : isActionEffectCategory() ? "dev_tools_label_target_action"
                : "dev_tools_label_target_attribute";
        String[] labelKeys = {
                "dev_tools_label_type", "dev_tools_label_id", "dev_tools_label_mod_id", "dev_tools_label_description", "dev_tools_label_cost",
                "dev_tools_label_max_purchases", "dev_tools_label_parent", targetLabelKey,
                "dev_tools_label_operation", "dev_tools_label_value", "dev_tools_label_item_tag_target", "dev_tools_label_include_lang"
        };
        for (String key : labelKeys) {
            boolean show = switch (key) {
                case "dev_tools_label_item_tag_target" -> usesItemTag();
                case "dev_tools_label_description" -> includeLang;
                case "dev_tools_label_operation" -> !isActionEffectCategory();
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

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.idBox, "e.g. my_upgrade");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.modIdBox, "e.g. yourmodid");
        if (includeLang) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.descriptionBox, "e.g. My upgrade.");
        }
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.costBox, "e.g. 1000 or 1e6");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.maxPurchasesBox, "e.g. 1");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.parentBox, "e.g. craftorio:root");
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.valueBox, valueHint());
        if (usesItemTag()) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.itemTagBox, "e.g. craftorio:copper");
        }
        if (includeLang) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.nameBox, "e.g. My Upgrade");
        }

        this.helpPanel.render(guiGraphics, this.font, this.width, 30, List.of(
                Component.translatable("misc.craftorio.dev_tools_help_location"),
                Component.translatable("misc.craftorio.dev_tools_help_export")
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

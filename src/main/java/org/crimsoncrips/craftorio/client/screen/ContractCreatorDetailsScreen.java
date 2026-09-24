package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.ClientContractCreatorDraftState;
import org.crimsoncrips.craftorio.networking.GenerateContractCodePacket;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItemReward;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContractCreatorDetailsScreen extends Screen {

    private static final String KEY_ID = "contract_id";
    private static final String KEY_MOD_ID = "contract_mod_id";
    private static final String KEY_SECONDS = "contract_seconds";
    private static final String KEY_BASE_VALUE = "contract_base_value";
    private static final String KEY_WEIGHT = "contract_weight";
    private static final String KEY_CLAIM_THRESHOLD = "contract_claim_threshold";
    private static final String KEY_MIN_THRESHOLD = "contract_min_threshold";
    private static final String KEY_MAX_THRESHOLD = "contract_max_threshold";
    private static final String KEY_PUNISHMENT = "contract_punishment";
    private static final String KEY_REQUIRED_MOD = "contract_required_mod";
    private static final String KEY_CARD_TEXTURE = "contract_card_texture";
    private static final String KEY_JSON_EXPORT = "contract_json_export";
    private static final String KEY_INCLUDE_LANG = "contract_include_lang";
    private static final String KEY_TITLE = "contract_title";
    private static final String KEY_DESCRIPTION = "contract_description";

    private final ContractCreatorRewardScreen parent;
    private final DevToolsHelpPanel helpPanel = new DevToolsHelpPanel();
    private DevToolsTimeConverterPanel timeConverterPanel;

    private int panelLeft;
    private int panelTop;
    private final int panelWidth = 320;
    private final int panelHeight = 378;

    private EditBox idBox;
    private EditBox modIdBox;
    private EditBox secondsBox;
    private EditBox basePointValueBox;
    private EditBox weightBox;
    private EditBox claimPointThresholdBox;
    private EditBox minPointThresholdBox;
    private EditBox maxPointThresholdBox;
    private EditBox punishmentBox;
    private EditBox requiredModIdBox;
    private EditBox cardTextureBox;
    private boolean jsonExport;
    private Button exportButton;
    private boolean includeLang;
    private Button includeLangButton;
    private EditBox titleBox;
    private EditBox descriptionBox;

    private final List<EditBox> hintedBoxes = new ArrayList<>();
    private final List<String> hintTexts = new ArrayList<>();

    public static void loadIntoDraft(ResourceLocation id, CraftorioContract contract) {
        ClientContractCreatorDraftState.set(KEY_ID, id.getPath());
        ClientContractCreatorDraftState.set(KEY_MOD_ID, id.getNamespace());
        ClientContractCreatorDraftState.set(KEY_SECONDS, String.valueOf(contract.getTime() / CraftorioMisc.SECONDS_TO_TICKS));
        ClientContractCreatorDraftState.set(KEY_BASE_VALUE, CraftorioMisc.toScientificString(contract.getBasePointValue()));
        ClientContractCreatorDraftState.set(KEY_WEIGHT, String.valueOf(contract.getWeight()));
        ClientContractCreatorDraftState.set(KEY_CLAIM_THRESHOLD, CraftorioMisc.toScientificString(contract.getPointThreshold()));
        ClientContractCreatorDraftState.set(KEY_MIN_THRESHOLD, CraftorioMisc.toScientificString(contract.getMinPointThreshold()));
        ClientContractCreatorDraftState.set(KEY_MAX_THRESHOLD, CraftorioMisc.toScientificString(contract.getMaxPointThreshold()));
        ClientContractCreatorDraftState.set(KEY_PUNISHMENT, contract.getPunishment() != null ? contract.getPunishment().toString() : "");
        ClientContractCreatorDraftState.set(KEY_REQUIRED_MOD, contract.getRequiredModId() != null ? contract.getRequiredModId() : "");
        ClientContractCreatorDraftState.set(KEY_CARD_TEXTURE, contract.getCardTexture() != null ? contract.getCardTexture().location().toString() : "");

        String translatedTitle = Component.translatable(contract.getName()).getString();
        String translatedDescription = Component.translatable(contract.getDescription()).getString();
        boolean hasTranslation = !translatedTitle.equals(contract.getName());
        ClientContractCreatorDraftState.set(KEY_INCLUDE_LANG, String.valueOf(hasTranslation));
        ClientContractCreatorDraftState.set(KEY_TITLE, hasTranslation ? translatedTitle : "");
        ClientContractCreatorDraftState.set(KEY_DESCRIPTION, hasTranslation && !translatedDescription.equals(contract.getDescription()) ? translatedDescription : "");

        int rolls = contract.getRewards().stream().mapToInt(CraftorioContractItemReward::getRandomEffectCount).max().orElse(0);
        ContractCreatorRewardScreen.setRewardRollsValue(String.valueOf(rolls));
    }

    public ContractCreatorDetailsScreen(ContractCreatorRewardScreen parent) {
        super(Component.translatable("misc.craftorio.contract_creator_details_title"));
        this.parent = parent;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.hintedBoxes.clear();
        this.hintTexts.clear();
        this.jsonExport = ClientContractCreatorDraftState.get(KEY_JSON_EXPORT, "false").equals("true");
        this.includeLang = ClientContractCreatorDraftState.get(KEY_INCLUDE_LANG, "false").equals("true");

        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;

        int fieldX = panelLeft + 132;
        int fieldWidth = panelWidth - 142;
        int y = panelTop + 24;
        int rowHeight = 22;

        this.idBox = newBox(fieldX, y, fieldWidth, KEY_ID, "e.g. my_contract");
        y += rowHeight;
        this.modIdBox = newBox(fieldX, y, fieldWidth, KEY_MOD_ID, "e.g. yourmodid");
        y += rowHeight;
        this.secondsBox = newBox(fieldX, y, fieldWidth, KEY_SECONDS, "e.g. 600");
        y += rowHeight;

        this.timeConverterPanel = new DevToolsTimeConverterPanel(this.font, this.secondsBox);
        for (var widget : this.timeConverterPanel.widgets()) {
            this.addRenderableWidget(widget);
        }
        this.basePointValueBox = newBox(fieldX, y, fieldWidth, KEY_BASE_VALUE, "e.g. 1000 or 1e6");
        y += rowHeight;
        this.weightBox = newBox(fieldX, y, fieldWidth, KEY_WEIGHT, "e.g. 10");
        y += rowHeight;
        this.claimPointThresholdBox = newBox(fieldX, y, fieldWidth, KEY_CLAIM_THRESHOLD, "e.g. 0 or 1e8");
        y += rowHeight;
        this.minPointThresholdBox = newBox(fieldX, y, fieldWidth, KEY_MIN_THRESHOLD, "e.g. 0");
        y += rowHeight;
        this.maxPointThresholdBox = newBox(fieldX, y, fieldWidth, KEY_MAX_THRESHOLD, "e.g. 1000000 or 1e20");
        y += rowHeight;
        this.punishmentBox = newBox(fieldX, y, fieldWidth, KEY_PUNISHMENT, "e.g. craftorio:general/some_punishment");
        y += rowHeight;
        this.requiredModIdBox = newBox(fieldX, y, fieldWidth, KEY_REQUIRED_MOD, "e.g. create");
        y += rowHeight;
        this.cardTextureBox = newBox(fieldX, y, fieldWidth, KEY_CARD_TEXTURE, "e.g. craftorio:default");
        y += rowHeight;

        this.includeLangButton = Button.builder(Component.literal(String.valueOf(includeLang)), b -> {
            includeLang = !includeLang;
            ClientContractCreatorDraftState.set(KEY_INCLUDE_LANG, String.valueOf(includeLang));
            includeLangButton.setMessage(Component.literal(String.valueOf(includeLang)));
            refreshLangVisibility();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.includeLangButton);
        y += rowHeight;

        this.titleBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal(""));
        this.titleBox.setMaxLength(256);
        this.titleBox.setValue(ClientContractCreatorDraftState.get(KEY_TITLE, ""));
        this.titleBox.setResponder(value -> ClientContractCreatorDraftState.set(KEY_TITLE, value));
        this.addRenderableWidget(this.titleBox);
        y += rowHeight;

        this.descriptionBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal(""));
        this.descriptionBox.setMaxLength(256);
        this.descriptionBox.setValue(ClientContractCreatorDraftState.get(KEY_DESCRIPTION, ""));
        this.descriptionBox.setResponder(value -> ClientContractCreatorDraftState.set(KEY_DESCRIPTION, value));
        this.addRenderableWidget(this.descriptionBox);
        y += rowHeight + 8;

        refreshLangVisibility();

        this.exportButton = Button.builder(exportLabel(), b -> {
            jsonExport = !jsonExport;
            ClientContractCreatorDraftState.set(KEY_JSON_EXPORT, String.valueOf(jsonExport));
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
        this.titleBox.visible = includeLang;
        this.titleBox.active = includeLang;
        this.descriptionBox.visible = includeLang;
        this.descriptionBox.active = includeLang;
    }

    private EditBox newBox(int x, int y, int width, String stateKey, String hint) {
        EditBox box = new EditBox(this.font, x, y, width, 16, Component.literal(""));
        box.setMaxLength(256);
        box.setValue(ClientContractCreatorDraftState.get(stateKey, ""));
        box.setResponder(value -> ClientContractCreatorDraftState.set(stateKey, value));
        this.addRenderableWidget(box);
        this.hintedBoxes.add(box);
        this.hintTexts.add(hint);
        return box;
    }

    private void generate() {
        PacketDistributor.sendToServer(new GenerateContractCodePacket(
                idBox.getValue(),
                modIdBox.getValue(),
                secondsBox.getValue(),
                basePointValueBox.getValue(),
                weightBox.getValue(),
                claimPointThresholdBox.getValue(),
                minPointThresholdBox.getValue(),
                maxPointThresholdBox.getValue(),
                punishmentBox.getValue(),
                requiredModIdBox.getValue(),
                ContractCreatorRewardScreen.getRewardRollsValue(),
                includeLang,
                titleBox.getValue(),
                descriptionBox.getValue(),
                cardTextureBox.getValue(),
                jsonExport
        ));
    }

    @Override
    public void onClose() {
        if (this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        super.onClose();
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
                "dev_tools_label_id", "dev_tools_label_mod_id", "dev_tools_label_seconds", "dev_tools_label_reward_points", "dev_tools_label_weight",
                "dev_tools_label_claim_threshold", "dev_tools_label_offer_min", "dev_tools_label_offer_max",
                "dev_tools_label_punishment", "dev_tools_label_required_mod", "dev_tools_label_card_texture", "dev_tools_label_include_lang"
        };
        for (String key : labelKeys) {
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio." + key), labelX, y + 4, 0xAAAAAA, false);
            y += rowHeight;
        }
        if (includeLang) {
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_title"), labelX, y + 4, 0xAAAAAA, false);
            guiGraphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_description"), labelX, y + rowHeight + 4, 0xAAAAAA, false);
        }
        y += rowHeight * 2;

        this.timeConverterPanel.updateAndRender(guiGraphics, this.width, 54);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < hintedBoxes.size(); i++) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, hintedBoxes.get(i), hintTexts.get(i));
        }
        if (includeLang) {
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.titleBox, "e.g. My Contract");
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.descriptionBox, "e.g. Turn in some items.");
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

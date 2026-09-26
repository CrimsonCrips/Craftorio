package org.crimsoncrips.craftorio.client.screen.devtools.contract_creator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsHelpPanel;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsPickerScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsScreen;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;
import org.crimsoncrips.craftorio.networking.devtools.LoadContractIntoCreatorPacket;
import org.crimsoncrips.craftorio.registries.contract.ContractTextColors;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractTexture;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.lwjgl.glfw.GLFW;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ContractCreatorCardScreen extends Screen implements MenuAccess<ContractCreatorMenu> {

    private enum Region {
        TITLE(24, 17, 100, 12, "dev_tools_contract_region_title", false, ContractCreatorDraft.TITLE_COLOR, 0xA9A9A9),
        TIME(33, 36, 82, 12, "dev_tools_contract_region_time", false, ContractCreatorDraft.TIME_COLOR, 0xFFD966),
        DESCRIPTION(33, 55, 82, 12, "dev_tools_contract_region_description", false, ContractCreatorDraft.DESCRIPTION_COLOR, 0xA9A9A9),
        PUNISHMENT(33, 100, 82, 12, "dev_tools_contract_region_punishment", false, ContractCreatorDraft.PUNISHMENT_COLOR, 0xFF0000),
        MIN(18, 123, 15, 37, "dev_tools_contract_region_min", true, ContractCreatorDraft.MIN_THRESHOLD, 0),
        THRESHOLD(40, 123, 68, 37, "dev_tools_contract_region_threshold", true, ContractCreatorDraft.CLAIM_THRESHOLD, 0),
        MAX(115, 123, 15, 37, "dev_tools_contract_region_max", true, ContractCreatorDraft.MAX_THRESHOLD, 0);

        final int x, y, w, h;
        final String labelKey;
        final boolean inline;
        final String key;
        final int defaultColor;

        Region(int x, int y, int w, int h, String labelKey, boolean inline, String key, int defaultColor) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.labelKey = labelKey;
            this.inline = inline;
            this.key = key;
            this.defaultColor = defaultColor;
        }
    }

    private static final int TEXTURE_WIDTH = 150;
    private static final int TEXTURE_HEIGHT = 180;
    private static final int CARD_TOP = 28;
    private static final int PARCHMENT_LEFT = 8;
    private static final int PARCHMENT_TOP = 6;
    private static final int PARCHMENT_RIGHT = 140;
    private static final int PARCHMENT_BOTTOM = 171;
    private static final int BOTTOM_AREA = 34;
    private static final float MAX_SCALE = 2.5f;
    private static final float SIZE_FACTOR = 0.6f;
    private static final float HOVER_SCALE = 1.05f;
    private static final float HOVER_LERP = 0.25f;
    private static final float TEXT_SCALE = 0.8f;

    private static final long PANEL_ANIM_MS = 250L;
    private static final int PANEL_WIDTH = 190;
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_TOP = 28;
    private static final int PANEL_PADDING = 8;
    private static final int ROW = 22;
    private static final int SWATCH_SIZE = 12;
    private static final int SWATCH_GAP = 2;
    private static final int SWATCH_COLUMNS = 8;
    private static final int[] SWATCHES = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    private final ContractCreatorMenu menu;
    private final DevToolsHelpPanel helpPanel = new DevToolsHelpPanel();

    private float scale = 1f;
    private float hoverScale = 1f;
    private int cardWidth;
    private int cardHeight;

    private Region panelRegion;
    private Region shownRegion;
    private boolean panelOpen = false;
    private long panelAnimStart = 0L;
    private float panelAnimFrom = 0f;

    private Region inlineRegion;
    private EditBox inlineBox;

    private final Map<Region, List<AbstractWidget>> panelWidgets = new EnumMap<>(Region.class);
    private final Map<Region, EditBox> colorBoxes = new EnumMap<>(Region.class);
    private final List<PanelLabel> panelLabels = new ArrayList<>();
    private final List<HintedBox> hints = new ArrayList<>();
    private Button exportButton;

    private final Map<AbstractWidget, Integer> offsets = new IdentityHashMap<>();
    private final Map<AbstractWidget, Integer> verticalOffsets = new IdentityHashMap<>();

    private record PanelLabel(Region region, String key, int offsetY) {}

    private record HintedBox(Region region, EditBox box, String hint) {}

    public ContractCreatorCardScreen(ContractCreatorMenu menu, Inventory inventory, Component title) {
        super(Component.translatable("misc.craftorio.contract_creator_title"));
        this.menu = menu;
    }

    @Override
    public ContractCreatorMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void init() {
        this.panelWidgets.clear();
        this.colorBoxes.clear();
        this.panelLabels.clear();
        this.hints.clear();
        this.offsets.clear();
        this.verticalOffsets.clear();

        this.scale = Mth.clamp(Math.min((this.height - CARD_TOP - BOTTOM_AREA) / (float) TEXTURE_HEIGHT,
                (this.width - PANEL_WIDTH - PANEL_MARGIN * 3) / (float) TEXTURE_WIDTH), 0.75f, MAX_SCALE) * SIZE_FACTOR;
        this.cardWidth = Math.round(TEXTURE_WIDTH * this.scale);
        this.cardHeight = Math.round(TEXTURE_HEIGHT * this.scale);

        buildTextPanel(Region.TITLE, ContractCreatorDraft.TITLE, "dev_tools_label_title", "e.g. My Contract");
        buildTimePanel();
        buildTextPanel(Region.DESCRIPTION, ContractCreatorDraft.DESCRIPTION, "dev_tools_label_description", "e.g. Turn in some items.");
        buildTextPanel(Region.PUNISHMENT, ContractCreatorDraft.PUNISHMENT, "dev_tools_label_punishment", "e.g. craftorio:general/some_punishment");
        for (List<AbstractWidget> widgets : this.panelWidgets.values()) {
            for (AbstractWidget widget : widgets) {
                this.offsets.put(widget, widget.getX());
                this.verticalOffsets.put(widget, widget.getY());
                this.addRenderableWidget(widget);
            }
        }
        positionPanelWidgetsVertically();

        this.inlineBox = new EditBox(this.font, 0, 0, 100, 16, Component.literal(""));
        this.inlineBox.setMaxLength(256);
        this.inlineBox.visible = false;
        this.inlineBox.active = false;
        this.addRenderableWidget(this.inlineBox);
        if (this.inlineRegion != null) {
            openInline(this.inlineRegion);
        }

        int gap = 4;
        int rowY = this.height - 24;
        Component generateLabel = Component.translatable("misc.craftorio.dev_tools_generate");
        Component editLabel = Component.translatable("misc.craftorio.dev_tools_edit_contracts");
        Component backLabel = Component.translatable("misc.craftorio.back");
        int exportWidth = Math.max(fitWidth(Component.translatable("misc.craftorio.dev_tools_export_json")), fitWidth(Component.translatable("misc.craftorio.dev_tools_export_code")));
        int generateWidth = fitWidth(generateLabel);
        int editWidth = fitWidth(editLabel);
        int backWidth = fitWidth(backLabel);
        int x = this.width / 2 - (exportWidth + generateWidth + editWidth + backWidth + gap * 3) / 2;

        this.exportButton = Button.builder(exportLabel(), b -> {
            ContractCreatorDraft.set(ContractCreatorDraft.JSON_EXPORT, String.valueOf(!ContractCreatorDraft.jsonExport()));
            this.exportButton.setMessage(exportLabel());
        }).bounds(x, rowY, exportWidth, 18).build();
        this.addRenderableWidget(this.exportButton);
        x += exportWidth + gap;

        this.addRenderableWidget(Button.builder(generateLabel, b -> ContractCreatorDraft.generate())
                .bounds(x, rowY, generateWidth, 18).build());
        x += generateWidth + gap;
        this.addRenderableWidget(Button.builder(editLabel, b -> openEditPicker())
                .bounds(x, rowY, editWidth, 18).build());
        x += editWidth + gap;
        this.addRenderableWidget(Button.builder(backLabel, b -> {
            if (this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
            this.minecraft.setScreen(new DevToolsScreen(null));
        }).bounds(x, rowY, backWidth, 18).build());

        this.addRenderableWidget(this.helpPanel.createButton(this.width, 4, true, () -> {}));

        updatePanelWidgets();
    }

    private void buildTextPanel(Region region, String key, String labelKey, String hint) {
        List<AbstractWidget> widgets = new ArrayList<>();
        int y = 18;
        this.panelLabels.add(new PanelLabel(region, labelKey, y));
        y += 11;
        widgets.add(panelBox(region, key, y, PANEL_WIDTH - PANEL_PADDING * 2, hint));
        y += ROW + 4;
        buildColorSection(region, widgets, y);
        this.panelWidgets.put(region, widgets);
    }

    private void buildTimePanel() {
        Region region = Region.TIME;
        List<AbstractWidget> widgets = new ArrayList<>();
        int y = 18;
        this.panelLabels.add(new PanelLabel(region, "dev_tools_label_seconds", y));
        y += 11;
        EditBox secondsBox = panelBox(region, ContractCreatorDraft.SECONDS, y, PANEL_WIDTH - PANEL_PADDING * 2, "e.g. 600");
        widgets.add(secondsBox);
        y += ROW + 4;

        this.panelLabels.add(new PanelLabel(region, "dev_tools_contract_time_convert", y));
        y += 11;
        int half = (PANEL_WIDTH - PANEL_PADDING * 2 - 4) / 2;
        EditBox hoursBox = new EditBox(this.font, 0, y, half, 16, Component.literal(""));
        hoursBox.setMaxLength(16);
        EditBox minutesBox = new EditBox(this.font, half + 4, y, half, 16, Component.literal(""));
        minutesBox.setMaxLength(16);
        widgets.add(hoursBox);
        widgets.add(minutesBox);
        this.hints.add(new HintedBox(region, hoursBox, "Hours"));
        this.hints.add(new HintedBox(region, minutesBox, "Minutes"));
        y += ROW - 2;
        Component applyLabel = Component.translatable("misc.craftorio.dev_tools_time_convert_apply");
        Button apply = Button.builder(applyLabel, b ->
                secondsBox.setValue(String.valueOf(parseInt(hoursBox.getValue()) * 3600 + parseInt(minutesBox.getValue()) * 60)))
                .bounds(0, y, fitWidth(applyLabel), 16).build();
        widgets.add(apply);
        y += ROW + 4;

        buildColorSection(region, widgets, y);
        this.panelWidgets.put(region, widgets);
    }

    private void buildColorSection(Region region, List<AbstractWidget> widgets, int y) {
        this.panelLabels.add(new PanelLabel(region, "dev_tools_label_text_color", y));
        y += 11;
        int resetWidth = fitWidth(Component.translatable("misc.craftorio.dev_tools_contract_color_reset"));
        EditBox colorBox = panelBox(region, region.key, y, PANEL_WIDTH - PANEL_PADDING * 2 - resetWidth - 4, ContractTextColors.toHex(region.defaultColor));
        colorBox.setMaxLength(9);
        widgets.add(colorBox);
        this.colorBoxes.put(region, colorBox);
        widgets.add(Button.builder(Component.translatable("misc.craftorio.dev_tools_contract_color_reset"), b -> colorBox.setValue(""))
                .bounds(PANEL_WIDTH - PANEL_PADDING * 2 - resetWidth, y, resetWidth, 16).build());
    }

    private EditBox panelBox(Region region, String key, int y, int width, String hint) {
        EditBox box = new EditBox(this.font, 0, y, width, 16, Component.literal(""));
        box.setMaxLength(256);
        box.setValue(ContractCreatorDraft.get(key));
        box.setResponder(value -> ContractCreatorDraft.set(key, value));
        this.hints.add(new HintedBox(region, box, hint));
        return box;
    }

    private int fitWidth(Component label) {
        return this.font.width(label) + 12;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Component exportLabel() {
        return Component.translatable(ContractCreatorDraft.jsonExport() ? "misc.craftorio.dev_tools_export_json" : "misc.craftorio.dev_tools_export_code");
    }

    private void openEditPicker() {
        if (this.minecraft.level == null) return;

        Registry<CraftorioContract> registry = this.minecraft.level.registryAccess().registryOrThrow(CraftorioContract.REGISTRY_KEY);
        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        for (Holder.Reference<CraftorioContract> holder : registry.holders().toList()) {
            ResourceLocation id = holder.key().location();
            CraftorioContract contract = holder.value();
            options.add(new DevToolsPickerScreen.Option(Component.literal(contract.getActualName()), id.toString(), 0, true, () -> {
                ContractCreatorDraft.loadIntoDraft(id, contract);
                PacketDistributor.sendToServer(new LoadContractIntoCreatorPacket(id));
                this.minecraft.setScreen(this);
            }));
        }

        this.minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_contract"), this, options));
    }

    private float panelProgress() {
        float t = Mth.clamp((System.currentTimeMillis() - this.panelAnimStart) / (float) PANEL_ANIM_MS, 0f, 1f);
        float t1 = t - 1;
        float eased = t1 * t1 * t1 + 1;
        float target = this.panelOpen ? 1f : 0f;
        return this.panelAnimFrom + (target - this.panelAnimFrom) * eased;
    }

    private void togglePanel(Region region) {
        closeInline();
        if (this.panelOpen && this.panelRegion == region) {
            setPanelOpen(false);
            return;
        }
        this.panelRegion = region;
        this.shownRegion = region;
        if (!this.panelOpen) {
            setPanelOpen(true);
        }
        updatePanelWidgets();
    }

    private void setPanelOpen(boolean open) {
        this.panelAnimFrom = panelProgress();
        this.panelOpen = open;
        this.panelAnimStart = System.currentTimeMillis();
        if (!open) {
            this.panelRegion = null;
        }
    }

    private int panelX(float progress) {
        return (int) Mth.lerp(progress, this.width, this.width - PANEL_WIDTH - PANEL_MARGIN);
    }

    private int panelHeight() {
        return this.shownRegion == Region.TIME ? 180 : 118;
    }

    private void updatePanelWidgets() {
        float progress = panelProgress();
        boolean interactable = progress > 0.05f;
        int x = panelX(progress) + PANEL_PADDING;

        for (Map.Entry<Region, List<AbstractWidget>> entry : this.panelWidgets.entrySet()) {
            boolean shown = interactable && entry.getKey() == this.shownRegion;
            for (AbstractWidget widget : entry.getValue()) {
                widget.visible = shown;
                widget.active = shown;
            }
        }

        if (this.shownRegion == null) return;
        for (AbstractWidget widget : this.panelWidgets.getOrDefault(this.shownRegion, List.of())) {
            widget.setX(x + widgetOffsetX(widget));
        }
    }

    private int widgetOffsetX(AbstractWidget widget) {
        return this.offsets.getOrDefault(widget, 0);
    }

    private void positionPanelWidgetsVertically() {
        for (List<AbstractWidget> widgets : this.panelWidgets.values()) {
            for (AbstractWidget widget : widgets) {
                widget.setY(PANEL_TOP + this.verticalOffsets.getOrDefault(widget, 0));
            }
        }
    }

    private void openInline(Region region) {
        this.inlineRegion = region;
        int[] rect = regionRect(region);
        int width = Math.max(rect[2], 100);
        this.inlineBox.setWidth(width);
        this.inlineBox.setX(Mth.clamp(rect[0] + rect[2] / 2 - width / 2, 2, this.width - width - 2));
        this.inlineBox.setY(rect[1] + rect[3] / 2 - 8);
        this.inlineBox.setResponder(null);
        this.inlineBox.setValue(ContractCreatorDraft.get(region.key));
        this.inlineBox.setResponder(value -> ContractCreatorDraft.set(region.key, value));
        this.inlineBox.visible = true;
        this.inlineBox.active = true;
        this.setFocused(this.inlineBox);
        this.inlineBox.setFocused(true);
    }

    private void closeInline() {
        if (this.inlineRegion == null) return;
        this.inlineRegion = null;
        this.inlineBox.setFocused(false);
        this.inlineBox.visible = false;
        this.inlineBox.active = false;
        if (this.getFocused() == this.inlineBox) {
            this.setFocused(null);
        }
    }

    private float cardCenterX() {
        float closedCenter = this.width / 2f;
        float openCenter = (this.width - PANEL_WIDTH - PANEL_MARGIN) / 2f;
        return Mth.lerp(panelProgress(), closedCenter, Math.min(closedCenter, openCenter));
    }

    private int cardLeft() {
        return Math.round(cardCenterX() - this.cardWidth / 2f);
    }

    private int cardTop() {
        return CARD_TOP + Math.max(0, (this.height - CARD_TOP - BOTTOM_AREA - this.cardHeight) / 2);
    }

    private int[] regionRect(Region region) {
        int left = cardLeft();
        int top = cardTop();
        return new int[]{
                left + Math.round(region.x * this.scale),
                top + Math.round(region.y * this.scale),
                Math.round(region.w * this.scale),
                Math.round(region.h * this.scale)
        };
    }

    private Region regionAt(double mouseX, double mouseY) {
        for (Region region : Region.values()) {
            int[] rect = regionRect(region);
            if (mouseX >= rect[0] && mouseX < rect[0] + rect[2] && mouseY >= rect[1] && mouseY < rect[1] + rect[3]) {
                return region;
            }
        }
        return null;
    }

    private boolean overCard(double mouseX, double mouseY) {
        int left = cardLeft() + Math.round(PARCHMENT_LEFT * this.scale);
        int top = cardTop() + Math.round(PARCHMENT_TOP * this.scale);
        int right = cardLeft() + Math.round(PARCHMENT_RIGHT * this.scale);
        int bottom = cardTop() + Math.round(PARCHMENT_BOTTOM * this.scale);
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private boolean overPanel(double mouseX, double mouseY) {
        float progress = panelProgress();
        if (progress <= 0f) return false;
        int x = panelX(progress);
        return mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= PANEL_TOP && mouseY < PANEL_TOP + panelHeight();
    }

    private int swatchTop() {
        return PANEL_TOP + panelHeight() - PANEL_PADDING - SWATCH_SIZE * 2 - SWATCH_GAP;
    }

    private Integer swatchAt(double mouseX, double mouseY) {
        if (this.shownRegion == null || !this.panelOpen) return null;
        int left = panelX(panelProgress()) + PANEL_PADDING;
        int top = swatchTop();
        for (int i = 0; i < SWATCHES.length; i++) {
            int x = left + (i % SWATCH_COLUMNS) * (SWATCH_SIZE + SWATCH_GAP);
            int y = top + (i / SWATCH_COLUMNS) * (SWATCH_SIZE + SWATCH_GAP);
            if (mouseX >= x && mouseX < x + SWATCH_SIZE && mouseY >= y && mouseY < y + SWATCH_SIZE) {
                return SWATCHES[i];
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.inlineRegion != null && !this.inlineBox.isMouseOver(mouseX, mouseY)) {
            closeInline();
        }
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;

        Integer swatch = swatchAt(mouseX, mouseY);
        if (swatch != null) {
            EditBox colorBox = this.colorBoxes.get(this.shownRegion);
            if (colorBox != null) {
                colorBox.setValue(ContractTextColors.toHex(swatch));
            }
            return true;
        }
        if (overPanel(mouseX, mouseY)) return true;

        Region region = regionAt(mouseX, mouseY);
        if (region != null) {
            if (region.inline) {
                if (this.panelOpen) {
                    setPanelOpen(false);
                }
                openInline(region);
            } else {
                togglePanel(region);
            }
            return true;
        }

        if (overCard(mouseX, mouseY)) {
            this.minecraft.setScreen(new ContractCreatorPropertiesScreen(this, this.menu));
            return true;
        }

        if (this.panelOpen) {
            setPanelOpen(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.inlineRegion != null && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE)) {
            closeInline();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.panelOpen) {
            setPanelOpen(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updatePanelWidgets();
        if (this.inlineRegion != null) {
            int[] rect = regionRect(this.inlineRegion);
            this.inlineBox.setX(Mth.clamp(rect[0] + rect[2] / 2 - this.inlineBox.getWidth() / 2, 2, this.width - this.inlineBox.getWidth() - 2));
            this.inlineBox.setY(rect[1] + rect[3] / 2 - 8);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        renderPanelOverlay(graphics);

        for (HintedBox hinted : this.hints) {
            if (hinted.box().visible) {
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, hinted.box(), hinted.hint());
            }
        }

        if (!overPanel(mouseX, mouseY) && this.inlineRegion == null) {
            Region hovered = regionAt(mouseX, mouseY);
            if (hovered != null) {
                graphics.renderTooltip(this.font, Component.translatable("misc.craftorio." + hovered.labelKey), mouseX, mouseY);
            } else if (overCard(mouseX, mouseY)) {
                graphics.renderTooltip(this.font, Component.translatable("misc.craftorio.dev_tools_contract_card_hint"), mouseX, mouseY);
            }
        }

        this.helpPanel.render(graphics, this.font, this.width, 28, true, List.of(
                Component.translatable("misc.craftorio.dev_tools_contract_help_regions"),
                Component.translatable("misc.craftorio.dev_tools_contract_help_card"),
                Component.translatable("misc.craftorio.dev_tools_contract_help_colors"),
                Component.translatable("misc.craftorio.dev_tools_help_location")
        ));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        renderCard(graphics, mouseX, mouseY);
        renderPanelBackground(graphics);
    }

    private ResourceLocation cardTexture() {
        ResourceLocation fallback = Craftorio.getGuiTexture("contract_textures/default_contract.png");
        ResourceLocation id = ResourceLocation.tryParse(ContractCreatorDraft.get(ContractCreatorDraft.CARD_TEXTURE).trim());
        if (id == null || this.minecraft.level == null) return fallback;
        return this.minecraft.level.registryAccess().registry(CraftorioContractTexture.REGISTRY_KEY)
                .flatMap(registry -> registry.getOptional(id))
                .map(CraftorioContractTexture::texture)
                .orElse(fallback);
    }

    private void renderCard(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = cardLeft();
        int top = cardTop();

        boolean blockHover = overPanel(mouseX, mouseY) || this.inlineRegion != null;
        Region hovered = blockHover ? null : regionAt(mouseX, mouseY);
        boolean cardHovered = !blockHover && hovered == null && overCard(mouseX, mouseY);
        this.hoverScale += ((cardHovered ? HOVER_SCALE : 1f) - this.hoverScale) * HOVER_LERP;

        ResourceLocation texture = cardTexture();
        graphics.pose().pushPose();
        graphics.pose().translate(left + this.cardWidth / 2f, top + this.cardHeight / 2f, 0);
        graphics.pose().scale(this.hoverScale, this.hoverScale, 1f);
        int halfWidth = this.cardWidth / 2;
        int halfHeight = this.cardHeight / 2;
        graphics.setColor(0f, 0f, 0f, 0.5f);
        graphics.blit(texture, -halfWidth + 4, -halfHeight + 4, 0, 0, this.cardWidth, this.cardHeight, this.cardWidth, this.cardHeight);
        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.blit(texture, -halfWidth, -halfHeight, 0, 0, this.cardWidth, this.cardHeight, this.cardWidth, this.cardHeight);
        graphics.pose().popPose();

        for (Region region : Region.values()) {
            int[] rect = regionRect(region);
            boolean active = region == this.panelRegion || region == this.inlineRegion;
            if (region == hovered) {
                graphics.fill(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3], 0x40FFFFFF);
            }
            int outline = active ? 0xFFFFFF55 : region == hovered ? 0xFFFFFFFF : 0x50000000;
            graphics.renderOutline(rect[0], rect[1], rect[2], rect[3], outline);
            if (region != this.inlineRegion) {
                renderRegionPreview(graphics, region, rect);
            }
        }
    }

    private int draftColor(Region region) {
        return ContractTextColors.tryParse(ContractCreatorDraft.get(region.key)).orElse(region.defaultColor);
    }

    private void renderRegionPreview(GuiGraphics graphics, Region region, int[] rect) {
        switch (region) {
            case TITLE -> {
                String title = ContractCreatorDraft.get(ContractCreatorDraft.TITLE);
                drawFitted(graphics, title.isBlank() ? placeholderTitle() : title, rect, draftColor(region), 1);
            }
            case TIME -> {
                int seconds = parseInt(ContractCreatorDraft.get(ContractCreatorDraft.SECONDS));
                String time = Component.translatable("misc.craftorio.contract_time_remaining",
                        CraftorioMisc.ticksToTimeString((seconds > 0 ? seconds : 600) * CraftorioMisc.SECONDS_TO_TICKS)).getString();
                drawFitted(graphics, time, rect, draftColor(region), 1);
            }
            case DESCRIPTION -> {
                String description = ContractCreatorDraft.get(ContractCreatorDraft.DESCRIPTION);
                drawFitted(graphics, description.isBlank() ? Component.translatable("misc.craftorio.dev_tools_contract_region_description").getString() : description,
                        rect, draftColor(region), 2);
            }
            case PUNISHMENT -> {
                Optional<Component> name = punishmentName();
                String text = name.map(effect -> Component.translatable("misc.craftorio.contract_punishment_line", effect).getString())
                        .orElse(Component.translatable("misc.craftorio.dev_tools_contract_no_punishment").getString());
                drawFitted(graphics, text, rect, draftColor(region), 1);
            }
            case MIN, THRESHOLD, MAX -> drawThreshold(graphics, region, rect);
        }
    }

    private String placeholderTitle() {
        String id = ContractCreatorDraft.get(ContractCreatorDraft.ID);
        return id.isBlank() ? Component.translatable("misc.craftorio.dev_tools_contract_region_title").getString() : id;
    }

    private Optional<Component> punishmentName() {
        ResourceLocation id = ResourceLocation.tryParse(ContractCreatorDraft.get(ContractCreatorDraft.PUNISHMENT).trim());
        if (id == null || ContractCreatorDraft.get(ContractCreatorDraft.PUNISHMENT).isBlank() || this.minecraft.level == null) return Optional.empty();
        return this.minecraft.level.registryAccess().registry(CraftorioEffects.REGISTRY_KEY)
                .flatMap(registry -> registry.getOptional(id))
                .<Component>map(effect -> Component.literal(effect.getActualName()))
                .or(() -> Optional.of(Component.literal(id.toString())));
    }

    private void drawFitted(GuiGraphics graphics, String text, int[] rect, int color, int maxLines) {
        float textScale = TEXT_SCALE * this.scale * (maxLines > 1 ? 0.75f : 1f);
        int wrapWidth = Math.max(10, (int) ((rect[2] - 2) / textScale));
        List<FormattedCharSequence> lines;
        if (maxLines == 1) {
            lines = List.of(Component.literal(text).getVisualOrderText());
            int textWidth = this.font.width(text);
            if (textWidth > wrapWidth) {
                textScale *= wrapWidth / (float) textWidth;
            }
        } else {
            lines = this.font.split(Component.literal(text), wrapWidth);
            if (lines.size() > maxLines) {
                lines = lines.subList(0, maxLines);
            }
        }
        int lineHeight = this.font.lineHeight;
        float blockHeight = lines.size() * lineHeight * textScale;
        if (blockHeight > rect[3]) {
            textScale *= rect[3] / blockHeight;
            blockHeight = rect[3];
        }

        int drawColor = color | 0xFF000000;
        graphics.pose().pushPose();
        graphics.pose().translate(rect[0] + rect[2] / 2f, rect[1] + (rect[3] - blockHeight) / 2f + 0.5f * textScale, 0);
        graphics.pose().scale(textScale, textScale, 1f);
        int y = 0;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, -this.font.width(line) / 2, y, drawColor, true);
            y += lineHeight;
        }
        graphics.pose().popPose();
    }

    private void drawThreshold(GuiGraphics graphics, Region region, int[] rect) {
        String raw = ContractCreatorDraft.get(region.key).trim();
        String value;
        if (raw.isEmpty()) {
            value = region == Region.MAX ? "1M" : "0";
        } else {
            try {
                value = CraftorioMisc.bigIntFormat(CraftorioMisc.scientificToInt(raw));
            } catch (RuntimeException e) {
                value = "?";
            }
        }
        String label = Component.translatable("misc.craftorio." + region.labelKey + "_short").getString();

        float labelScale = Math.min(0.5f * this.scale, (rect[2] - 2) / (float) Math.max(1, this.font.width(label)));
        float valueScale = Math.min(0.7f * this.scale, (rect[2] - 2) / (float) Math.max(1, this.font.width(value)));
        float centerX = rect[0] + rect[2] / 2f;
        float centerY = rect[1] + rect[3] / 2f;
        int labelColor = 0xFF404040;
        int valueColor = 0xFF202020;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY - this.font.lineHeight * labelScale - 1, 0);
        graphics.pose().scale(labelScale, labelScale, 1f);
        graphics.drawString(this.font, label, -this.font.width(label) / 2, 0, labelColor, false);
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY + 1, 0);
        graphics.pose().scale(valueScale, valueScale, 1f);
        graphics.drawString(this.font, value, -this.font.width(value) / 2, 0, valueColor, false);
        graphics.pose().popPose();
    }

    private void renderPanelBackground(GuiGraphics graphics) {
        float progress = panelProgress();
        if (progress <= 0f || this.shownRegion == null) return;

        int x = panelX(progress);
        int height = panelHeight();
        graphics.fill(x, PANEL_TOP, x + PANEL_WIDTH, PANEL_TOP + height, 0xE0101010);
        graphics.renderOutline(x, PANEL_TOP, PANEL_WIDTH, height, 0xFFFFFFFF);
    }

    private void renderPanelOverlay(GuiGraphics graphics) {
        float progress = panelProgress();
        if (progress <= 0f || this.shownRegion == null) return;

        int x = panelX(progress);
        graphics.drawString(this.font, Component.translatable("misc.craftorio." + this.shownRegion.labelKey), x + PANEL_PADDING, PANEL_TOP + 6, 0xFFFFFF, false);
        for (PanelLabel label : this.panelLabels) {
            if (label.region() != this.shownRegion) continue;
            graphics.drawString(this.font, Component.translatable("misc.craftorio." + label.key()), x + PANEL_PADDING, PANEL_TOP + label.offsetY(), 0xAAAAAA, false);
        }

        int left = x + PANEL_PADDING;
        int top = swatchTop();
        for (int i = 0; i < SWATCHES.length; i++) {
            int sx = left + (i % SWATCH_COLUMNS) * (SWATCH_SIZE + SWATCH_GAP);
            int sy = top + (i / SWATCH_COLUMNS) * (SWATCH_SIZE + SWATCH_GAP);
            graphics.fill(sx, sy, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFF000000 | SWATCHES[i]);
            graphics.renderOutline(sx, sy, SWATCH_SIZE, SWATCH_SIZE, 0xFF808080);
        }

        int previewX = left + SWATCH_COLUMNS * (SWATCH_SIZE + SWATCH_GAP) + 4;
        int previewSize = SWATCH_SIZE * 2 + SWATCH_GAP;
        int previewColor = draftColor(this.shownRegion);
        graphics.fill(previewX, top, previewX + previewSize, top + previewSize, 0xFF000000 | previewColor);
        graphics.renderOutline(previewX, top, previewSize, previewSize, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

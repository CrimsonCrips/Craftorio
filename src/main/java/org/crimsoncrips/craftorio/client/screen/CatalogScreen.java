package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public abstract class CatalogScreen<T> extends Screen {

    protected static final int MAX_COLS = 9;
    protected static final int MAX_ROWS = 16;
    protected static final int MIN_COLS = 3;
    protected static final int MIN_ROWS = 1;
    protected static final int SLOT_SIZE = 18;
    protected static final int SLOT_GAP = 4;
    protected static final int CELL_SIZE = SLOT_SIZE + SLOT_GAP;
    protected static final int GRID_TOP = 50;
    protected static final int GRID_SIDE_MARGIN = 20;
    protected static final int FOOTER_RESERVED = 64;

    private static final long HELP_ANIM_DURATION_MS = 250L;
    private static final int HELP_PANEL_WIDTH = 170;
    private static final int HELP_PANEL_MARGIN = 8;
    private static final int HELP_BUTTON_SIZE = 20;

    private EditBox searchBox;
    private String searchQuery = "";
    protected List<T> filtered = List.of();
    protected int page = 0;
    protected int cols = MAX_COLS;
    protected int rows = MAX_ROWS;
    protected int pageSize = MAX_COLS * MAX_ROWS;

    private boolean helpVisible = false;
    private boolean helpAnimOpening = false;
    private long helpAnimStartMillis = 0L;
    private float helpAnimFromProgress = 0f;

    protected CatalogScreen(Component title) {
        super(title);
    }

    protected abstract List<T> buildCatalog();

    protected abstract String getSearchName(T entry);

    protected abstract String getSearchNamespace(T entry);

    protected abstract Stream<ResourceLocation> getSearchTags(T entry);

    protected abstract AbstractWidget createEntryWidget(int x, int y, T entry);

    protected void addExtraWidgets() {}

    protected void renderHeader(GuiGraphics guiGraphics, int centerX) {}

    protected void updateFiltered() {
        String query = this.searchQuery.strip().toLowerCase(Locale.ROOT);
        List<T> full = buildCatalog();

        if (query.isEmpty()) {
            this.filtered = full;
            return;
        }

        String[] tokens = query.split("\\s+");
        List<T> matches = new ArrayList<>();
        entryLoop:
        for (T entry : full) {
            for (String token : tokens) {
                if (!matchesToken(entry, token)) continue entryLoop;
            }
            matches.add(entry);
        }
        this.filtered = matches;
    }

    private boolean matchesToken(T entry, String token) {
        if (token.isEmpty()) return true;

        char prefix = token.charAt(0);
        if (prefix == '#') {
            String tagQuery = token.substring(1);
            if (tagQuery.isEmpty()) return true;
            return getSearchTags(entry).anyMatch(tag -> tag.toString().contains(tagQuery));
        }

        if (prefix == '@') {
            String modQuery = token.substring(1);
            if (modQuery.isEmpty()) return true;
            return getSearchNamespace(entry).toLowerCase(Locale.ROOT).contains(modQuery);
        }

        return getSearchName(entry).toLowerCase(Locale.ROOT).contains(token);
    }

    protected int totalPages() {
        return Math.max(1, (this.filtered.size() + this.pageSize - 1) / this.pageSize);
    }

    protected int footerY() {
        return GRID_TOP + this.rows * CELL_SIZE + 8;
    }

    @Override
    protected void init() {
        super.init();

        this.cols = Mth.clamp((this.width - GRID_SIDE_MARGIN * 2) / CELL_SIZE, MIN_COLS, MAX_COLS);
        this.rows = Mth.clamp((this.height - GRID_TOP - FOOTER_RESERVED) / CELL_SIZE, MIN_ROWS, MAX_ROWS);
        this.pageSize = this.cols * this.rows;

        this.updateFiltered();
        this.page = Mth.clamp(this.page, 0, this.totalPages() - 1);

        this.searchBox = new EditBox(this.font, this.width / 2 - 70, 28, 140, 16, Component.translatable("misc.craftorio.search"));
        this.searchBox.setValue(this.searchQuery);
        this.searchBox.setResponder(this::onSearchChanged);

        this.refreshWidgets();
    }

    private void onSearchChanged(String value) {
        this.searchQuery = value;
        this.page = 0;
        this.updateFiltered();
        this.refreshWidgets();
    }

    protected void refreshWidgets() {
        this.clearWidgets();

        this.addRenderableWidget(this.searchBox);

        int gridWidth = this.cols * CELL_SIZE - SLOT_GAP;
        int startX = (this.width - gridWidth) / 2;

        int firstIndex = this.page * this.pageSize;
        for (int i = 0; i < this.pageSize; i++) {
            int index = firstIndex + i;
            if (index >= this.filtered.size()) break;

            int col = i % this.cols;
            int row = i / this.cols;

            this.addRenderableWidget(createEntryWidget(startX + col * CELL_SIZE, GRID_TOP + row * CELL_SIZE, this.filtered.get(index)));
        }

        int footerY = footerY();
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(Component.literal("<<"), b -> this.setPage(0))
                .bounds(centerX - 90, footerY, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.setPage(this.page - 1))
                .bounds(centerX - 54, footerY, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.setPage(this.page + 1))
                .bounds(centerX + 20, footerY, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">>"), b -> this.setPage(this.totalPages() - 1))
                .bounds(centerX + 56, footerY, 34, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - 50, footerY + 26, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("?"), b -> toggleHelp())
                .bounds(this.width - HELP_BUTTON_SIZE - 6, 6, HELP_BUTTON_SIZE, HELP_BUTTON_SIZE).build());

        addExtraWidgets();
    }

    private void toggleHelp() {
        this.helpAnimFromProgress = currentHelpProgress();
        this.helpVisible = !this.helpVisible;
        this.helpAnimOpening = this.helpVisible;
        this.helpAnimStartMillis = System.currentTimeMillis();
    }

    private float currentHelpProgress() {
        long elapsed = System.currentTimeMillis() - this.helpAnimStartMillis;
        float t = Mth.clamp(elapsed / (float) HELP_ANIM_DURATION_MS, 0f, 1f);
        float eased = easeOutCubic(t);
        float target = this.helpAnimOpening ? 1f : 0f;
        return this.helpAnimFromProgress + (target - this.helpAnimFromProgress) * eased;
    }

    private static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    private void renderHelpPanel(GuiGraphics graphics, float progress) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.addAll(this.font.split(Component.translatable("misc.craftorio.search_help_title"), HELP_PANEL_WIDTH - 12));
        lines.addAll(this.font.split(Component.translatable("misc.craftorio.search_help_mod"), HELP_PANEL_WIDTH - 12));
        lines.addAll(this.font.split(Component.translatable("misc.craftorio.search_help_tag"), HELP_PANEL_WIDTH - 12));

        int panelHeight = lines.size() * this.font.lineHeight + 14;
        int panelY = 30;
        int onScreenX = this.width - HELP_PANEL_WIDTH - HELP_PANEL_MARGIN;
        int panelX = (int) Mth.lerp(progress, this.width, onScreenX);

        graphics.fill(panelX, panelY, panelX + HELP_PANEL_WIDTH, panelY + panelHeight, 0xE0101010);
        graphics.fill(panelX, panelY, panelX + HELP_PANEL_WIDTH, panelY + 1, 0xFFFFFFFF);
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + HELP_PANEL_WIDTH, panelY + panelHeight, 0xFFFFFFFF);
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, 0xFFFFFFFF);
        graphics.fill(panelX + HELP_PANEL_WIDTH - 1, panelY, panelX + HELP_PANEL_WIDTH, panelY + panelHeight, 0xFFFFFFFF);

        int textY = panelY + 7;
        for (var line : lines) {
            graphics.drawString(this.font, line, panelX + 6, textY, 0xFFFFFF, false);
            textY += this.font.lineHeight;
        }
    }

    protected void setPage(int newPage) {
        this.page = Mth.clamp(newPage, 0, this.totalPages() - 1);
        this.refreshWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        renderHeader(guiGraphics, centerX);

        int footerY = footerY();
        guiGraphics.drawCenteredString(this.font, Component.literal((this.page + 1) + " / " + this.totalPages()), centerX, footerY + 6, 0xFFFFFF);

        float helpProgress = currentHelpProgress();
        if (helpProgress > 0f) {
            renderHelpPanel(guiGraphics, helpProgress);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

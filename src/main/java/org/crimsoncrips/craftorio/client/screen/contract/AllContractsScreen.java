package org.crimsoncrips.craftorio.client.screen.contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.registries.contract.ContractType;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AllContractsScreen extends Screen implements ScrollableScreen {

    private static final int TOP_MARGIN = 48;
    private static final int BOTTOM_MARGIN = 40;
    private static final int SIDE_MARGIN = 40;
    private static final int ROW_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 4;

    private enum SortMode {
        CLAIM_THRESHOLD, POINT_REWARD, ALPHABETICAL;

        SortMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private static SortMode sortMode = SortMode.CLAIM_THRESHOLD;
    private static ContractType typeFilter = null;
    private static boolean ascending = true;

    private final Screen parent;
    private List<Holder.Reference<CraftorioContract>> allContracts = List.of();
    private String query = "";
    private EditBox searchBox;
    private Button sortButton;
    private Button directionButton;
    private Button typeButton;
    private List<CraftorioContract> contracts = List.of();
    private List<ResourceLocation> contractIds = List.of();

    private int scroll = 0;
    private boolean draggingScrollbar = false;
    private double dragStartMouseY = 0;
    private int dragStartScroll = 0;

    public AllContractsScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.all_contracts_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.minecraft.level != null) {
            this.allContracts = List.copyOf(CraftorioMisc.getAllContracts(this.minecraft.level.registryAccess()));
        }

        int searchWidth = 160;
        int sortWidth = 130;
        int directionWidth = 20;
        int typeWidth = 90;
        int rowLeft = this.width / 2 - (searchWidth + sortWidth + directionWidth + typeWidth + 12) / 2;

        this.searchBox = new EditBox(this.font, rowLeft, 24, searchWidth, 16, Component.literal("search"));
        this.searchBox.setMaxLength(128);
        this.searchBox.setValue(this.query);
        this.searchBox.setResponder(value -> {
            this.query = value;
            refreshList();
        });
        this.addRenderableWidget(this.searchBox);

        this.sortButton = Button.builder(sortLabel(), b -> {
            sortMode = sortMode.next();
            this.sortButton.setMessage(sortLabel());
            refreshList();
        }).bounds(rowLeft + searchWidth + 4, 22, sortWidth, 20).build();
        this.addRenderableWidget(this.sortButton);

        this.directionButton = Button.builder(directionLabel(), b -> {
            ascending = !ascending;
            this.directionButton.setMessage(directionLabel());
            refreshList();
        }).bounds(rowLeft + searchWidth + sortWidth + 8, 22, directionWidth, 20).build();
        this.addRenderableWidget(this.directionButton);

        this.typeButton = Button.builder(typeLabel(), b -> {
            typeFilter = nextType(typeFilter);
            this.typeButton.setMessage(typeLabel());
            refreshList();
        }).bounds(rowLeft + searchWidth + sortWidth + directionWidth + 12, 22, typeWidth, 20).build();
        this.addRenderableWidget(this.typeButton);

        refreshList();

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private Component sortLabel() {
        return Component.translatable("misc.craftorio.all_contracts_sort", Component.translatable("misc.craftorio.all_contracts_sort_" + sortMode.name().toLowerCase(Locale.ROOT)));
    }

    private static ContractType nextType(ContractType current) {
        ContractType[] types = ContractType.values();
        if (current == null) return types[0];
        return current.ordinal() + 1 < types.length ? types[current.ordinal() + 1] : null;
    }

    private Component typeLabel() {
        Component value = typeFilter == null ? Component.translatable("misc.craftorio.all_contracts_type_all")
                : Component.translatable(typeFilter == ContractType.BUILDING ? "misc.craftorio.all_contracts_type_build" : "misc.craftorio.all_contracts_type_item");
        return Component.translatable("misc.craftorio.all_contracts_type", value);
    }

    private Component directionLabel() {
        return Component.literal(ascending ? "\u2191" : "\u2193");
    }

    private void refreshList() {
        String needle = this.query.trim().toLowerCase(Locale.ROOT);
        List<Holder.Reference<CraftorioContract>> filtered = this.allContracts.stream()
                .filter(holder -> typeFilter == null || holder.value().getType() == typeFilter)
                .filter(holder -> matches(holder, needle))
                .sorted(ascending ? comparator() : comparator().reversed())
                .toList();
        this.contracts = filtered.stream().map(Holder.Reference::value).toList();
        this.contractIds = filtered.stream().map(holder -> holder.key().location()).toList();
        this.scroll = 0;
    }

    private static boolean matches(Holder.Reference<CraftorioContract> holder, String needle) {
        if (needle.isEmpty()) return true;
        ResourceLocation id = holder.key().location();
        String name = holder.value().getActualName().toLowerCase(Locale.ROOT);
        for (String token : needle.split("\\s+")) {
            if (token.startsWith("@")) {
                if (!id.getNamespace().startsWith(token.substring(1))) return false;
            } else if (!name.contains(token) && !id.toString().contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static Comparator<Holder.Reference<CraftorioContract>> comparator() {
        Comparator<Holder.Reference<CraftorioContract>> byName = Comparator.comparing(holder -> holder.value().getActualName().toLowerCase(Locale.ROOT));
        return switch (sortMode) {
            case CLAIM_THRESHOLD -> Comparator.<Holder.Reference<CraftorioContract>, BigInteger>comparing(holder -> holder.value().getPointThreshold()).thenComparing(byName);
            case POINT_REWARD -> Comparator.<Holder.Reference<CraftorioContract>, BigInteger>comparing(holder -> holder.value().getBasePointValue()).thenComparing(byName);
            case ALPHABETICAL -> byName;
        };
    }

    private int listTop() { return TOP_MARGIN; }

    private int listBottom() { return this.height - BOTTOM_MARGIN; }

    private int listLeft() { return SIDE_MARGIN; }

    private int listRight() { return this.width - SIDE_MARGIN; }

    private int maxScroll() {
        return Math.max(0, this.contracts.size() * ROW_HEIGHT - (listBottom() - listTop()));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, 8, 0xFFFFFF);
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.searchBox, Component.translatable("misc.craftorio.all_contracts_search_hint").getString());

        if (this.contracts.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.no_contracts"), this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        int top = listTop();
        int bottom = listBottom();
        int left = listLeft();
        int right = listRight();

        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());

        graphics.enableScissor(left, top, right, bottom);

        int y = top - this.scroll;
        for (CraftorioContract contract : this.contracts) {
            if (y + ROW_HEIGHT >= top && y <= bottom) {
                boolean hovered = mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT;
                if (hovered) {
                    graphics.fill(left, y, right, y + ROW_HEIGHT, 0x40FFFFFF);
                }

                String name = contract.getActualName();
                int textY = y + (ROW_HEIGHT - this.font.lineHeight) / 2;
                String claimText = Component.translatable("misc.craftorio.all_contracts_claim", CraftorioMisc.bigIntFormat(contract.getPointThreshold())).getString();
                String rewardText = Component.translatable("misc.craftorio.all_contracts_reward", CraftorioMisc.bigIntFormat(contract.getBasePointValue())).getString();
                int claimX = right - 4 - this.font.width(claimText);
                int rewardX = claimX - 12 - this.font.width(rewardText);
                String typeText = Component.translatable(contract.getType() == ContractType.BUILDING ? "misc.craftorio.contract_type_build" : "misc.craftorio.contract_type_item").getString();
                int typeX = rewardX - 12 - this.font.width(typeText);

                graphics.drawString(this.font, this.font.plainSubstrByWidth(name, Math.max(20, typeX - left - 12)), left + 4, textY, 0xFFFFFF, false);
                graphics.drawString(this.font, typeText, typeX, textY, typeFilter != null ? 0x55FF55 : 0xAAAAAA, false);
                graphics.drawString(this.font, rewardText, rewardX, textY, sortMode == SortMode.POINT_REWARD ? 0xFFAA00 : 0xAAAAAA, false);
                graphics.drawString(this.font, claimText, claimX, textY, sortMode == SortMode.CLAIM_THRESHOLD ? 0x55FFFF : 0xAAAAAA, false);
            }
            y += ROW_HEIGHT;
        }

        graphics.disableScissor();
        renderScrollbar(graphics, right + 2, top, bottom);
    }

    private void renderScrollbar(GuiGraphics graphics, int x, int top, int bottom) {
        int contentHeight = this.contracts.size() * ROW_HEIGHT;
        int trackHeight = bottom - top;
        if (contentHeight <= trackHeight) return;

        int thumbHeight = Math.max(10, trackHeight * trackHeight / contentHeight);
        int maxScroll = maxScroll();
        int thumbY = top + (maxScroll == 0 ? 0 : this.scroll * (trackHeight - thumbHeight) / maxScroll);

        graphics.fill(x, top, x + SCROLLBAR_WIDTH, bottom, 0x40FFFFFF);
        graphics.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        this.scroll = Mth.clamp(this.scroll - (int) Math.round(scrollY * ROW_HEIGHT), 0, maxScroll());
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int top = listTop();
            int bottom = listBottom();
            int contentHeight = this.contracts.size() * ROW_HEIGHT;
            int trackHeight = bottom - top;

            if (contentHeight > trackHeight) {
                int thumbHeight = Math.max(10, trackHeight * trackHeight / contentHeight);
                int maxScroll = maxScroll();
                int thumbY = top + (maxScroll == 0 ? 0 : this.scroll * (trackHeight - thumbHeight) / maxScroll);
                int scrollbarX = listRight() + 2;

                if (mouseX >= scrollbarX - 1 && mouseX < scrollbarX + SCROLLBAR_WIDTH + 1 && mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
                    this.draggingScrollbar = true;
                    this.dragStartMouseY = mouseY;
                    this.dragStartScroll = this.scroll;
                    return true;
                }
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0 && mouseX >= listLeft() && mouseX < listRight() && mouseY >= listTop() && mouseY < listBottom()) {
            int index = (int) ((mouseY - listTop() + this.scroll) / ROW_HEIGHT);
            if (index >= 0 && index < this.contracts.size()) {
                this.minecraft.setScreen(new ContractDetailsScreen(this, this.contracts.get(index), this.contractIds.get(index)));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            int top = listTop();
            int bottom = listBottom();
            int trackHeight = bottom - top;
            int contentHeight = this.contracts.size() * ROW_HEIGHT;
            int maxScroll = maxScroll();
            int thumbHeight = Math.max(10, trackHeight * trackHeight / Math.max(1, contentHeight));
            int scrollRange = trackHeight - thumbHeight;

            if (maxScroll > 0 && scrollRange > 0) {
                double deltaMouseY = mouseY - this.dragStartMouseY;
                int newScroll = this.dragStartScroll + (int) Math.round(deltaMouseY * maxScroll / (double) scrollRange);
                this.scroll = Mth.clamp(newScroll, 0, maxScroll);
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

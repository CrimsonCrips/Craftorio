package org.crimsoncrips.craftorio.client.screen.devtools;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.networking.devtools.AdvancementListPacket;
import org.crimsoncrips.craftorio.networking.devtools.PointDataMapsPacket;
import org.crimsoncrips.craftorio.networking.devtools.RequestAdvancementListPacket;
import org.crimsoncrips.craftorio.networking.devtools.RequestPointDataMapsPacket;
import org.crimsoncrips.craftorio.server.devtools.CraftorioDevTools;
import org.crimsoncrips.craftorio.server.devtools.PointsDeterminerKind;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@OnlyIn(Dist.CLIENT)
public class PointsDeterminerScreen extends Screen implements ScrollableScreen {

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int LIST_WIDTH = 320;
    private static final int ROW_HEIGHT = 20;
    private static final int TAB_TOP = 24;
    private static final int TAB_WIDTH = 90;
    private static final int TAB_HEIGHT = 16;
    private static final int SEARCH_TOP = TAB_TOP + TAB_HEIGHT + 4;
    private static final int LIST_TOP = SEARCH_TOP + 20;
    private static final int FOOTER_HEIGHT = 64;
    private static final int VALUE_WIDTH = 80;
    private static final int FOOTER_BUTTON_WIDTH = 100;

    private record PointEntry(String key, Component name, ItemStack icon, @Nullable TextureAtlasSprite sprite) {}

    private final Screen parent;
    private final Map<PointsDeterminerKind, Map<String, String>> values = new EnumMap<>(PointsDeterminerKind.class);
    private PointsDeterminerKind kind = PointsDeterminerKind.ITEM;
    @Nullable
    private List<AdvancementListPacket.Entry> advancements;
    private boolean advancementsRequested;
    private String filter = "";
    private boolean valuedOnly;
    private Component status = Component.empty();
    private EditBox searchBox;
    private ValueList list;

    public PointsDeterminerScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.points_determiner_title"));
        this.parent = parent;
        for (PointsDeterminerKind entry : PointsDeterminerKind.values()) {
            this.values.put(entry, new LinkedHashMap<>());
        }
    }

    @Override
    protected void init() {
        PointsDeterminerKind[] kinds = PointsDeterminerKind.values();
        int tabsLeft = this.width / 2 - kinds.length * TAB_WIDTH / 2;
        for (int i = 0; i < kinds.length; i++) {
            PointsDeterminerKind entry = kinds[i];
            Button tab = Button.builder(Component.translatable("misc.craftorio." + entry.langKey()), b -> selectKind(entry))
                    .bounds(tabsLeft + i * TAB_WIDTH, TAB_TOP, TAB_WIDTH, TAB_HEIGHT).build();
            tab.active = entry != this.kind;
            this.addRenderableWidget(tab);
        }

        this.searchBox = new EditBox(this.font, this.width / 2 - 150, SEARCH_TOP, 200, 16, Component.literal("search"));
        this.searchBox.setMaxLength(128);
        this.searchBox.setValue(this.filter);
        this.searchBox.setResponder(value -> {
            this.filter = value;
            this.list.refill();
        });
        this.addRenderableWidget(this.searchBox);

        this.addRenderableWidget(Button.builder(showLabel(), b -> {
            this.valuedOnly = !this.valuedOnly;
            b.setMessage(showLabel());
            this.list.refill();
        }).bounds(this.width / 2 + 54, SEARCH_TOP - 2, 96, 20).build());

        this.list = new ValueList(this.minecraft);
        this.list.refill();
        this.addRenderableWidget(this.list);

        int rowY = this.height - 52;
        int footerLeft = this.width / 2 - (FOOTER_BUTTON_WIDTH * 3 + 8) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.points_determiner_edit_values"),
                        b -> PacketDistributor.sendToServer(new RequestPointDataMapsPacket(this.kind)))
                .bounds(footerLeft, rowY, FOOTER_BUTTON_WIDTH, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.points_determiner_generate_json"), b -> generate(true))
                .bounds(footerLeft + FOOTER_BUTTON_WIDTH + 4, rowY, FOOTER_BUTTON_WIDTH, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.points_determiner_generate_code"), b -> generate(false))
                .bounds(footerLeft + (FOOTER_BUTTON_WIDTH + 4) * 2, rowY, FOOTER_BUTTON_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private Component showLabel() {
        return Component.translatable(this.valuedOnly ? "misc.craftorio.points_determiner_show_valued" : "misc.craftorio.points_determiner_show_all");
    }

    private void selectKind(PointsDeterminerKind kind) {
        this.kind = kind;
        this.rebuildWidgets();
    }

    private Map<String, String> currentValues() {
        return this.values.get(this.kind);
    }

    public void setAdvancements(List<AdvancementListPacket.Entry> advancements) {
        this.advancements = advancements;
        if (this.kind.isAdvancement() && this.list != null) {
            this.list.refill();
        }
    }

    public void showSources(PointDataMapsPacket message) {
        if (message.sources().isEmpty()) {
            this.status = Component.translatable("misc.craftorio.points_determiner_no_data_map").withStyle(ChatFormatting.RED);
            return;
        }

        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        for (PointDataMapsPacket.Source source : message.sources()) {
            Component label = source.name().isEmpty()
                    ? Component.translatable("misc.craftorio.points_determiner_merged")
                    : Component.literal(source.name());
            String detail = Component.translatable("misc.craftorio.points_determiner_entries", source.values().size()).getString();
            options.add(new DevToolsPickerScreen.Option(label, detail, 0, true, () -> {
                this.kind = message.kind();
                this.values.put(message.kind(), new LinkedHashMap<>(source.values()));
                this.status = Component.translatable("misc.craftorio.points_determiner_loaded", source.values().size(), label).withStyle(ChatFormatting.GREEN);
                this.minecraft.setScreen(this);
            }));
        }
        this.minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.points_determiner_sources_title"), this, options));
    }

    private void generate(boolean json) {
        Map<String, String> sorted = new TreeMap<>();
        int invalid = 0;
        for (Map.Entry<String, String> entry : currentValues().entrySet()) {
            if (this.kind.isValidValue(entry.getValue())) {
                sorted.put(entry.getKey(), entry.getValue().trim());
            } else {
                invalid++;
            }
        }

        if (invalid > 0) {
            this.status = Component.translatable("misc.craftorio.points_determiner_invalid", invalid).withStyle(ChatFormatting.RED);
            return;
        }
        if (sorted.isEmpty()) {
            this.status = Component.translatable("misc.craftorio.points_determiner_nothing_to_generate").withStyle(ChatFormatting.RED);
            return;
        }

        String content = json ? buildJson(sorted) : buildCode(sorted);
        Path dir = this.minecraft.gameDirectory.toPath().resolve(CraftorioDevTools.DEV_TOOLS_DIR_NAME);
        try {
            String fileName = CraftorioDevTools.writeFile(dir, "points_" + this.kind.id(), content, json ? "json" : "txt");
            this.status = Component.translatable("misc.craftorio.dev_tools_generate_success", CraftorioDevTools.DEV_TOOLS_DIR_NAME + "/" + fileName).withStyle(ChatFormatting.GREEN);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write points determiner output", e);
            this.status = Component.translatable("misc.craftorio.points_determiner_write_failed").withStyle(ChatFormatting.RED);
        }
    }

    private static String buildJson(Map<String, String> sorted) {
        JsonObject entries = new JsonObject();
        sorted.forEach(entries::addProperty);
        JsonObject root = new JsonObject();
        root.add("values", entries);
        return CraftorioDevTools.toPrettyJson(root);
    }

    private String buildCode(Map<String, String> sorted) {
        String registry = switch (this.kind) {
            case ITEM -> "Registries.ITEM";
            case ENCHANTMENT -> "Registries.ENCHANTMENT";
            case EFFECT -> "Registries.MOB_EFFECT";
            case ADVANCEMENT, ADVANCEMENT_MULTIPLIER -> "Registries.ADVANCEMENT";
        };

        StringBuilder code = new StringBuilder();
        code.append("Builder<String, ").append(this.kind.typeName()).append("> point_value = this.builder(CraftorioDataMaps.").append(this.kind.fieldName()).append(");\n");
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String key = entry.getKey();
            code.append("point_value.add(");
            if (key.startsWith("#")) {
                code.append("TagKey.create(").append(registry).append(", ResourceLocation.parse(\"").append(key.substring(1)).append("\"))");
            } else {
                code.append("ResourceLocation.parse(\"").append(key).append("\")");
            }
            code.append(", \"").append(entry.getValue()).append("\", false);\n");
        }
        return code.toString();
    }

    private List<PointEntry> buildEntries() {
        Map<String, String> current = currentValues();
        List<PointEntry> entries = new ArrayList<>();
        Set<String> known = new HashSet<>();

        for (String key : current.keySet()) {
            if (!key.startsWith("#")) continue;
            ItemStack icon = ItemStack.EMPTY;
            ResourceLocation tagId = ResourceLocation.tryParse(key.substring(1));
            if (this.kind == PointsDeterminerKind.ITEM && tagId != null) {
                icon = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagId))
                        .flatMap(set -> set.stream().findFirst())
                        .map(holder -> new ItemStack(holder.value()))
                        .orElse(ItemStack.EMPTY);
            }
            entries.add(new PointEntry(key, Component.literal(key), icon, null));
            known.add(key);
        }

        switch (this.kind) {
            case ITEM -> {
                for (Item item : BuiltInRegistries.ITEM) {
                    if (item == Items.AIR) continue;
                    ItemStack stack = new ItemStack(item);
                    String key = BuiltInRegistries.ITEM.getKey(item).toString();
                    entries.add(new PointEntry(key, stack.getHoverName(), stack, null));
                    known.add(key);
                }
            }
            case ENCHANTMENT -> {
                if (this.minecraft.level != null) {
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(holder -> {
                        String key = holder.key().location().toString();
                        Enchantment enchantment = holder.value();
                        entries.add(new PointEntry(key, enchantment.description(), book, null));
                        known.add(key);
                    });
                }
            }
            case EFFECT -> BuiltInRegistries.MOB_EFFECT.holders().forEach(holder -> {
                String key = holder.key().location().toString();
                entries.add(new PointEntry(key, holder.value().getDisplayName(), ItemStack.EMPTY, effectSprite(holder)));
                known.add(key);
            });
            case ADVANCEMENT, ADVANCEMENT_MULTIPLIER -> {
                if (this.advancements == null) {
                    if (!this.advancementsRequested) {
                        this.advancementsRequested = true;
                        PacketDistributor.sendToServer(new RequestAdvancementListPacket());
                    }
                } else {
                    for (AdvancementListPacket.Entry advancement : this.advancements) {
                        String key = advancement.id().toString();
                        entries.add(new PointEntry(key, advancement.title(), advancement.icon(), null));
                        known.add(key);
                    }
                }
            }
        }

        for (String key : current.keySet()) {
            if (known.contains(key)) continue;
            entries.add(new PointEntry(key, Component.literal(key), ItemStack.EMPTY, null));
        }
        return entries;
    }

    private TextureAtlasSprite effectSprite(Holder<MobEffect> holder) {
        return this.minecraft.getMobEffectTextures().get(holder);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(guiGraphics, this.font, this.searchBox, "search");
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, this.height - FOOTER_HEIGHT + 2, 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class ValueList extends ContainerObjectSelectionList<ValueList.Row> {

        ValueList(Minecraft minecraft) {
            super(minecraft, PointsDeterminerScreen.this.width,
                    PointsDeterminerScreen.this.height - LIST_TOP - FOOTER_HEIGHT, LIST_TOP, ROW_HEIGHT);
        }

        void refill() {
            String needle = PointsDeterminerScreen.this.filter.trim().toLowerCase(Locale.ROOT);
            Map<String, String> current = currentValues();
            this.clearEntries();
            for (PointEntry entry : buildEntries()) {
                if (PointsDeterminerScreen.this.valuedOnly && !current.containsKey(entry.key())) continue;
                if (!needle.isEmpty()
                        && !entry.name().getString().toLowerCase(Locale.ROOT).contains(needle)
                        && !entry.key().toLowerCase(Locale.ROOT).contains(needle)) {
                    continue;
                }
                this.addEntry(new Row(entry));
            }
            this.setScrollAmount(0);
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH;
        }

        @OnlyIn(Dist.CLIENT)
        class Row extends ContainerObjectSelectionList.Entry<Row> {
            private final PointEntry entry;
            private final EditBox valueBox;

            Row(PointEntry entry) {
                this.entry = entry;
                this.valueBox = new EditBox(PointsDeterminerScreen.this.font, 0, 0, VALUE_WIDTH, 16, Component.literal(entry.key()));
                this.valueBox.setMaxLength(64);
                this.valueBox.setFilter(value -> value.matches("[0-9.eE+-]*"));
                this.valueBox.setValue(currentValues().getOrDefault(entry.key(), ""));
                this.valueBox.setResponder(value -> {
                    if (value.isBlank()) {
                        currentValues().remove(entry.key());
                    } else {
                        currentValues().put(entry.key(), value.trim());
                    }
                    updateColor(value);
                });
                updateColor(this.valueBox.getValue());
            }

            private void updateColor(String value) {
                boolean valid = value.isBlank() || PointsDeterminerScreen.this.kind.isValidValue(value);
                this.valueBox.setTextColor(valid ? 0xE0E0E0 : 0xFF5555);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                var font = PointsDeterminerScreen.this.font;
                guiGraphics.blitSprite(SLOT_SPRITE, left, top, 18, 18);
                if (!this.entry.icon().isEmpty()) {
                    guiGraphics.renderItem(this.entry.icon(), left + 1, top + 1);
                } else if (this.entry.sprite() != null) {
                    guiGraphics.blit(left + 1, top + 1, 0, 16, 16, this.entry.sprite());
                }

                int textY = top + height / 2 - font.lineHeight / 2;
                int color = index % 2 == 0 ? 0xFFFFFF : 0xAAAAAA;
                int nameSpace = width - 22 - VALUE_WIDTH - 8;
                String name = font.plainSubstrByWidth(this.entry.name().getString(), nameSpace);
                guiGraphics.drawString(font, name, left + 22, textY, color, false);

                this.valueBox.setX(left + width - VALUE_WIDTH - 2);
                this.valueBox.setY(top + 1);
                this.valueBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(this.valueBox);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(this.valueBox);
            }
        }
    }
}

package org.crimsoncrips.craftorio.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.client.config.CraftorioClientConfig;
import org.crimsoncrips.craftorio.server.config.CraftorioServerConfig;

import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class CraftorioConfigScreen extends Screen {

    private enum Tab {
        CLIENT("misc.craftorio.config_tab_client"),
        SERVER("misc.craftorio.config_tab_server");

        private final String translationKey;

        Tab(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    private static final String[] FORMAT_KEYS = {"format_raw", "format_scientific", "format_short_suffix", "format_worded"};

    private static final int TAB_TOP = 24;
    private static final int TAB_WIDTH = 100;
    private static final int TAB_HEIGHT = 20;
    private static final int LIST_TOP = 52;
    private static final int FOOTER_HEIGHT = 36;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_WIDTH = 340;
    private static final int CONTROL_WIDTH = 130;
    private static final int CONTROL_HEIGHT = 18;

    private final Screen parent;
    private Tab tab = Tab.CLIENT;
    private ConfigList list;

    public CraftorioConfigScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.config_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Tab[] tabs = Tab.values();
        int tabsLeft = this.width / 2 - (tabs.length * TAB_WIDTH + (tabs.length - 1) * 2) / 2;
        for (int i = 0; i < tabs.length; i++) {
            Tab entry = tabs[i];
            Button button = Button.builder(Component.translatable(entry.translationKey), b -> {
                this.tab = entry;
                this.rebuildWidgets();
            }).bounds(tabsLeft + i * (TAB_WIDTH + 2), TAB_TOP, TAB_WIDTH, TAB_HEIGHT).build();
            button.active = entry != this.tab;
            this.addRenderableWidget(button);
        }

        this.list = new ConfigList(this.minecraft);
        if (this.tab == Tab.CLIENT) {
            populateClient(Craftorio.CLIENT_CONFIG);
        } else {
            populateServer(Craftorio.SERVER_CONFIG);
        }
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void populateClient(CraftorioClientConfig config) {
        this.list.addHeader("misc.craftorio.config_group_display");
        this.list.addRow(Component.translatable("misc.craftorio.config_point_formatting"), formatButton(config.POINT_FORMATTING));
        this.list.addRow(Component.translatable("misc.craftorio.welcome_toast_seconds_label"), intBox("welcome_toast", config.WELCOME_TOAST_SECONDS));

        this.list.addHeader("misc.craftorio.config_group_animations");
        this.list.addRow(Component.translatable("misc.craftorio.config_skip_claim_animation"), booleanButton(config.SKIP_CONTRACT_CLAIM_ANIMATION));
        this.list.addRow(Component.translatable("misc.craftorio.gold_rain_intensity_label"), intBox("gold_rain", config.GOLD_RAIN_INTENSITY));
    }

    private void populateServer(CraftorioServerConfig config) {
        this.list.addHeader("misc.craftorio.config_group_general");
        addServerRow("UNIVERSAL_PROGRESSION", booleanButton(config.UNIVERSAL_PROGRESSION));
        addServerRow("STARTING_POINTS", stringBox("STARTING_POINTS", config.STARTING_POINTS));
        addServerRow("INSTANT_DEATH_OUTSIDE_CLAIM", booleanButton(config.INSTANT_DEATH_OUTSIDE_CLAIM));
        addServerRow("MIN_SPAWN_DISTANCE", doubleBox("MIN_SPAWN_DISTANCE", config.MIN_SPAWN_DISTANCE));
        addServerRow("MAX_SPAWN_DISTANCE", doubleBox("MAX_SPAWN_DISTANCE", config.MAX_SPAWN_DISTANCE));
        addServerRow("MULT_PER_CONTRACT_DONE", doubleBox("MULT_PER_CONTRACT_DONE", config.MULT_PER_CONTRACT_DONE));

        this.list.addHeader("misc.craftorio.config_group_expansion");
        addServerRow("CHUNK_BASED_EXPANSION", booleanButton(config.CHUNK_BASED_EXPANSION));
        addServerRow("STARTING_LAND_SIZE", intBox("STARTING_LAND_SIZE", config.STARTING_LAND_SIZE));
        addServerRow("COST_MULTIPLIER", doubleBox("COST_MULTIPLIER", config.COST_MULTIPLIER));
        addServerRow("BASE_COST", intBox("BASE_COST", config.BASE_COST));

        this.list.addHeader("misc.craftorio.config_group_border_based");
        addServerRow("EXPANSION_AMOUNT", intBox("EXPANSION_AMOUNT", config.EXPANSION_AMOUNT));

        this.list.addHeader("misc.craftorio.config_group_chunk_based");
        addServerRow("NO_BORDERS", booleanButton(config.NO_BORDERS));
        addServerRow("CHUNK_OUT_OF_BOUNDS_DAMAGE", doubleBox("CHUNK_OUT_OF_BOUNDS_DAMAGE", config.CHUNK_OUT_OF_BOUNDS_DAMAGE));

        this.list.addHeader("misc.craftorio.config_group_random_effects");
        addServerRow("RANDOM_EFFECTS_ENABLED", booleanButton(config.RANDOM_EFFECTS_ENABLED));
        addServerRow("RANDOM_EFFECT_INTERVAL", intBox("RANDOM_EFFECT_INTERVAL", config.RANDOM_EFFECT_INTERVAL));

        this.list.addHeader("misc.craftorio.config_group_shop");
        addServerRow("SHOP_MODE", enumButton(config.SHOP_MODE));
        addServerRow("SHOP_COST_MULTIPLIER", intBox("SHOP_COST_MULTIPLIER", config.SHOP_COST_MULTIPLIER));

        this.list.addHeader("misc.craftorio.config_group_contracts");
        addServerRow("MAX_OFFERED_CONTRACTS", intBox("MAX_OFFERED_CONTRACTS", config.MAX_OFFERED_CONTRACTS));
        addServerRow("CONTRACT_REFRESH_SECONDS", intBox("CONTRACT_REFRESH_SECONDS", config.CONTRACT_REFRESH_SECONDS));
        addServerRow("CONTRACT_REFRESH_COST_PERCENT", doubleBox("CONTRACT_REFRESH_COST_PERCENT", config.CONTRACT_REFRESH_COST_PERCENT));

        this.list.addHeader("misc.craftorio.config_group_sink_value");
        addServerRow("SINK_VALUE_BONUS_AMOUNT", intBox("SINK_VALUE_BONUS_AMOUNT", config.SINK_VALUE_BONUS_AMOUNT));
        addServerRow("SINK_VALUE_BONUS_THRESHOLD", intBox("SINK_VALUE_BONUS_THRESHOLD", config.SINK_VALUE_BONUS_THRESHOLD));

        this.list.addHeader("misc.craftorio.config_group_rebirth");
        addServerRow("REBIRTH_BASE_COST", stringBox("REBIRTH_BASE_COST", config.REBIRTH_BASE_COST));
        addServerRow("REBIRTH_BASE_LIFE_POINTS", intBox("REBIRTH_BASE_LIFE_POINTS", config.REBIRTH_BASE_LIFE_POINTS));
        addServerRow("REBIRTH_SKIP_BONUS_PERCENT", doubleBox("REBIRTH_SKIP_BONUS_PERCENT", config.REBIRTH_SKIP_BONUS_PERCENT));
        addServerRow("REBIRTH_MAX_SKIP", intBox("REBIRTH_MAX_SKIP", config.REBIRTH_MAX_SKIP));
    }

    private void addServerRow(String name, AbstractWidget control) {
        this.list.addRow(Component.literal(name), control);
    }

    private Button booleanButton(ModConfigSpec.BooleanValue value) {
        return Button.builder(booleanLabel(value.get()), b -> {
            value.set(!value.get());
            b.setMessage(booleanLabel(value.get()));
        }).bounds(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT).build();
    }

    private static Component booleanLabel(boolean value) {
        return value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    private <E extends Enum<E>> Button enumButton(ModConfigSpec.EnumValue<E> value) {
        return Button.builder(Component.literal(value.get().name()), b -> {
            E[] constants = value.get().getDeclaringClass().getEnumConstants();
            value.set(constants[(value.get().ordinal() + 1) % constants.length]);
            b.setMessage(Component.literal(value.get().name()));
        }).bounds(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT).build();
    }

    private Button formatButton(ModConfigSpec.IntValue value) {
        Function<Integer, Component> label = index -> Component.translatable("misc.craftorio." + FORMAT_KEYS[Mth.clamp(index, 0, FORMAT_KEYS.length - 1)]);
        return Button.builder(label.apply(value.get()), b -> {
            value.set((value.get() + 1) % FORMAT_KEYS.length);
            b.setMessage(label.apply(value.get()));
        }).bounds(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT).build();
    }

    private EditBox intBox(String name, ModConfigSpec.IntValue value) {
        ModConfigSpec.Range<Integer> range = value.getSpec().getRange();
        EditBox box = new EditBox(this.font, 0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.literal(name));
        box.setMaxLength(32);
        box.setValue(String.valueOf(value.get()));
        box.setResponder(text -> {
            try {
                value.set(Mth.clamp(Integer.parseInt(text.trim()), range.getMin(), range.getMax()));
            } catch (NumberFormatException ignored) {
            }
        });
        return box;
    }

    private EditBox doubleBox(String name, ModConfigSpec.DoubleValue value) {
        ModConfigSpec.Range<Double> range = value.getSpec().getRange();
        EditBox box = new EditBox(this.font, 0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.literal(name));
        box.setMaxLength(32);
        box.setValue(String.valueOf(value.get()));
        box.setResponder(text -> {
            try {
                value.set(Mth.clamp(Double.parseDouble(text.trim()), range.getMin(), range.getMax()));
            } catch (NumberFormatException ignored) {
            }
        });
        return box;
    }

    private EditBox stringBox(String name, ModConfigSpec.ConfigValue<String> value) {
        EditBox box = new EditBox(this.font, 0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.literal(name));
        box.setMaxLength(256);
        box.setValue(value.get());
        box.setResponder(value::set);
        return box;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        Craftorio.CLIENT_CONFIG_SPEC.save();
        Craftorio.SERVER_CONFIG_SPEC.save();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {

        ConfigList(Minecraft minecraft) {
            super(minecraft, CraftorioConfigScreen.this.width, CraftorioConfigScreen.this.height - LIST_TOP - FOOTER_HEIGHT, LIST_TOP, ROW_HEIGHT);
        }

        void addHeader(String translationKey) {
            this.addEntry(new HeaderEntry(Component.translatable(translationKey)));
        }

        void addRow(Component label, AbstractWidget control) {
            this.addEntry(new RowEntry(label, control));
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        @OnlyIn(Dist.CLIENT)
        abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        }

        @OnlyIn(Dist.CLIENT)
        class HeaderEntry extends Entry {
            private final Component title;

            HeaderEntry(Component title) {
                this.title = title;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = CraftorioConfigScreen.this.font;
                guiGraphics.drawString(font, this.title, left + 2, top + height - font.lineHeight - 4, 0xFFD84A, false);
                guiGraphics.fill(left, top + height - 2, left + width, top + height - 1, 0x60FFFFFF);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RowEntry extends Entry {
            private final Component label;
            private final AbstractWidget control;

            RowEntry(Component label, AbstractWidget control) {
                this.label = label;
                this.control = control;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = CraftorioConfigScreen.this.font;
                int available = width - CONTROL_WIDTH - 12;
                int textWidth = font.width(this.label);
                float scale = textWidth > available ? available / (float) textWidth : 1f;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(left + 4, top + (height - font.lineHeight * scale) / 2f, 0f);
                guiGraphics.pose().scale(scale, scale, 1f);
                guiGraphics.drawString(font, this.label, 0, 0, 0xFFFFFF, false);
                guiGraphics.pose().popPose();

                this.control.setX(left + width - CONTROL_WIDTH);
                this.control.setY(top + (height - CONTROL_HEIGHT) / 2);
                this.control.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(this.control);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(this.control);
            }
        }
    }
}

package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.server.CraftorioServerConfig;
import org.crimsoncrips.craftorio.server.CraftorioShopMode;

@OnlyIn(Dist.CLIENT)
public class CraftorioServerConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int MIN_ROWS_PER_COLUMN = 3;
    private static final int START_Y = 30;
    private static final int BOTTOM_RESERVED = 40;
    private static final int COLUMN_WIDTH = 260;
    private static final int LABEL_WIDTH = 150;
    private static final int FIELD_WIDTH = 90;
    private static final int WIDGET_HEIGHT = 18;

    private final Screen parent;
    private int row = 0;
    private int column = 0;
    private int rowsPerColumn = MIN_ROWS_PER_COLUMN;

    public CraftorioServerConfigScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.server_config_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.row = 0;
        this.column = 0;
        this.rowsPerColumn = Math.max(MIN_ROWS_PER_COLUMN, (this.height - START_Y - BOTTOM_RESERVED) / ROW_HEIGHT);

        CraftorioServerConfig config = Craftorio.SERVER_CONFIG;

        addBooleanRow("CHUNK_BASED_EXPANSION", config.CHUNK_BASED_EXPANSION);
        addBooleanRow("UNIVERSAL_PROGRESSION", config.UNIVERSAL_PROGRESSION);
        addBooleanRow("NO_BORDERS", config.NO_BORDERS);
        addBooleanRow("RANDOM_EFFECTS_ENABLED", config.RANDOM_EFFECTS_ENABLED);
        addBooleanRow("INSTANT_DEATH_OUTSIDE_CLAIM", config.INSTANT_DEATH_OUTSIDE_CLAIM);
        addEnumRow("SHOP_MODE", config.SHOP_MODE);

        addIntRow("STARTING_LAND_SIZE", config.STARTING_LAND_SIZE);
        addDoubleRow("COST_MULTIPLIER", config.COST_MULTIPLIER);
        addIntRow("SHOP_COST_MULTIPLIER", config.SHOP_COST_MULTIPLIER);
        addIntRow("BASE_COST", config.BASE_COST);
        addIntRow("EXPANSION_AMOUNT", config.EXPANSION_AMOUNT);
        addStringRow("STARTING_POINTS", config.STARTING_POINTS);
        addDoubleRow("MIN_SPAWN_DISTANCE", config.MIN_SPAWN_DISTANCE);
        addDoubleRow("MAX_SPAWN_DISTANCE", config.MAX_SPAWN_DISTANCE);
        addDoubleRow("CHUNK_OUT_OF_BOUNDS_DAMAGE", config.CHUNK_OUT_OF_BOUNDS_DAMAGE);
        addIntRow("RANDOM_EFFECT_MIN_INTERVAL", config.RANDOM_EFFECT_MIN_INTERVAL);
        addIntRow("RANDOM_EFFECT_MAX_INTERVAL", config.RANDOM_EFFECT_MAX_INTERVAL);
        addIntRow("MAX_OFFERED_CONTRACTS", config.MAX_OFFERED_CONTRACTS);
        addIntRow("CONTRACT_REFRESH_SECONDS", config.CONTRACT_REFRESH_SECONDS);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20).build());
    }

    private int columnX() {
        return 20 + column * COLUMN_WIDTH;
    }

    private int rowY() {
        return START_Y + row * ROW_HEIGHT;
    }

    private void advanceRow() {
        row++;
        if (row >= this.rowsPerColumn) {
            row = 0;
            column++;
        }
    }

    private void addBooleanRow(String label, ModConfigSpec.BooleanValue value) {
        int x = columnX();
        int y = rowY();
        Button button = Button.builder(Component.literal(label + ": " + value.get()), b -> {
            value.set(!value.get());
            b.setMessage(Component.literal(label + ": " + value.get()));
        }).bounds(x, y, LABEL_WIDTH + FIELD_WIDTH, WIDGET_HEIGHT).build();
        this.addRenderableWidget(button);
        advanceRow();
    }

    private void addEnumRow(String label, ModConfigSpec.EnumValue<CraftorioShopMode> value) {
        int x = columnX();
        int y = rowY();
        CraftorioShopMode[] modes = CraftorioShopMode.values();
        Button button = Button.builder(Component.literal(label + ": " + value.get()), b -> {
            int next = (value.get().ordinal() + 1) % modes.length;
            value.set(modes[next]);
            b.setMessage(Component.literal(label + ": " + value.get()));
        }).bounds(x, y, LABEL_WIDTH + FIELD_WIDTH, WIDGET_HEIGHT).build();
        this.addRenderableWidget(button);
        advanceRow();
    }

    private void addIntRow(String label, ModConfigSpec.IntValue value) {
        int x = columnX();
        int y = rowY();
        this.addRenderableWidget(new FieldLabel(x, y, label));

        EditBox box = new EditBox(this.font, x + LABEL_WIDTH, y, FIELD_WIDTH, WIDGET_HEIGHT, Component.literal(label));
        box.setValue(String.valueOf(value.get()));
        box.setResponder(text -> {
            try {
                value.set(Integer.parseInt(text.trim()));
            } catch (NumberFormatException ignored) {
            }
        });
        this.addRenderableWidget(box);
        advanceRow();
    }

    private void addDoubleRow(String label, ModConfigSpec.DoubleValue value) {
        int x = columnX();
        int y = rowY();
        this.addRenderableWidget(new FieldLabel(x, y, label));

        EditBox box = new EditBox(this.font, x + LABEL_WIDTH, y, FIELD_WIDTH, WIDGET_HEIGHT, Component.literal(label));
        box.setValue(String.valueOf(value.get()));
        box.setResponder(text -> {
            try {
                value.set(Double.parseDouble(text.trim()));
            } catch (NumberFormatException ignored) {
            }
        });
        this.addRenderableWidget(box);
        advanceRow();
    }

    private void addStringRow(String label, ModConfigSpec.ConfigValue<String> value) {
        int x = columnX();
        int y = rowY();
        this.addRenderableWidget(new FieldLabel(x, y, label));

        EditBox box = new EditBox(this.font, x + LABEL_WIDTH, y, FIELD_WIDTH, WIDGET_HEIGHT, Component.literal(label));
        box.setValue(value.get());
        box.setResponder(value::set);
        this.addRenderableWidget(box);
        advanceRow();
    }

    private class FieldLabel extends AbstractWidget {
        private final String label;

        FieldLabel(int x, int y, String label) {
            super(x, y, LABEL_WIDTH - 6, WIDGET_HEIGHT, Component.literal(label));
            this.label = label;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.drawString(CraftorioServerConfigScreen.this.font, this.label, this.getX(), this.getY() + 5, 0xFFFFFF, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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
}

package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EffectDetailsScreen extends Screen {

    private static final int TOP_MARGIN = 40;
    private static final int ICON_SIZE = 24;
    private static final int TAG_COLUMNS = 6;
    private static final int TAG_CELL_SIZE = 24;
    private static final int TAG_ICON_SIZE = 16;

    private final Screen parent;
    private final CraftorioEffects effect;
    private final List<ItemStack> tagItemStacks = new ArrayList<>();

    public EffectDetailsScreen(Screen parent, CraftorioEffects effect) {
        super(Component.literal(effect.getActualName()));
        this.parent = parent;
        this.effect = effect;

        if (effect instanceof TagMultiplierEffect tagEffect) {
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagEffect.getItemTag())) {
                Item item = holder.value();
                this.tagItemStacks.add(item == null || item == Items.AIR ? new ItemStack(Items.BARRIER) : new ItemStack(item));
            }
        }
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.effect.getActualName(), this.width / 2, 16, ClientEvents.activeEffectColor(this.effect));

        boolean isTag = this.effect instanceof TagMultiplierEffect;
        int leftColX = isTag ? this.width / 4 : this.width / 2;
        int leftColWidth = isTag ? this.width / 2 - 20 : this.width - 40;
        int x = leftColX - leftColWidth / 2;
        int y = TOP_MARGIN;

        if (this.effect.getIcon() != null) {
            graphics.blit(this.effect.getIcon(), leftColX - ICON_SIZE / 2, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            y += ICON_SIZE + 6;
        }

        y = drawLine(graphics, x, y, Component.translatable("misc.craftorio.effect_type_label", effectTypeName()).getString(), 0xFFFFFF);
        y = drawLine(graphics, x, y, effectMultiplierLine(), 0xFFDD55);
        y = drawLine(graphics, x, y, Component.translatable("misc.craftorio.contract_time_remaining",
                CraftorioMisc.ticksToTimeString(this.effect.getTime())).getString(), 0xAAAAAA);

        if (this.effect instanceof TagMultiplierEffect tagEffect) {
            String tagLine = Component.translatable("misc.craftorio.effect_tag_label",
                    "#" + tagEffect.getItemTag().location()).getString();
            drawWrappedLine(graphics, x, y, leftColWidth, tagLine, 0xFFFFFF);

            int rightColX = this.width * 3 / 4;
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.effect_tag_items_title"), rightColX, TOP_MARGIN - 14, 0xFFFFFF);
            renderTagItems(graphics, rightColX, mouseX, mouseY);
        }
    }

    private String effectTypeName() {
        if (this.effect instanceof TagMultiplierEffect) return Component.translatable("misc.craftorio.effect_type_tag").getString();
        if (this.effect instanceof ShopMultiplierEffect) return Component.translatable("misc.craftorio.effect_type_shop").getString();
        return Component.translatable("misc.craftorio.effect_type_general").getString();
    }

    private String effectMultiplierLine() {
        float multiplier = 0f;
        if (this.effect instanceof GeneralMultiplierEffect general) multiplier = general.getMultiplier();
        else if (this.effect instanceof TagMultiplierEffect tag) multiplier = tag.getMultiplier();
        else if (this.effect instanceof ShopMultiplierEffect shop) multiplier = shop.getMultiplier();

        return Component.translatable("misc.craftorio.effect_multiplier_label", String.format("%+.0f%%", multiplier * 100)).getString();
    }

    private int drawLine(GuiGraphics graphics, int x, int y, String text, int color) {
        graphics.drawString(this.font, text, x, y, color, true);
        return y + this.font.lineHeight + 4;
    }

    private void drawWrappedLine(GuiGraphics graphics, int x, int y, int wrapWidth, String text, int color) {
        for (var line : this.font.split(Component.literal(text), wrapWidth)) {
            graphics.drawString(this.font, line, x, y, color, true);
            y += this.font.lineHeight;
        }
    }

    private void renderTagItems(GuiGraphics graphics, int centerX, int mouseX, int mouseY) {
        int gridWidth = TAG_COLUMNS * TAG_CELL_SIZE;
        int startX = centerX - gridWidth / 2;
        int startY = TOP_MARGIN;

        for (int i = 0; i < this.tagItemStacks.size(); i++) {
            int col = i % TAG_COLUMNS;
            int row = i / TAG_COLUMNS;
            int x = startX + col * TAG_CELL_SIZE;
            int y = startY + row * TAG_CELL_SIZE;

            ItemStack stack = this.tagItemStacks.get(i);
            graphics.renderItem(stack, x, y);

            if (mouseX >= x && mouseX <= x + TAG_ICON_SIZE && mouseY >= y && mouseY <= y + TAG_ICON_SIZE) {
                graphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

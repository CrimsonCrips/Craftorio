package org.crimsoncrips.craftorio.client.screen.schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.client.schematic.CraftorioSchematicRenderer;
import org.crimsoncrips.craftorio.client.schematic.SchematicVerifier;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class SchematicIssuesScreen extends Screen implements ScrollableScreen {

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_WIDTH = 300;
    private static final int LIST_TOP = 32;
    private static final int FOOTER_HEIGHT = 36;
    private static final int REFRESH_TICKS = 10;

    private record Group(SchematicVerifier.Kind kind, BlockState state, int count) {}

    private final Screen parent;
    private final InteractionHand hand;
    private IssueList list;
    private int ticks;

    public SchematicIssuesScreen(Screen parent, InteractionHand hand) {
        super(Component.translatable("misc.craftorio.schematic_issues_title"));
        this.parent = parent;
        this.hand = hand;
    }

    @Override
    protected void init() {
        this.list = new IssueList(this.minecraft);
        this.list.refill();
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private Optional<SchematicVerifier.Result> result() {
        Player player = this.minecraft.player;
        if (player == null) return Optional.empty();
        SchematicData data = player.getItemInHand(this.hand).get(CraftorioDataComponents.SCHEMATIC.get());
        return data == null ? Optional.empty() : CraftorioSchematicRenderer.result(data);
    }

    private List<Group> groups() {
        Optional<SchematicVerifier.Result> result = result();
        if (result.isEmpty()) return List.of();

        Map<String, Group> grouped = new LinkedHashMap<>();
        for (SchematicVerifier.Kind kind : SchematicVerifier.Kind.values()) {
            for (SchematicVerifier.Issue issue : result.get().issues()) {
                if (issue.kind() != kind) continue;
                BlockState state = kind == SchematicVerifier.Kind.EXTRA ? issue.actual() : issue.expected();
                String key = kind.name() + "|" + state.getBlock().getDescriptionId();
                Group existing = grouped.get(key);
                grouped.put(key, new Group(kind, state, existing == null ? 1 : existing.count() + 1));
            }
        }

        List<Group> sorted = new ArrayList<>(grouped.values());
        sorted.sort((a, b) -> a.kind() != b.kind() ? a.kind().compareTo(b.kind()) : Integer.compare(b.count(), a.count()));
        return sorted;
    }

    @Override
    public void tick() {
        if (++this.ticks % REFRESH_TICKS == 0) {
            this.list.refill();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        Optional<SchematicVerifier.Result> result = result();
        if (result.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.schematic_issues_unavailable"), this.width / 2, this.height / 2, 0xAAAAAA);
        } else if (result.get().issues().isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.schematic_issues_none"), this.width / 2, this.height / 2, 0x55FF55);
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
    private class IssueList extends ObjectSelectionList<IssueList.Row> {

        IssueList(Minecraft minecraft) {
            super(minecraft, SchematicIssuesScreen.this.width, SchematicIssuesScreen.this.height - LIST_TOP - FOOTER_HEIGHT, LIST_TOP, ROW_HEIGHT);
        }

        void refill() {
            double scroll = this.getScrollAmount();
            this.clearEntries();
            SchematicVerifier.Kind header = null;
            for (Group group : groups()) {
                if (group.kind() != header) {
                    header = group.kind();
                    this.addEntry(new Row(header, null));
                }
                this.addEntry(new Row(group.kind(), group));
            }
            this.setScrollAmount(scroll);
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH;
        }

        @OnlyIn(Dist.CLIENT)
        class Row extends ObjectSelectionList.Entry<Row> {
            private final SchematicVerifier.Kind kind;
            private final Group group;

            Row(SchematicVerifier.Kind kind, Group group) {
                this.kind = kind;
                this.group = group;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                var font = SchematicIssuesScreen.this.font;
                int textY = top + height / 2 - font.lineHeight / 2;

                if (this.group == null) {
                    graphics.drawString(font, Component.translatable("misc.craftorio.schematic_issues_" + this.kind.name().toLowerCase(Locale.ROOT)), left + 2, textY, this.kind.color(), false);
                    return;
                }

                Block block = this.group.state().getBlock();
                ItemStack icon = iconFor(block);
                if (!icon.isEmpty()) {
                    graphics.renderItem(icon, left + 12, top + (height - 16) / 2);
                }
                graphics.drawString(font, block.getName(), left + 32, textY, 0xFFFFFF, false);
                String count = "x" + this.group.count();
                graphics.drawString(font, count, left + width - font.width(count) - 4, textY, 0xAAAAAA, false);
            }

            private ItemStack iconFor(Block block) {
                if (block instanceof LiquidBlock liquid) {
                    return new ItemStack(liquid.fluid.getBucket());
                }
                return new ItemStack(block.asItem());
            }

            @Override
            public Component getNarration() {
                return this.group == null ? Component.empty() : this.group.state().getBlock().getName();
            }
        }
    }
}

package org.crimsoncrips.craftorio.client.screen.schematic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.networking.schematic.SchematicActionPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class SchematicScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 240;
    private static final int PADDING = 10;
    private static final int STATUS_TOP = 26;
    private static final int STATUS_LINE_HEIGHT = 12;
    private static final int STATUS_LINES = 3;
    private static final int BUTTONS_TOP = STATUS_TOP + STATUS_LINES * STATUS_LINE_HEIGHT + 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_ROW_GAP = 8;
    private static final int WARNING_GAP = 12;
    private static final int BOTTOM_PADDING = 30;

    private final InteractionHand hand;
    private int panelLeft;
    private int panelTop;
    private int panelHeight;
    private Button rotateButton;
    private Button moveButton;
    private Button submitButton;
    private Button instaCompleteButton;

    public SchematicScreen(InteractionHand hand) {
        super(Component.translatable("item.craftorio.schematic"));
        this.hand = hand;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    private SchematicData data() {
        Player player = this.minecraft.player;
        if (player == null) return null;
        ItemStack stack = player.getItemInHand(this.hand);
        return stack.get(CraftorioDataComponents.SCHEMATIC.get());
    }

    private boolean isCreative() {
        Player player = this.minecraft.player;
        return player != null && player.isCreative();
    }

    @Override
    protected void init() {
        boolean creative = isCreative();
        List<FormattedCharSequence> warning = this.font.split(Component.translatable("misc.craftorio.schematic_menu_warning").withStyle(ChatFormatting.RED), PANEL_WIDTH - PADDING * 2);

        int rowsBelowSubmit = creative ? 2 : 1;
        int buttonsBottom = BUTTONS_TOP + BUTTON_HEIGHT + rowsBelowSubmit * (BUTTON_ROW_GAP + BUTTON_HEIGHT);
        this.panelHeight = buttonsBottom + WARNING_GAP + warning.size() * this.font.lineHeight + BOTTOM_PADDING;

        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        int left = this.panelLeft + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int y = this.panelTop + BUTTONS_TOP;

        this.rotateButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.schematic_menu_rotate"), b -> send(SchematicActionPacket.ROTATE))
                .bounds(left, y, width / 2 - 2, BUTTON_HEIGHT).build());
        this.moveButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.schematic_menu_move"), b -> send(SchematicActionPacket.PICK_UP))
                .bounds(left + width / 2 + 2, y, width / 2 - 2, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("misc.craftorio.schematic_menu_move_tooltip"))).build());
        y += BUTTON_HEIGHT + BUTTON_ROW_GAP;

        this.submitButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.schematic_menu_submit").withStyle(ChatFormatting.GREEN), b -> {
                    send(SchematicActionPacket.SUBMIT);
                    this.onClose();
                })
                .bounds(left, y, width, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("misc.craftorio.schematic_menu_submit_tooltip"))).build());
        y += BUTTON_HEIGHT + BUTTON_ROW_GAP;

        if (creative) {
            this.instaCompleteButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.schematic_menu_insta_complete").withStyle(ChatFormatting.YELLOW), b -> send(SchematicActionPacket.INSTA_COMPLETE))
                    .bounds(left, y, width, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable("misc.craftorio.schematic_menu_insta_complete_tooltip"))).build());
            y += BUTTON_HEIGHT + BUTTON_ROW_GAP;
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.panelLeft + PANEL_WIDTH / 2 - 50, y + 25, 100, BUTTON_HEIGHT).build());
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new SchematicActionPacket(this.hand, action));
    }

    @Override
    public void tick() {
        SchematicData data = data();
        if (data == null) {
            this.onClose();
            return;
        }

        Player player = this.minecraft.player;
        Optional<CraftorioContract> contract = player == null ? Optional.empty() : CraftorioSchematics.findContract(player, data.instance());
        boolean settled = contract.map(active -> active.getProgress().submitted()).orElse(false);
        boolean placed = data.origin().isPresent();

        this.rotateButton.active = !settled;
        this.moveButton.active = placed && !settled;
        this.submitButton.active = placed && !settled;
        if (this.instaCompleteButton != null) {
            this.instaCompleteButton.active = placed && !settled;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + this.panelHeight, 0xE0202020);
        graphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, this.panelHeight, 0xFF808080);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 8, 0xFFFFFF);

        SchematicData data = data();
        int left = this.panelLeft + PADDING;
        int y = this.panelTop + STATUS_TOP;
        if (data != null) {
            graphics.drawString(this.font, Component.translatable("misc.craftorio.schematic_owner",
                    data.owner().isEmpty() ? Component.translatable("misc.craftorio.schematic_owner_any") : Component.literal(data.owner())), left, y, 0x55FFFF, false);
            y += STATUS_LINE_HEIGHT;
            graphics.drawString(this.font, Component.translatable("misc.craftorio.schematic_rotation", data.rotation().ordinal() * 90), left, y, 0xAAAAAA, false);
            y += STATUS_LINE_HEIGHT;

            Player player = this.minecraft.player;
            Optional<CraftorioContract> contract = player == null ? Optional.empty() : CraftorioSchematics.findContract(player, data.instance());
            if (contract.isPresent() && contract.get().getProgress().submitted()) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.schematic_settled"), left, y, 0x55FF55, false);
            } else if (contract.isPresent()) {
                int placed = contract.get().getProgress().blocksPlaced();
                int total = contract.get().getProgress().blocksTotal();
                graphics.drawString(this.font, Component.translatable("misc.craftorio.schematic_progress", placed, total), left, y,
                        total > 0 && placed >= total ? 0x55FF55 : 0xFFAA00, false);
            } else {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.schematic_inactive"), left, y, 0xFF5555, false);
            }
        }

        List<FormattedCharSequence> warning = this.font.split(Component.translatable("misc.craftorio.schematic_menu_warning").withStyle(ChatFormatting.RED), PANEL_WIDTH - PADDING * 2);
        int warningY = this.panelTop + this.panelHeight - BOTTOM_PADDING - warning.size() * this.font.lineHeight;
        for (FormattedCharSequence line : warning) {
            graphics.drawString(this.font, line, left, warningY, 0xFFFFFF, false);
            warningY += this.font.lineHeight;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

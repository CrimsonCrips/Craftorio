package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.AbandonContractPacket;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OwnedContractsScreen extends Screen {

    private static final ResourceLocation CARD_TEXTURE = Craftorio.getGuiTexture("contract.png");
    private static final int BASE_CARD_WIDTH = 120;
    private static final int BASE_CARD_HEIGHT = 144;
    private static final int MIN_CARD_HEIGHT = 70;
    private static final int CARD_GAP = 20;
    private static final int TOP_MARGIN = 40;
    private static final int BOTTOM_MARGIN = 46;
    private static final int SIDE_MARGIN = 30;
    private static final int ICON_SIZE = 24;
    private static final int ICON_GAP = 6;
    private static final int SHADOW_OFFSET = 5;
    private static final int SHADOW_ALPHA = 0x80;
    private static final float TEXT_SCALE = 0.8f;
    private static final float HOVER_SCALE = 1.15f;
    private static final float HOVER_LERP = 0.2f;
    private static final float SCROLL_SPEED = 30f;
    private static final int SCROLLBAR_HEIGHT = 4;
    private static final int SCROLLBAR_GAP = 6;
    private static final int DRAG_THRESHOLD = 2;
    private static final int ABANDON_BUTTON_WIDTH = 60;
    private static final int ABANDON_BUTTON_HEIGHT = 14;
    private static final int ABANDON_BUTTON_GAP = 6;

    private final Screen parent;
    private final List<CraftorioShipmentContract> contracts = new ArrayList<>();
    private final List<Float> hoverScales = new ArrayList<>();

    private int cardWidth = BASE_CARD_WIDTH;
    private int cardHeight = BASE_CARD_HEIGHT;
    private int viewportLeft;
    private int viewportRight;
    private int viewportTop;
    private int viewportBottom;
    private int cardCenterY;
    private float scrollX = 0f;
    private int maxScroll = 0;

    private boolean dragging = false;
    private boolean hasDragged = false;
    private double dragStartX;
    private float dragStartScroll;

    public OwnedContractsScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.owned_contracts_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Player player = this.minecraft.player;
        this.contracts.clear();
        if (player != null) {
            this.contracts.addAll(CraftorioMisc.getCraftorioContracts(player));
        }

        this.hoverScales.clear();
        for (int i = 0; i < this.contracts.size(); i++) {
            this.hoverScales.add(1.0f);
        }

        this.viewportLeft = SIDE_MARGIN;
        this.viewportRight = this.width - SIDE_MARGIN;
        this.viewportTop = TOP_MARGIN;
        this.viewportBottom = this.height - BOTTOM_MARGIN;

        float aspect = (float) BASE_CARD_WIDTH / BASE_CARD_HEIGHT;
        float hoverOverflow = (HOVER_SCALE - 1f) / 2f;
        int abandonArea = ABANDON_BUTTON_GAP + ABANDON_BUTTON_HEIGHT;
        int availableHeight = Math.max(MIN_CARD_HEIGHT, (int) ((this.viewportBottom - this.viewportTop - abandonArea) / (1f + hoverOverflow)));
        this.cardHeight = Math.min(BASE_CARD_HEIGHT, availableHeight);
        this.cardWidth = Math.round(this.cardHeight * aspect);

        this.cardCenterY = this.viewportTop + this.cardHeight / 2;

        int viewportWidth = Math.max(0, this.viewportRight - this.viewportLeft);
        int contentWidth = this.contracts.isEmpty() ? 0
                : this.contracts.size() * this.cardWidth + (this.contracts.size() - 1) * CARD_GAP;
        this.maxScroll = Math.max(0, contentWidth - viewportWidth);
        this.scrollX = Mth.clamp(this.scrollX, 0, this.maxScroll);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, 16, 0xFFFFFF);

        if (this.contracts.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.no_contracts"), this.width / 2, this.cardCenterY, 0xFFFFFF);
            return;
        }

        int hoverMarginY = Math.round(this.cardHeight * (HOVER_SCALE - 1f) / 2f) + 1;
        graphics.enableScissor(this.viewportLeft, this.viewportTop - hoverMarginY, this.viewportRight, this.viewportBottom + hoverMarginY);

        int x = this.viewportLeft - Math.round(this.scrollX);
        for (int i = 0; i < this.contracts.size(); i++) {
            renderCard(graphics, this.contracts.get(i), i, x, mouseX, mouseY);
            x += this.cardWidth + CARD_GAP;
        }

        graphics.disableScissor();

        if (this.maxScroll > 0) {
            renderScrollbar(graphics);
        }
    }

    private void renderCard(GuiGraphics graphics, CraftorioShipmentContract contract, int index, int left, int mouseX, int mouseY) {
        if (left + this.cardWidth < this.viewportLeft || left > this.viewportRight) return;

        int centerX = left + this.cardWidth / 2;
        int top = this.cardCenterY - this.cardHeight / 2;

        boolean hovered = mouseX >= left && mouseX <= left + this.cardWidth
                && mouseY >= top && mouseY <= top + this.cardHeight
                && mouseX >= this.viewportLeft && mouseX <= this.viewportRight;

        float hoverScale = this.hoverScales.get(index);
        hoverScale += ((hovered ? HOVER_SCALE : 1.0f) - hoverScale) * HOVER_LERP;
        this.hoverScales.set(index, hoverScale);

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, this.cardCenterY, 0);
        graphics.pose().scale(hoverScale, hoverScale, 1f);

        graphics.setColor(0f, 0f, 0f, SHADOW_ALPHA / 255f);
        graphics.blit(CARD_TEXTURE, -this.cardWidth / 2 + SHADOW_OFFSET, -this.cardHeight / 2 + SHADOW_OFFSET, 0, 0, this.cardWidth, this.cardHeight, this.cardWidth, this.cardHeight);
        graphics.setColor(1f, 1f, 1f, 1f);

        graphics.blit(CARD_TEXTURE, -this.cardWidth / 2, -this.cardHeight / 2, 0, 0, this.cardWidth, this.cardHeight, this.cardWidth, this.cardHeight);

        graphics.pose().popPose();

        if (hovered) {
            graphics.fill(left, top, left + this.cardWidth, top + this.cardHeight, 0x30FFFFFF);
        }

        float textScale = TEXT_SCALE * this.cardWidth / BASE_CARD_WIDTH * hoverScale;
        int wrapWidth = (int) ((this.cardWidth - 16) / textScale);
        float textTop = top + 12;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, textTop, 0);
        graphics.pose().scale(textScale, textScale, 1f);

        float y = 0;
        if (contract.getIcon() != null) {
            graphics.blit(contract.getIcon(), -ICON_SIZE / 2, 0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            y = ICON_SIZE + ICON_GAP;
        }

        for (var line : this.font.split(Component.literal(contract.getName()), wrapWidth)) {
            graphics.drawString(this.font, line, -this.font.width(line) / 2, (int) y, 0xFFFFFF, true);
            y += this.font.lineHeight;
        }

        if (contract.isAbandoned()) {
            Component abandonedLine = Component.translatable("misc.craftorio.contract_abandoned")
                    .withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true).withItalic(true));
            var line = abandonedLine.getVisualOrderText();
            graphics.drawString(this.font, line, -this.font.width(abandonedLine) / 2, (int) y, 0xFFFFFF, true);
        } else {
            String timeLine = Component.translatable("misc.craftorio.contract_time_remaining", CraftorioMisc.ticksToTimeString(contract.getTime())).getString();
            graphics.drawString(this.font, timeLine, -this.font.width(timeLine) / 2, (int) y, 0xAAAAAA, true);
        }
        y += this.font.lineHeight;

        Component punishmentLine = punishmentLine(contract);
        if (!punishmentLine.getString().isEmpty()) {
            for (var line : this.font.split(punishmentLine, wrapWidth)) {
                graphics.drawString(this.font, line, -this.font.width(line) / 2, (int) y, 0xFFFFFF, true);
                y += this.font.lineHeight;
            }
        }

        graphics.pose().popPose();

        if (!contract.isAbandoned()) {
            renderAbandonButton(graphics, contract, centerX, top + this.cardHeight + ABANDON_BUTTON_GAP, mouseX, mouseY);
        }
    }

    private Component punishmentLine(CraftorioShipmentContract contract) {
        ResourceLocation punishmentId = contract.getPunishment();
        if (punishmentId == null || this.minecraft.level == null) {
            return Component.empty();
        }

        return this.minecraft.level.registryAccess().registryOrThrow(CraftorioEffects.REGISTRY_KEY).getOptional(punishmentId)
                .<Component>map(effect -> Component.translatable("misc.craftorio.contract_punishment_line", effect.getActualName())
                        .withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true)))
                .orElse(Component.empty());
    }

    private void renderAbandonButton(GuiGraphics graphics, CraftorioShipmentContract contract, int centerX, int y, int mouseX, int mouseY) {
        int left = centerX - ABANDON_BUTTON_WIDTH / 2;
        boolean hovered = mouseX >= left && mouseX <= left + ABANDON_BUTTON_WIDTH
                && mouseY >= y && mouseY <= y + ABANDON_BUTTON_HEIGHT
                && mouseX >= this.viewportLeft && mouseX <= this.viewportRight;

        int background = hovered ? 0xC0AA3333 : 0xA0552222;
        graphics.fill(left, y, left + ABANDON_BUTTON_WIDTH, y + ABANDON_BUTTON_HEIGHT, background);
        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.abandon_contract_button"), centerX, y + 3, 0xFFFFFF);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int trackY = this.viewportBottom + SCROLLBAR_GAP;
        int trackWidth = this.viewportRight - this.viewportLeft;
        graphics.fill(this.viewportLeft, trackY, this.viewportRight, trackY + SCROLLBAR_HEIGHT, 0x40FFFFFF);

        int contentWidth = trackWidth + this.maxScroll;
        int barWidth = Mth.clamp(trackWidth * trackWidth / contentWidth, 20, trackWidth);
        int barX = this.viewportLeft + Math.round(this.scrollX / this.maxScroll * (trackWidth - barWidth));
        graphics.fill(barX, trackY, barX + barWidth, trackY + SCROLLBAR_HEIGHT, 0xA0FFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0 && mouseX >= this.viewportLeft && mouseX <= this.viewportRight
                && mouseY >= this.viewportTop && mouseY <= this.viewportBottom) {
            this.dragging = true;
            this.hasDragged = false;
            this.dragStartX = mouseX;
            this.dragStartScroll = this.scrollX;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging) {
            double delta = mouseX - this.dragStartX;
            if (Math.abs(delta) > DRAG_THRESHOLD) {
                this.hasDragged = true;
            }
            this.scrollX = Mth.clamp(this.dragStartScroll - (float) delta, 0, this.maxScroll);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.dragging = false;
            if (!this.hasDragged) {
                clickAt(mouseX, mouseY);
            }
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        double delta = scrollX != 0 ? scrollX : scrollY;
        this.scrollX = Mth.clamp(this.scrollX - (float) delta * SCROLL_SPEED, 0, this.maxScroll);
        return true;
    }

    private void clickAt(double mouseX, double mouseY) {
        int top = this.cardCenterY - this.cardHeight / 2;
        int abandonY = top + this.cardHeight + ABANDON_BUTTON_GAP;
        int x = this.viewportLeft - Math.round(this.scrollX);

        for (int i = 0; i < this.contracts.size(); i++) {
            CraftorioShipmentContract contract = this.contracts.get(i);
            int centerX = x + this.cardWidth / 2;

            if (!contract.isAbandoned() && mouseX >= centerX - ABANDON_BUTTON_WIDTH / 2.0 && mouseX <= centerX + ABANDON_BUTTON_WIDTH / 2.0
                    && mouseY >= abandonY && mouseY <= abandonY + ABANDON_BUTTON_HEIGHT) {
                promptAbandon(i, contract);
                return;
            }

            if (mouseX >= x && mouseX <= x + this.cardWidth && mouseY >= top && mouseY <= top + this.cardHeight) {
                this.minecraft.setScreen(new ContractDetailsScreen(this, contract));
                return;
            }

            x += this.cardWidth + CARD_GAP;
        }
    }

    private void promptAbandon(int index, CraftorioShipmentContract contract) {
        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                PacketDistributor.sendToServer(new AbandonContractPacket(index));
                contract.setAbandoned(true);
                contract.setTime(0);
            }
            this.minecraft.setScreen(this);
        }, Component.translatable("misc.craftorio.abandon_contract_title"),
                Component.translatable("misc.craftorio.abandon_contract_message", contract.getName())));
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

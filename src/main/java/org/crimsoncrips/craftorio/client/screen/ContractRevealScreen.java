package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.ClaimContractPacket;
import org.crimsoncrips.craftorio.networking.ForceContractRefreshPacket;
import org.crimsoncrips.craftorio.networking.RefreshContractOfferPacket;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContractRevealScreen extends Screen {

    private static final ResourceLocation CARD_TEXTURE = Craftorio.getGuiTexture("contract.png");
    private static final int BASE_CARD_WIDTH = 150;
    private static final int BASE_CARD_HEIGHT = 180;
    private static final int MIN_CARD_HEIGHT = 80;
    private static final int CARD_GAP = 30;
    private static final int SIDE_MARGIN = 20;
    private static final int TOP_MARGIN = 40;
    private static final int DONE_BUTTON_AREA = 40;

    private static final long SPIN_DURATION_MS = 1400L;
    private static final int SPIN_TURNS = 3;
    private static final long DROP_WINDOW_MS = 300L;
    private static final long REVEAL_DELAY_MS = 500L;
    private static final long GROW_DURATION_MS = 200L;
    private static final long CARD_STAGGER_MS = 150L;

    private static final float HOVER_SCALE = 1.15f;
    private static final float HOVER_LERP = 0.2f;
    private static final float BOB_AMPLITUDE = 4.0f;
    private static final long BOB_PERIOD_MS = 2200L;

    private static final int SHADOW_OFFSET = 6;
    private static final int SHADOW_ALPHA = 0x80;

    private static final float TEXT_SCALE = 0.8f;
    private static final int ICON_SIZE = 32;
    private static final int ICON_GAP = 6;
    private static final int CLAIM_BUTTON_WIDTH = 60;
    private static final int CLAIM_BUTTON_HEIGHT = 16;
    private static final int CLAIM_BUTTON_GAP = 8;

    private List<ResourceLocation> contractIds;
    private final List<OfferedCard> cards = new ArrayList<>();

    private long startMillis = -1L;
    private int cardWidth = BASE_CARD_WIDTH;
    private int cardHeight = BASE_CARD_HEIGHT;

    private int initialTicksUntilRefresh;
    private long refreshTimerStartMillis = -1L;

    private Button refreshButton;

    public ContractRevealScreen(List<ResourceLocation> contractIds, int ticksUntilRefresh) {
        super(Component.translatable("misc.craftorio.contract_reveal_title"));
        this.contractIds = contractIds;
        this.initialTicksUntilRefresh = ticksUntilRefresh;
    }

    @Override
    protected void init() {
        this.cards.clear();
        boolean firstInit = this.startMillis < 0L;
        if (firstInit) {
            this.startMillis = System.currentTimeMillis();
            this.refreshTimerStartMillis = System.currentTimeMillis();

            if (Craftorio.CLIENT_CONFIG.SKIP_CONTRACT_CLAIM_ANIMATION.get()) {
                skipIntro();
            }
        }

        int centerX = this.width / 2;
        int count = Math.max(1, contractIds.size());
        float aspect = (float) BASE_CARD_WIDTH / BASE_CARD_HEIGHT;

        float hoverOverflow = (HOVER_SCALE - 1f) / 2f;
        int reservedBottom = DONE_BUTTON_AREA + CLAIM_BUTTON_GAP + CLAIM_BUTTON_HEIGHT + 20;
        int maxAreaHeight = Math.max(MIN_CARD_HEIGHT, (int) ((this.height - TOP_MARGIN - reservedBottom) / (1f + hoverOverflow)));
        int maxAreaWidth = Math.max(60, (int) ((this.width - SIDE_MARGIN * 2) / (1f + hoverOverflow)));

        int heightLimitedHeight = Math.min(BASE_CARD_HEIGHT, maxAreaHeight);
        int heightLimitedWidth = Math.round(heightLimitedHeight * aspect);

        int widthLimitedWidth = Math.min(BASE_CARD_WIDTH, (maxAreaWidth - (count - 1) * CARD_GAP) / count);
        int widthLimitedHeight = Math.round(widthLimitedWidth / aspect);

        if (heightLimitedWidth <= widthLimitedWidth) {
            this.cardWidth = heightLimitedWidth;
            this.cardHeight = heightLimitedHeight;
        } else {
            this.cardWidth = widthLimitedWidth;
            this.cardHeight = widthLimitedHeight;
        }

        int cardsAreaTop = TOP_MARGIN;
        int cardsAreaBottom = this.height - reservedBottom;
        int restY = (cardsAreaTop + cardsAreaBottom) / 2;

        int totalWidth = count * this.cardWidth + Math.max(0, count - 1) * CARD_GAP;
        int startX = centerX - totalWidth / 2 + this.cardWidth / 2;

        Player player = this.minecraft.player;
        for (int i = 0; i < contractIds.size(); i++) {
            ResourceLocation id = contractIds.get(i);
            if (player == null) continue;

            final int index = i;
            CraftorioMisc.getContractTemplate(player.level(), id).ifPresent(template -> {
                int restX = startX + index * (this.cardWidth + CARD_GAP);
                OfferedCard card = new OfferedCard(id, template, index * CARD_STAGGER_MS, restX, restY);

                Button claimButton = Button.builder(Component.translatable("misc.craftorio.claim_contract_button"), b -> {
                    PacketDistributor.sendToServer(new ClaimContractPacket(card.id));
                    card.claimed = true;
                    this.onClose();
                }).bounds(restX - CLAIM_BUTTON_WIDTH / 2, claimButtonY((float) restY), CLAIM_BUTTON_WIDTH, CLAIM_BUTTON_HEIGHT).build();
                claimButton.visible = false;
                card.claimButton = claimButton;
                this.addRenderableWidget(claimButton);

                this.cards.add(card);
            });
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - 50, this.height - 30, 100, 20).build());

        this.refreshButton = Button.builder(Component.translatable("misc.craftorio.refresh_contracts_button"),
                        b -> PacketDistributor.sendToServer(new RefreshContractOfferPacket()))
                .bounds(centerX - 60, 32, 120, 16).build();
        updateRefreshTooltip();
        this.addRenderableWidget(this.refreshButton);

        if (player != null && player.isCreative()) {
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.force_contract_refresh_button"),
                            b -> PacketDistributor.sendToServer(new ForceContractRefreshPacket()))
                    .bounds(this.width - 130, 6, 120, 16).build());
        }
    }

    public void updateOffer(List<ResourceLocation> contractIds, int ticksUntilRefresh) {
        this.contractIds = contractIds;
        this.initialTicksUntilRefresh = ticksUntilRefresh;
        this.refreshTimerStartMillis = System.currentTimeMillis();
        this.rebuildWidgets();
    }

    private int claimButtonY(float centerY) {
        return (int) (centerY + this.cardHeight / 2f + CLAIM_BUTTON_GAP);
    }

    public void skipIntro() {
        long longestEntrance = SPIN_DURATION_MS + REVEAL_DELAY_MS + GROW_DURATION_MS
                + Math.max(0, contractIds.size() - 1) * CARD_STAGGER_MS;
        this.startMillis = Math.min(this.startMillis, System.currentTimeMillis() - longestEntrance - 1000L);
    }

    private long elapsedSince(long delay) {
        return Math.max(0, System.currentTimeMillis() - startMillis - delay);
    }

    private int ticksUntilRefresh() {
        long elapsedTicks = (System.currentTimeMillis() - this.refreshTimerStartMillis) / 50L;
        return (int) Math.max(0, this.initialTicksUntilRefresh - elapsedTicks);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private void updateRefreshTooltip() {
        if (this.refreshButton == null || this.minecraft.player == null) return;

        BigInteger cost = CraftorioMisc.contractRefreshCost(this.minecraft.player);
        String costText = CraftorioMisc.bigIntFormat(cost, Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());

        this.refreshButton.setTooltip(Tooltip.create(Component.translatable("misc.craftorio.refresh_contracts_tooltip", costText)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);

        updateRefreshTooltip();

        graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.choose_a_contract"), this.width / 2, 8, 0xFFFFFF);

        String refreshLine = Component.translatable("misc.craftorio.contract_refresh_timer", CraftorioMisc.ticksToTimeString(ticksUntilRefresh())).getString();
        graphics.drawCenteredString(this.font, refreshLine, this.width / 2, 20, 0xAAAAAA);

        if (this.cards.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.no_contracts"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else {
            for (OfferedCard card : this.cards) {
                renderCard(graphics, card, mouseX, mouseY);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCard(GuiGraphics graphics, OfferedCard card, int mouseX, int mouseY) {
        long elapsed = elapsedSince(card.startDelay);

        float spinT = Mth.clamp(elapsed / (float) SPIN_DURATION_MS, 0f, 1f);
        float eased = easeOutCubic(spinT);
        float angleDeg = 90f + (360f * SPIN_TURNS - 90f) * eased;
        float scaleX = Math.abs((float) Math.cos(Math.toRadians(angleDeg)));
        float pulse = 1.0f + 0.15f * (float) Math.sin(Math.PI * spinT);

        float dropT = Mth.clamp((elapsed - (SPIN_DURATION_MS - DROP_WINDOW_MS)) / (float) DROP_WINDOW_MS, 0f, 1f);
        float dropOffset = -20f * (1f - easeOutCubic(dropT));

        boolean landed = spinT >= 1f;
        float bobOffset = 0f;
        if (landed) {
            long sinceLand = elapsed - SPIN_DURATION_MS;
            bobOffset = BOB_AMPLITUDE * (float) Math.sin(sinceLand / (float) BOB_PERIOD_MS * Math.PI * 2);
        }

        boolean hovered = landed && mouseX >= card.restX - this.cardWidth / 2.0 && mouseX <= card.restX + this.cardWidth / 2.0
                && mouseY >= card.restY - this.cardHeight / 2.0 && mouseY <= card.restY + this.cardHeight / 2.0;
        card.hoverScale = card.hoverScale + (((hovered ? HOVER_SCALE : 1.0f) - card.hoverScale) * HOVER_LERP);

        float finalScale = scaleX * pulse * card.hoverScale;
        float centerY = card.restY + dropOffset + bobOffset;
        boolean locked = !meetsThreshold(card.contract);

        graphics.pose().pushPose();
        graphics.pose().translate(card.restX + SHADOW_OFFSET, centerY + SHADOW_OFFSET, 0);
        graphics.pose().scale(finalScale, pulse * card.hoverScale, 1f);
        graphics.setColor(0f, 0f, 0f, SHADOW_ALPHA / 255f);
        graphics.blit(CARD_TEXTURE, -this.cardWidth / 2, -this.cardHeight / 2, 0, 0, this.cardWidth, this.cardHeight, this.cardWidth, this.cardHeight);
        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(card.restX, centerY, 0);
        graphics.pose().scale(finalScale, pulse * card.hoverScale, 1f);
        if (locked) {
            graphics.setColor(0.6f, 0.6f, 0.6f, 0.55f);
        }
        graphics.blit(CARD_TEXTURE, -this.cardWidth / 2, -this.cardHeight / 2, 0, 0, this.cardWidth, this.cardHeight, this.cardWidth, this.cardHeight);
        if (locked) {
            graphics.setColor(1f, 1f, 1f, 1f);
        }
        graphics.pose().popPose();

        boolean revealed = elapsed >= SPIN_DURATION_MS + REVEAL_DELAY_MS;
        if (card.claimButton != null) {
            card.claimButton.visible = revealed && !locked;
        }

        if (revealed) {
            renderDetails(graphics, card, elapsed, centerY, locked);
        }

        if (revealed && locked) {
            renderLockedMessage(graphics, card, elapsed, centerY);
        }
    }

    private boolean meetsThreshold(CraftorioShipmentContract contract) {
        Player player = this.minecraft.player;
        if (player == null) return true;
        return CraftorioMisc.getHighestPoints(player).compareTo(contract.getPointThreshold()) >= 0;
    }

    private void renderDetails(GuiGraphics graphics, OfferedCard card, long elapsed, float centerY, boolean locked) {
        long sinceReveal = elapsed - (SPIN_DURATION_MS + REVEAL_DELAY_MS);
        float growT = Mth.clamp(sinceReveal / (float) GROW_DURATION_MS, 0f, 1f);
        float growScale = easeOutBack(growT);
        if (growScale <= 0f) return;

        float textScale = TEXT_SCALE * this.cardWidth / BASE_CARD_WIDTH * card.hoverScale;
        int innerWidth = this.cardWidth - 20;
        int wrapWidth = (int) (innerWidth / textScale);
        float textTop = centerY - this.cardHeight / 2f + 14;

        int nameColor = locked ? 0x707070 : 0xFFFFFF;
        int timeColor = locked ? 0x555555 : 0xAAAAAA;
        int descColor = locked ? 0x707070 : 0xFFFFFF;
        int punishmentColor = locked ? 0x707070 : 0xFFFFFF;

        graphics.pose().pushPose();
        graphics.pose().translate(card.restX, textTop, 0);
        graphics.pose().scale(growScale * textScale, growScale * textScale, 1f);

        float y = 0;
        if (card.contract.getIcon() != null) {
            if (locked) {
                graphics.setColor(0.5f, 0.5f, 0.5f, 0.7f);
            }
            graphics.blit(card.contract.getIcon(), -ICON_SIZE / 2, 0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            if (locked) {
                graphics.setColor(1f, 1f, 1f, 1f);
            }
            y = ICON_SIZE + ICON_GAP;
        }
        for (var line : this.font.split(Component.literal(card.contract.getActualName()), wrapWidth)) {
            graphics.drawString(this.font, line, -this.font.width(line) / 2, (int) y, nameColor, true);
            y += this.font.lineHeight;
        }

        String timeLine = Component.translatable("misc.craftorio.contract_time_remaining", CraftorioMisc.ticksToTimeString(card.contract.getTime())).getString();
        graphics.drawString(this.font, timeLine, -this.font.width(timeLine) / 2, (int) y, timeColor, true);
        y += this.font.lineHeight + 3;

        for (var line : this.font.split(Component.literal(card.contract.getActualDescription()), wrapWidth)) {
            graphics.drawString(this.font, line, -this.font.width(line) / 2, (int) y, descColor, true);
            y += this.font.lineHeight;
        }

        Component punishmentLine = punishmentLine(card.contract);
        if (!punishmentLine.getString().isEmpty()) {
            y += 3;
            for (var line : this.font.split(punishmentLine, wrapWidth)) {
                graphics.drawString(this.font, line, -this.font.width(line) / 2, (int) y, punishmentColor, true);
                y += this.font.lineHeight;
            }
        }

        graphics.pose().popPose();
    }

    private void renderLockedMessage(GuiGraphics graphics, OfferedCard card, long elapsed, float centerY) {
        long sinceReveal = elapsed - (SPIN_DURATION_MS + REVEAL_DELAY_MS);
        float growT = Mth.clamp(sinceReveal / (float) GROW_DURATION_MS, 0f, 1f);
        float growScale = easeOutBack(growT);
        if (growScale <= 0f) return;

        float textScale = TEXT_SCALE * this.cardWidth / BASE_CARD_WIDTH * card.hoverScale;
        int innerWidth = this.cardWidth - 20;
        int wrapWidth = (int) (innerWidth / textScale);

        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.addAll(this.font.split(Component.translatable("misc.craftorio.contract_threshold_not_met"), wrapWidth));
        lines.addAll(this.font.split(Component.translatable("misc.craftorio.contract_threshold_required",
                CraftorioMisc.bigIntFormat(card.contract.getPointThreshold(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt())), wrapWidth));

        graphics.pose().pushPose();
        graphics.pose().translate(card.restX, centerY, 0);
        graphics.pose().scale(growScale * textScale, growScale * textScale, 1f);

        float y = -(lines.size() * this.font.lineHeight) / 2f;
        for (var line : lines) {
            graphics.drawString(this.font, line, -this.font.width(line) / 2, (int) y, 0xFFFFFF, true);
            y += this.font.lineHeight;
        }

        graphics.pose().popPose();
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        for (OfferedCard card : this.cards) {
            long elapsed = elapsedSince(card.startDelay);
            boolean revealed = elapsed >= SPIN_DURATION_MS + REVEAL_DELAY_MS;
            if (!revealed || !meetsThreshold(card.contract)) continue;

            if (mouseX >= card.restX - this.cardWidth / 2.0 && mouseX <= card.restX + this.cardWidth / 2.0
                    && mouseY >= card.restY - this.cardHeight / 2.0 && mouseY <= card.restY + this.cardHeight / 2.0) {
                this.minecraft.setScreen(new ContractDetailsScreen(this, card.contract));
                return true;
            }
        }

        return false;
    }

    private static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float t1 = t - 1;
        return 1 + c3 * t1 * t1 * t1 + c1 * t1 * t1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class OfferedCard {
        final ResourceLocation id;
        final CraftorioShipmentContract contract;
        final long startDelay;
        final int restX;
        final int restY;

        boolean claimed;
        float hoverScale = 1.0f;
        Button claimButton;

        OfferedCard(ResourceLocation id, CraftorioShipmentContract contract, long startDelay, int restX, int restY) {
            this.id = id;
            this.contract = contract;
            this.startDelay = startDelay;
            this.restX = restX;
            this.restY = restY;
        }
    }
}

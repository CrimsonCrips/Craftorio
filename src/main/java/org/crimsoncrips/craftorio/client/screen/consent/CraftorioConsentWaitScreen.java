package org.crimsoncrips.craftorio.client.screen.consent;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.networking.consent.ConsentStatusPacket;
import org.crimsoncrips.craftorio.networking.consent.VoteConsentPacket;
import org.crimsoncrips.craftorio.networking.consent.WithdrawConsentPacket;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioConsentWaitScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 240;
    private static final int PADDING = 12;
    private static final int LINE_HEIGHT = 12;
    private static final int TITLE_HEIGHT = 40;
    private static final long URGENT_MS = 10_000L;
    private static final int PROGRESS_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 40;
    private static final long DOT_PERIOD_MS = 400L;

    private final ConsentKind kind;
    private boolean closedByServer = false;
    private boolean votedYes;
    private int panelLeft;
    private int panelTop;
    private int panelHeight;
    private int layoutKey = -1;

    public CraftorioConsentWaitScreen(ConsentKind kind, boolean alreadyAgreed) {
        super(Component.translatable(kind == ConsentKind.REBIRTH ? "misc.craftorio.consent_wait_title_rebirth" : "misc.craftorio.consent_wait_title_sacrifice"));
        this.kind = kind;
        this.votedYes = alreadyAgreed;
    }

    public ConsentKind kind() {
        return this.kind;
    }

    public void closeFromServer() {
        this.closedByServer = true;
        this.minecraft.setScreen(null);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    private ConsentStatusPacket status() {
        return ClientConsentState.status(this.kind);
    }

    private List<String> requiredNames() {
        ConsentStatusPacket status = status();
        return status == null ? List.of() : status.required();
    }

    private List<String> agreedNames() {
        ConsentStatusPacket status = status();
        return status == null ? List.of() : status.agreed();
    }

    private List<Component> details() {
        ConsentStatusPacket status = status();
        return status == null ? List.of() : status.details();
    }

    private boolean agreed() {
        if (this.votedYes) return true;
        String self = this.minecraft.player != null ? this.minecraft.player.getGameProfile().getName() : "";
        return agreedNames().contains(self);
    }

    private int computeLayoutKey() {
        return (requiredNames().size() * 31 + details().size()) * 2 + (agreed() ? 1 : 0);
    }

    @Override
    protected void init() {
        this.layoutKey = computeLayoutKey();
        int detailLines = details().size();
        this.panelHeight = TITLE_HEIGHT + (detailLines > 0 ? detailLines * LINE_HEIGHT + 6 : 0) + PROGRESS_HEIGHT
                + Math.max(1, requiredNames().size()) * LINE_HEIGHT + FOOTER_HEIGHT;
        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        int buttonY = this.panelTop + this.panelHeight - PADDING - 20;
        int innerWidth = PANEL_WIDTH - PADDING * 2;
        if (agreed()) {
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.consent_withdraw"), b -> this.onClose())
                    .bounds(this.panelLeft + PADDING, buttonY, innerWidth, 20).build());
        } else {
            int half = (innerWidth - 6) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.consent_vote_yes").withStyle(ChatFormatting.GREEN), b -> {
                this.votedYes = true;
                PacketDistributor.sendToServer(new VoteConsentPacket(this.kind, true));
                this.rebuildWidgets();
            }).bounds(this.panelLeft + PADDING, buttonY, half, 20).build());
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.consent_vote_no").withStyle(ChatFormatting.RED), b -> {
                PacketDistributor.sendToServer(new VoteConsentPacket(this.kind, false));
                this.closedByServer = true;
                this.onClose();
            }).bounds(this.panelLeft + PADDING + half + 6, buttonY, half, 20).build());
        }
    }

    @Override
    public void tick() {
        if (computeLayoutKey() != this.layoutKey) {
            this.rebuildWidgets();
        }
    }

    @Override
    public void removed() {
        if (!this.closedByServer && agreed()) {
            PacketDistributor.sendToServer(new WithdrawConsentPacket(this.kind));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + this.panelHeight, 0xE0202020);
        guiGraphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, this.panelHeight, 0xFF808080);

        int centerX = this.panelLeft + PANEL_WIDTH / 2;
        guiGraphics.drawCenteredString(this.font, this.title, centerX, this.panelTop + 10, this.kind == ConsentKind.SACRIFICE ? 0xFF5555 : 0xFFFFFF);
        long remaining = ClientConsentState.remainingMs(this.kind);
        if (remaining >= 0L) {
            long seconds = (remaining + 999L) / 1000L;
            Component countdown = Component.translatable("misc.craftorio.consent_time_left", String.format("%d:%02d", seconds / 60, seconds % 60));
            guiGraphics.drawCenteredString(this.font, countdown, centerX, this.panelTop + 22, remaining <= URGENT_MS ? 0xFF5555 : 0xFFFFFF);
        }

        int y = this.panelTop + TITLE_HEIGHT;
        List<Component> details = details();
        for (Component detail : details) {
            guiGraphics.drawCenteredString(this.font, detail, centerX, y, 0xAAAAAA);
            y += LINE_HEIGHT;
        }
        if (!details.isEmpty()) y += 6;

        List<String> required = requiredNames();
        List<String> agreed = agreedNames();
        guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.consent_wait_progress", agreed.size(), required.size()), centerX, y, 0xFFFF55);
        y += 14;
        guiGraphics.drawCenteredString(this.font, Component.translatable(agreed() ? "misc.craftorio.consent_wait_hint" : "misc.craftorio.consent_vote_hint").withStyle(ChatFormatting.ITALIC), centerX, y, 0x888888);
        y += PROGRESS_HEIGHT - 14;

        ConsentStatusPacket status = status();
        String proposer = status == null ? "" : status.proposer();
        String dots = ".".repeat((int) (Util.getMillis() / DOT_PERIOD_MS % 3) + 1);
        for (String name : required) {
            boolean ready = agreed.contains(name);
            String label = name.equals(proposer) ? name + " " + Component.translatable("misc.craftorio.consent_proposer").getString() : name;
            Component line = ready
                    ? Component.literal("✔ " + label).withStyle(ChatFormatting.GREEN)
                    : Component.literal(label + " " + dots).withStyle(ChatFormatting.GRAY);
            guiGraphics.drawString(this.font, line, this.panelLeft + PADDING, y, 0xFFFFFF, false);
            y += LINE_HEIGHT;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

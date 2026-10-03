package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsScreen;
import org.crimsoncrips.craftorio.client.screen.effect.ActiveEffectsScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.BorderExpandScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.ClaimItemPurchaseScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.EffectRuneShopScreen;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;
import org.crimsoncrips.craftorio.server.skill_tree.SkillTreeReveal;
import org.crimsoncrips.craftorio.networking.skill_tree.RevealSkillTreePacket;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioBasicSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioRebirthSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.consent.ClientConsentState;
import org.crimsoncrips.craftorio.client.screen.consent.CraftorioConsentWaitScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioSacrificeSkillTreeScreen;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.client.screen.widget.LoanSharkButton;
import org.crimsoncrips.craftorio.client.screen.widget.AssemblingButton;
import org.crimsoncrips.craftorio.client.screen.widget.SheetIconButton;
import org.crimsoncrips.craftorio.client.state.ClientContractOfferState;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.networking.contract.RequestContractOfferPacket;
import org.crimsoncrips.craftorio.networking.sacrifice.RequestSacrificePacket;
import org.crimsoncrips.craftorio.networking.sacrifice.SacrificeIntroStartedPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenValueBrowserPacket;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.shop.CraftorioEffectRuneShop;
import org.crimsoncrips.craftorio.server.shop.CraftorioShopMode;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioHubScreen extends Screen implements ScrollableScreen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;
    private static final int REGULAR_BUTTON_COUNT = 9;
    private static final int DONE_EXTRA_GAP = 16;
    private static final int LIFT_OFFSET = 20;

    private static final int SACRIFICE_REVEAL_STEPS = 10;
    private static final float SACRIFICE_SIZE_SCALE = 1.7f;
    private static final int SACRIFICE_WIDTH = Math.round(BUTTON_WIDTH * SACRIFICE_SIZE_SCALE);
    private static final int SACRIFICE_HEIGHT = Math.round(BUTTON_HEIGHT * SACRIFICE_SIZE_SCALE);
    private static final int SACRIFICE_SCREEN_MARGIN = 8;
    private static final int SACRIFICE_SLOT_SHIFT = SACRIFICE_HEIGHT + (BUTTON_STRIDE - BUTTON_HEIGHT) + DONE_EXTRA_GAP;
    private static final long INTRO_SHIFT_MS = 1400L;
    private static final long INTRO_SHARDS_MS = 1800L;
    private static final long INTRO_HOLD_MS = 500L;
    private static final long INTRO_TOTAL_MS = INTRO_SHIFT_MS + INTRO_SHARDS_MS + INTRO_HOLD_MS;
    private static final long SKILL_TREE_PANEL_ANIM_MS = 250L;
    private static final int SKILL_TREE_PANEL_WIDTH = 150;
    private static final int SKILL_TREE_PANEL_PADDING = 6;
    private static final int SKILL_TREE_PANEL_TITLE_HEIGHT = 12;
    private static final int SKILL_TREE_PANEL_MARGIN = 8;

    private static final long TREE_REVEAL_DELAY_MS = 150L;
    private static final long TREE_REVEAL_MS = 450L;
    private static final long TREE_REVEAL_STAGGER_MS = 200L;

    private final List<Button> skillTreeButtons = new ArrayList<>();
    private final List<Button> revealingTreeButtons = new ArrayList<>();
    private final List<UpgradeTree> revealingTrees = new ArrayList<>();
    private long treeRevealStartMillis = -1L;
    private boolean treeRevealSent;
    private boolean skillTreeAnimOpening;
    private long skillTreeAnimStartMillis;
    private float skillTreeAnimFrom;
    private int skillTreePanelTop;
    private int skillTreePanelHeight;

    private final List<AbstractWidget> columnWidgets = new ArrayList<>();
    private final List<Integer> columnBaseY = new ArrayList<>();
    private boolean introPlaying;
    private boolean introStarted;
    private long introStartMillis;

    private Button loanSharkButton;
    private boolean effectRuneShopShown;
    private AssemblingButton sacrificeButton;

    public CraftorioHubScreen() {
        super(Component.translatable("misc.craftorio.hub_title"));
    }

    @Override
    protected void init() {
        if (this.minecraft.level != null && CraftorioMisc.isInHavenDimension(this.minecraft.level)) {
            this.minecraft.setScreen(null);
            return;
        }

        int centerX = this.width / 2;
        this.effectRuneShopShown = effectRuneShopUnlocked();
        boolean shopShown = Craftorio.SERVER_CONFIG.SHOP_MODE.get() != CraftorioShopMode.DISABLED;
        int regularButtonCount = REGULAR_BUTTON_COUNT - (this.effectRuneShopShown ? 0 : 1) - (shopShown ? 0 : 1);
        int blockHeight = (regularButtonCount - 1) * BUTTON_STRIDE + BUTTON_HEIGHT + DONE_EXTRA_GAP + SACRIFICE_SLOT_SHIFT;
        int y = (this.height - blockHeight) / 2 - LIFT_OFFSET;

        boolean chunkBased = this.minecraft.level != null && CraftorioMisc.chunkBased(this.minecraft.level);

        if (this.sacrificeButton != null) {
            this.sacrificeButton.release();
        }

        int sacrificeWidth = Math.min(SACRIFICE_WIDTH, this.width - SACRIFICE_SCREEN_MARGIN * 2);
        this.sacrificeButton = (AssemblingButton) Button.builder(Component.literal("Sacrifice"), b -> {
                    PacketDistributor.sendToServer(new RequestSacrificePacket());
                    this.onClose();
                })
                .bounds(centerX - sacrificeWidth / 2, y, sacrificeWidth, SACRIFICE_HEIGHT).build(builder -> new AssemblingButton(builder, 1.6f * SACRIFICE_SIZE_SCALE));
        this.addRenderableWidget(this.sacrificeButton);
        updateSacrificeButton();
        y += SACRIFICE_SLOT_SHIFT;


        if (shopShown) {
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.hub_shop"), b -> PacketDistributor.sendToServer(new RequestOpenShopPacket()))
                    .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_STRIDE;
        }
        if (chunkBased) {
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.claim_shop_title"), b -> this.minecraft.setScreen(new ClaimItemPurchaseScreen()))
                    .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.expand_border"), b -> this.minecraft.setScreen(new BorderExpandScreen()))
                    .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.value_browser_title"), b -> PacketDistributor.sendToServer(new RequestOpenValueBrowserPacket()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        Component revealLabel = Component.translatable("misc.craftorio.reveal_contract_button");
        if (ClientContractOfferState.isAvailable()) {
            revealLabel = revealLabel.copy().withStyle(style -> style.withColor(ChatFormatting.YELLOW));
        }
        this.addRenderableWidget(Button.builder(revealLabel, b -> PacketDistributor.sendToServer(new RequestContractOfferPacket()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.active_effects_button"), b -> this.minecraft.setScreen(new ActiveEffectsScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        if (this.effectRuneShopShown) {
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.effect_rune_shop_button"), b -> this.minecraft.setScreen(new EffectRuneShopScreen()))
                    .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_STRIDE;
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.rebirth_button"), b -> this.minecraft.setScreen(ClientConsentState.status(ConsentKind.REBIRTH) != null ? new CraftorioConsentWaitScreen(ConsentKind.REBIRTH, false) : new CraftorioRebirthConfirmScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.loanSharkButton = Button.builder(Component.empty(), b -> this.minecraft.setScreen(new CraftorioLoanSharkScreen(this)))
                .bounds(8, 8, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("misc.craftorio.loan_shark_button_tooltip")))
                .build(LoanSharkButton::new);
        this.addRenderableWidget(this.loanSharkButton);
        updateLoanSharkButton();

        int statsSize = 20;
        int statsX = 8;
        int statsY = this.height - statsSize - 8;
        this.addRenderableWidget(Button.builder(Component.empty(), b -> this.minecraft.setScreen(new CraftorioStatisticsScreen()))
                .bounds(statsX, statsY, statsSize, statsSize)
                .tooltip(Tooltip.create(Component.translatable("misc.craftorio.stats_button")))
                .build(builder -> new SheetIconButton(builder, ClientEvents.STATISTICS_ICON_U, ClientEvents.STATISTICS_ICON_V)));

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.skill_trees_button"), b -> toggleSkillTreePanel())
                .bounds(statsX + statsSize + 4, statsY, 90, statsSize).build());

        this.skillTreeButtons.clear();
        this.revealingTreeButtons.clear();
        this.revealingTrees.clear();
        this.skillTreeButtons.add(Button.builder(Component.translatable("misc.craftorio.skill_tree_title"),
                b -> this.minecraft.setScreen(new CraftorioBasicSkillTreeScreen("misc.craftorio.skill_tree_title"))).build());
        addTreeButton(UpgradeTree.REBIRTH, Button.builder(Component.translatable("misc.craftorio.rebirth_skill_tree_title"),
                b -> this.minecraft.setScreen(new CraftorioRebirthSkillTreeScreen())).build());
        addTreeButton(UpgradeTree.SACRIFICE, Button.builder(Component.translatable("misc.craftorio.sacrifice_skill_tree_title"),
                b -> this.minecraft.setScreen(new CraftorioSacrificeSkillTreeScreen())).build());

        this.skillTreePanelHeight = SKILL_TREE_PANEL_PADDING * 2 + SKILL_TREE_PANEL_TITLE_HEIGHT
                + this.skillTreeButtons.size() * BUTTON_STRIDE - (BUTTON_STRIDE - BUTTON_HEIGHT);
        this.skillTreePanelTop = statsY - 4 - this.skillTreePanelHeight;
        for (int i = 0; i < this.skillTreeButtons.size(); i++) {
            Button button = this.skillTreeButtons.get(i);
            button.setWidth(SKILL_TREE_PANEL_WIDTH - SKILL_TREE_PANEL_PADDING * 2);
            button.setHeight(BUTTON_HEIGHT);
            button.setY(this.skillTreePanelTop + SKILL_TREE_PANEL_PADDING + SKILL_TREE_PANEL_TITLE_HEIGHT + i * BUTTON_STRIDE);
            this.addRenderableWidget(button);
        }
        layoutSkillTreePanel();

        if (this.minecraft.player != null && this.minecraft.player.isCreative()) {
            int devToolsWidth = 100;
            int devToolsY = this.height - BUTTON_HEIGHT - 8;
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_title"), b -> this.minecraft.setScreen(new DevToolsScreen(this)))
                    .bounds(this.width - devToolsWidth - 8, devToolsY, devToolsWidth, BUTTON_HEIGHT).build());
        }

        this.columnWidgets.clear();
        this.columnBaseY.clear();
        int columnX = centerX - BUTTON_WIDTH / 2;
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget widget && widget != this.sacrificeButton
                    && widget.getX() == columnX && widget.getWidth() == BUTTON_WIDTH) {
                this.columnWidgets.add(widget);
                this.columnBaseY.add(widget.getY());
            }
        }

        maybeStartIntro();
        layoutSacrificeSlot();
    }

    private void maybeStartIntro() {
        Player player = this.minecraft.player;
        if (this.introPlaying || this.introStarted || this.sacrificeButton == null || !this.sacrificeButton.visible
                || player == null || player.getData(CraftorioDataAttachments.SACRIFICE_INTRO_SEEN)) return;

        this.introPlaying = true;
        this.introStarted = true;
        this.introStartMillis = Util.getMillis();
        PacketDistributor.sendToServer(new SacrificeIntroStartedPacket());
    }

    private static float easeInOutCubic(float t) {
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    private long introElapsed() {
        return Util.getMillis() - this.introStartMillis;
    }

    private float introPhase(long startMs, long durationMs) {
        return Mth.clamp((introElapsed() - startMs) / (float) durationMs, 0f, 1f);
    }

    private void layoutSacrificeSlot() {
        if (this.sacrificeButton == null) return;

        float shift;
        float appear;
        if (this.introPlaying) {
            shift = easeInOutCubic(introPhase(0L, INTRO_SHIFT_MS));
            appear = introPhase(INTRO_SHIFT_MS, INTRO_SHARDS_MS);
        } else {
            shift = this.sacrificeButton.visible ? 1f : 0f;
            appear = 1f;
        }

        int offset = Math.round(-SACRIFICE_SLOT_SHIFT * (1f - shift));
        for (int i = 0; i < this.columnWidgets.size(); i++) {
            this.columnWidgets.get(i).setY(this.columnBaseY.get(i) + offset);
        }
        this.sacrificeButton.setAppearProgress(appear);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.introPlaying;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.introPlaying || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.introPlaying || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.introPlaying || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.introPlaying || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.introPlaying || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.introPlaying || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        if (this.sacrificeButton != null) {
            this.sacrificeButton.release();
        }
        super.removed();
    }

    private void updateSacrificeButton() {
        if (this.sacrificeButton == null) return;
        Player player = this.minecraft.player;
        int required = Craftorio.SERVER_CONFIG.SACRIFICE_REQUIRED_LIFE.getAsInt();
        float progress = player == null ? 0f : Math.min(1f, CraftorioMisc.getLife(player) / (float) required);
        float fraction = Mth.floor(progress * SACRIFICE_REVEAL_STEPS) / (float) SACRIFICE_REVEAL_STEPS;

        this.sacrificeButton.visible = fraction > 0f;
        this.sacrificeButton.setRevealFraction(fraction);
    }

    private void updateLoanSharkButton() {
        Player player = this.minecraft.player;
        boolean show = player != null && (CraftorioMisc.getPoints(player).signum() < 0 || CraftorioMisc.getLoanOwed(player).signum() > 0);
        this.loanSharkButton.visible = show;
        this.loanSharkButton.active = show;
    }

    private boolean effectRuneShopUnlocked() {
        Player player = this.minecraft.player;
        return player != null && CraftorioEffectRuneShop.isUnlocked(player);
    }

    @Override
    public void tick() {
        updateSacrificeButton();
        maybeStartIntro();
        updateLoanSharkButton();
        if (!this.introPlaying && effectRuneShopUnlocked() != this.effectRuneShopShown) {
            this.rebuildWidgets();
        }
    }

    private void addTreeButton(UpgradeTree tree, Button button) {
        Player player = this.minecraft.player;
        int state = player == null ? SkillTreeReveal.HIDDEN : SkillTreeReveal.state(player, tree);
        if (state == SkillTreeReveal.HIDDEN) return;

        this.skillTreeButtons.add(button);
        if (state == SkillTreeReveal.PENDING && !this.treeRevealSent) {
            this.revealingTreeButtons.add(button);
            this.revealingTrees.add(tree);
        }
    }

    private void layoutRevealingTrees(float progress, int left) {
        if (this.revealingTreeButtons.isEmpty()) return;

        long now = Util.getMillis();
        boolean done = true;
        if (progress >= 1f) {
            if (this.treeRevealStartMillis < 0L) {
                this.treeRevealStartMillis = now + TREE_REVEAL_DELAY_MS;
            }
        }

        for (int i = 0; i < this.revealingTreeButtons.size(); i++) {
            Button button = this.revealingTreeButtons.get(i);
            float t = this.treeRevealStartMillis < 0L ? 0f
                    : Mth.clamp((now - this.treeRevealStartMillis - i * TREE_REVEAL_STAGGER_MS) / (float) TREE_REVEAL_MS, 0f, 1f);
            float eased = 1f - (1f - t) * (1f - t) * (1f - t);
            button.setX(Math.round(Mth.lerp(eased, -button.getWidth() - 10f, left + SKILL_TREE_PANEL_PADDING)));
            button.visible = progress > 0f && t > 0f;
            button.active = progress >= 0.99f && t >= 1f;
            if (t < 1f) done = false;
        }

        if (!done && !this.skillTreeAnimOpening) {
            this.treeRevealStartMillis = -1L;
        }
        if (done && !this.treeRevealSent) {
            this.treeRevealSent = true;
            for (UpgradeTree tree : this.revealingTrees) {
                PacketDistributor.sendToServer(new RevealSkillTreePacket(tree));
            }
        }
    }

    private void toggleSkillTreePanel() {
        this.skillTreeAnimFrom = skillTreePanelProgress();
        this.skillTreeAnimOpening = !this.skillTreeAnimOpening;
        this.skillTreeAnimStartMillis = Util.getMillis();
    }

    private float skillTreePanelProgress() {
        float t = Mth.clamp((Util.getMillis() - this.skillTreeAnimStartMillis) / (float) SKILL_TREE_PANEL_ANIM_MS, 0f, 1f);
        float t1 = t - 1f;
        float eased = t1 * t1 * t1 + 1f;
        float target = this.skillTreeAnimOpening ? 1f : 0f;
        return this.skillTreeAnimFrom + (target - this.skillTreeAnimFrom) * eased;
    }

    private int skillTreePanelLeft(float progress) {
        return (int) Mth.lerp(progress, -SKILL_TREE_PANEL_WIDTH - 1, SKILL_TREE_PANEL_MARGIN);
    }

    private void layoutSkillTreePanel() {
        float progress = skillTreePanelProgress();
        int left = skillTreePanelLeft(progress);
        for (Button button : this.skillTreeButtons) {
            button.setX(left + SKILL_TREE_PANEL_PADDING);
            button.visible = progress > 0f;
            button.active = progress >= 0.99f;
        }
        layoutRevealingTrees(progress, left);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        float progress = skillTreePanelProgress();
        if (progress <= 0f) return;

        int left = skillTreePanelLeft(progress);
        int top = this.skillTreePanelTop;
        int right = left + SKILL_TREE_PANEL_WIDTH;
        int bottom = top + this.skillTreePanelHeight;
        guiGraphics.fill(left, top, right, bottom, 0xE0101010);
        guiGraphics.renderOutline(left, top, SKILL_TREE_PANEL_WIDTH, this.skillTreePanelHeight, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.skill_trees_title"),
                left + SKILL_TREE_PANEL_WIDTH / 2, top + SKILL_TREE_PANEL_PADDING + 1, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.introPlaying && introElapsed() >= INTRO_TOTAL_MS) {
            this.introPlaying = false;
        }
        layoutSkillTreePanel();
        layoutSacrificeSlot();

        int contentMouseX = this.introPlaying ? -1000 : mouseX;
        int contentMouseY = this.introPlaying ? -1000 : mouseY;
        super.render(guiGraphics, contentMouseX, contentMouseY, partialTick);
        if (this.sacrificeButton != null && this.sacrificeButton.visible) {
            this.sacrificeButton.renderAura(guiGraphics);
        }
        guiGraphics.flush();

        Player player = this.minecraft.player;
        if (player == null || this.minecraft.level == null) return;

        int indicatorX = this.width - ClientEvents.BORDER_MODE_INDICATOR_SIZE - 8;
        ClientEvents.drawBorderModeIndicators(guiGraphics, this.font, indicatorX, 8, mouseX, mouseY);

        ClientEvents.drawMaxPointsAtMeterPosition(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

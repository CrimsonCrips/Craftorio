package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
import org.crimsoncrips.craftorio.client.screen.contract.OwnedContractsScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsScreen;
import org.crimsoncrips.craftorio.client.screen.effect.ActiveEffectsScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.BorderExpandScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.ClaimItemPurchaseScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.EffectRuneShopScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioBasicSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioRebirthSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioSacrificeSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.widget.LoanSharkButton;
import org.crimsoncrips.craftorio.client.screen.widget.AssemblingButton;
import org.crimsoncrips.craftorio.client.screen.widget.SheetIconButton;
import org.crimsoncrips.craftorio.client.state.ClientContractOfferState;
import org.crimsoncrips.craftorio.client.state.ClientShopState;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.networking.contract.RequestContractOfferPacket;
import org.crimsoncrips.craftorio.networking.sacrifice.RequestSacrificePacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenValueBrowserPacket;
import org.crimsoncrips.craftorio.server.shop.CraftorioEffectRuneShop;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class CraftorioHubScreen extends Screen implements ScrollableScreen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;
    private static final int REGULAR_BUTTON_COUNT = 9;
    private static final int DONE_EXTRA_GAP = 16;
    private static final int LIFT_OFFSET = 20;

    private static final int SACRIFICE_REVEAL_STEPS = 10;
    private static final long SKILL_TREE_PANEL_ANIM_MS = 250L;
    private static final int SKILL_TREE_PANEL_WIDTH = 150;
    private static final int SKILL_TREE_PANEL_PADDING = 6;
    private static final int SKILL_TREE_PANEL_TITLE_HEIGHT = 12;
    private static final int SKILL_TREE_PANEL_MARGIN = 8;

    private final List<Button> skillTreeButtons = new ArrayList<>();
    private boolean skillTreeAnimOpening;
    private long skillTreeAnimStartMillis;
    private float skillTreeAnimFrom;
    private int skillTreePanelTop;
    private int skillTreePanelHeight;

    private Button loanSharkButton;
    private Button effectRuneShopButton;
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
        int blockHeight = (REGULAR_BUTTON_COUNT - 1) * BUTTON_STRIDE + BUTTON_HEIGHT + DONE_EXTRA_GAP + BUTTON_STRIDE;
        int y = (this.height - blockHeight) / 2 - LIFT_OFFSET;

        boolean chunkBased = this.minecraft.level != null && CraftorioMisc.chunkBased(this.minecraft.level);

        if (this.sacrificeButton != null) {
            this.sacrificeButton.release();
        }

        this.sacrificeButton = (AssemblingButton) Button.builder(Component.literal("Sacrifice"), b -> {
                    PacketDistributor.sendToServer(new RequestSacrificePacket());
                    this.onClose();
                })
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build(builder -> new AssemblingButton(builder, 1.6f));
        this.addRenderableWidget(this.sacrificeButton);
        updateSacrificeButton();
        y += BUTTON_STRIDE + DONE_EXTRA_GAP;



        Component shopLabel = Component.translatable("misc.craftorio.hub_shop");
        if (!ClientShopState.isEnabled()) {
            shopLabel = shopLabel.copy().append(Component.translatable("misc.craftorio.shop_disabled_suffix").withStyle(ChatFormatting.RED));
        }
        this.addRenderableWidget(Button.builder(shopLabel, b -> PacketDistributor.sendToServer(new RequestOpenShopPacket()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

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

        this.effectRuneShopButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.effect_rune_shop_button"), b -> this.minecraft.setScreen(new EffectRuneShopScreen()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.rebirth_button"), b -> this.minecraft.setScreen(new CraftorioRebirthConfirmScreen(this)))
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
        updateEffectRuneShopButton();

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
        this.skillTreeButtons.add(Button.builder(Component.translatable("misc.craftorio.skill_tree_title"),
                b -> this.minecraft.setScreen(new CraftorioBasicSkillTreeScreen("misc.craftorio.skill_tree_title"))).build());
        this.skillTreeButtons.add(Button.builder(Component.translatable("misc.craftorio.rebirth_skill_tree_title"),
                b -> this.minecraft.setScreen(new CraftorioRebirthSkillTreeScreen())).build());
        this.skillTreeButtons.add(Button.builder(Component.translatable("misc.craftorio.sacrifice_skill_tree_title"),
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
    }

    @Override
    public void removed() {
        if (this.sacrificeButton != null) {
            this.sacrificeButton.release();
        }
        super.removed();
    }

    private void updateSacrificeButton() {
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

    private void updateEffectRuneShopButton() {
        Player player = this.minecraft.player;
        boolean unlocked = player != null && CraftorioEffectRuneShop.isUnlocked(player);
        this.effectRuneShopButton.visible = unlocked;
        this.effectRuneShopButton.active = unlocked;
    }

    @Override
    public void tick() {
        updateSacrificeButton();
        updateLoanSharkButton();
        updateEffectRuneShopButton();
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
        layoutSkillTreePanel();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.sacrificeButton != null && this.sacrificeButton.visible) {
            this.sacrificeButton.renderAura(guiGraphics);
        }

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

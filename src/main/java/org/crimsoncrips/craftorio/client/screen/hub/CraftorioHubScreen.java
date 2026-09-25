package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.contract.OwnedContractsScreen;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsScreen;
import org.crimsoncrips.craftorio.client.screen.effect.ActiveEffectsScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.BorderExpandScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.ClaimItemPurchaseScreen;
import org.crimsoncrips.craftorio.client.screen.skill_tree.CraftorioBasicSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.widget.SheetIconButton;
import org.crimsoncrips.craftorio.client.state.ClientContractOfferState;
import org.crimsoncrips.craftorio.client.state.ClientShopState;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.networking.contract.RequestContractOfferPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenValueBrowserPacket;

@OnlyIn(Dist.CLIENT)
public class CraftorioHubScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;
    private static final int REGULAR_BUTTON_COUNT = 8;
    private static final int DONE_EXTRA_GAP = 16;
    private static final int LIFT_OFFSET = 20;

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

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.owned_contracts_button"), b -> this.minecraft.setScreen(new OwnedContractsScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.skill_tree_button"), b -> this.minecraft.setScreen(new CraftorioBasicSkillTreeScreen("misc.craftorio.skill_tree_title")))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.active_effects_button"), b -> this.minecraft.setScreen(new ActiveEffectsScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.rebirth_button"), b -> this.minecraft.setScreen(new CraftorioRebirthConfirmScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE + DONE_EXTRA_GAP;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        int statsSize = 20;
        int statsX = 8;
        int statsY = this.height - statsSize - 8;
        this.addRenderableWidget(Button.builder(Component.empty(), b -> this.minecraft.setScreen(new CraftorioStatisticsScreen()))
                .bounds(statsX, statsY, statsSize, statsSize)
                .tooltip(Tooltip.create(Component.translatable("misc.craftorio.stats_button")))
                .build(builder -> new SheetIconButton(builder, ClientEvents.STATISTICS_ICON_U, ClientEvents.STATISTICS_ICON_V)));

        if (this.minecraft.player != null && this.minecraft.player.isCreative()) {
            int devToolsWidth = 100;
            int devToolsY = this.height - BUTTON_HEIGHT - 8;
            this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_title"), b -> this.minecraft.setScreen(new DevToolsScreen(this)))
                    .bounds(this.width - devToolsWidth - 8, devToolsY, devToolsWidth, BUTTON_HEIGHT).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

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

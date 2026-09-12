package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.ClientContractOfferState;
import org.crimsoncrips.craftorio.client.ClientShopState;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.networking.RequestContractOfferPacket;
import org.crimsoncrips.craftorio.networking.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.networking.RequestOpenValueBrowserPacket;

@OnlyIn(Dist.CLIENT)
public class CraftorioHubScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;
    private static final int REGULAR_BUTTON_COUNT = 7;
    private static final int DONE_EXTRA_GAP = 16;
    private static final int LIFT_OFFSET = 20;

    public CraftorioHubScreen() {
        super(Component.translatable("misc.craftorio.hub_title"));
    }

    @Override
    protected void init() {
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

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.skill_tree_button"), b -> this.minecraft.setScreen(new CraftorioSkillTreeScreen()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.active_effects_button"), b -> this.minecraft.setScreen(new ActiveEffectsScreen(this)))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE + DONE_EXTRA_GAP;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
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

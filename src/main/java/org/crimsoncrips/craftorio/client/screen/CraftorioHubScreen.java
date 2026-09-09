package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.ClientEvents;
import org.crimsoncrips.craftorio.networking.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioHubScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STRIDE = 24;
    private static final int BUTTON_COUNT = 5;

    public CraftorioHubScreen() {
        super(Component.translatable("misc.craftorio.hub_title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int blockHeight = (BUTTON_COUNT - 1) * BUTTON_STRIDE + BUTTON_HEIGHT;
        int y = (this.height - blockHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.hub_shop"), b -> PacketDistributor.sendToServer(new RequestOpenShopPacket()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.claim_shop_title"), b -> this.minecraft.setScreen(new ClaimItemPurchaseScreen()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.value_browser_title"), b -> this.minecraft.setScreen(new ValueBrowserScreen()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.expand_border"), b -> this.minecraft.setScreen(new BorderExpandScreen()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_STRIDE;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Player player = this.minecraft.player;
        if (player == null || this.minecraft.level == null) return;

        int indicatorX = this.width - ClientEvents.BORDER_MODE_INDICATOR_SIZE - 8;
        ClientEvents.drawBorderModeIndicators(guiGraphics, indicatorX, 8);

        int textX = this.width / 2 + 120;
        int y = 30;

        List<CraftorioShipmentContract> contracts = CraftorioMisc.getCraftorioContracts(player);
        for (CraftorioShipmentContract contract : contracts) {
            String line = Component.translatable("misc.craftorio.contract_info", contract.getName(), contract.getTime()).getString();
            guiGraphics.drawString(this.font, line, textX, y, 0xFFFFFF, false);
            y += this.font.lineHeight + 2;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

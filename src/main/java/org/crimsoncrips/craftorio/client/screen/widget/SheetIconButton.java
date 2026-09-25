package org.crimsoncrips.craftorio.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.events.ClientEvents;

@OnlyIn(Dist.CLIENT)
public class SheetIconButton extends Button {

    private final int u;
    private final int v;

    public SheetIconButton(Button.Builder builder, int u, int v) {
        super(builder);
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        int size = ClientEvents.STATUS_ICON_SIZE;
        int iconX = getX() + (getWidth() - size) / 2;
        int iconY = getY() + (getHeight() - size) / 2;
        guiGraphics.blit(ClientEvents.STATUS_ICONS, iconX, iconY, u, v, size, size,
                ClientEvents.STATUS_ICON_SHEET_WIDTH, ClientEvents.STATUS_ICON_SHEET_HEIGHT);
    }
}

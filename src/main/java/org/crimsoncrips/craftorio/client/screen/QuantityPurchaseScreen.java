package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class QuantityPurchaseScreen extends Screen {

    private final ItemStack displayStack;

    protected int quantity = 1;
    private EditBox quantityBox;
    private Component errorMessage = CommonComponents.EMPTY;

    protected QuantityPurchaseScreen(Component title, ItemStack displayStack) {
        super(title);
        this.displayStack = displayStack;
    }

    protected abstract int maxAffordable();

    protected abstract void onConfirm(int quantity);

    protected abstract void renderPriceInfo(GuiGraphics graphics, int centerX, int centerY);

    protected void onCancel() {
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.quantityBox = new EditBox(this.font, centerX - 50, centerY + 6, 100, 20, Component.translatable("misc.craftorio.quantity"));
        this.quantityBox.setValue(String.valueOf(quantity));
        this.quantityBox.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.quantityBox.setResponder(this::onQuantityTyped);
        this.addRenderableWidget(this.quantityBox);
        this.setInitialFocus(this.quantityBox);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.max_minus"), b -> setQuantity(0))
                .bounds(centerX - 94, centerY + 34, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> setQuantity(quantity - 1))
                .bounds(centerX - 46, centerY + 34, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> setQuantity(quantity + 1))
                .bounds(centerX + 2, centerY + 34, 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.max_plus"), b -> setQuantity(maxAffordable()))
                .bounds(centerX + 50, centerY + 34, 44, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.buy"), b -> this.confirm())
                .bounds(centerX - 50, centerY + 60, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.cancel"), b -> this.onCancel())
                .bounds(centerX + 2, centerY + 60, 48, 20).build());
    }

    protected void setQuantity(int newQuantity) {
        this.quantity = Math.max(0, newQuantity);
        this.quantityBox.setValue(String.valueOf(this.quantity));
    }

    private void onQuantityTyped(String value) {
        try {
            this.quantity = Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            this.quantity = 0;
        }
    }

    private void confirm() {
        if (quantity <= 0) {
            this.errorMessage = Component.translatable("misc.craftorio.enter_valid_quantity").withColor(0xFF5555);
            return;
        }

        onConfirm(quantity);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.renderItem(this.displayStack, centerX - 8, centerY - 40);
        guiGraphics.drawCenteredString(this.font, this.displayStack.getHoverName(), centerX, centerY - 58, 0xFFFFFF);

        renderPriceInfo(guiGraphics, centerX, centerY);

        if (!this.errorMessage.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.errorMessage, centerX, centerY + 86, 0xFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

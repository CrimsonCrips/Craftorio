package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.networking.AddItemToContractCreatorPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ContractCreatorItemBrowserScreen extends CatalogScreen<ItemStack> {

    private static List<ItemStack> catalog;

    private final net.minecraft.client.gui.screens.Screen parent;

    public ContractCreatorItemBrowserScreen(net.minecraft.client.gui.screens.Screen parent) {
        super(Component.translatable("misc.craftorio.dev_tools_browse_items"));
        this.parent = parent;
    }

    private static void ensureCatalogBuilt() {
        if (catalog != null) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            CreativeModeTabs.tryRebuildTabContents(minecraft.level.enabledFeatures(), true, minecraft.level.registryAccess());
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : CreativeModeTabs.searchTab().getDisplayItems()) {
            if (stack.isEmpty()) continue;
            stacks.add(stack.copy());
        }
        catalog = stacks;
    }

    @Override
    protected List<ItemStack> buildCatalog() {
        ensureCatalogBuilt();
        return catalog;
    }

    @Override
    protected String getSearchName(ItemStack stack) {
        return stack.getHoverName().getString();
    }

    @Override
    protected String getSearchNamespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }

    @Override
    protected Stream<ResourceLocation> getSearchTags(ItemStack stack) {
        return stack.getTags().map(TagKey::location);
    }

    @Override
    protected AbstractWidget createEntryWidget(int x, int y, ItemStack stack) {
        return new BrowseEntryButton(x, y, stack);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @OnlyIn(Dist.CLIENT)
    private class BrowseEntryButton extends AbstractButton {

        private final ItemStack displayStack;

        BrowseEntryButton(int x, int y, ItemStack stack) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, CommonComponents.EMPTY);
            this.displayStack = stack.copy();
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(this.displayStack.getHoverName()));
        }

        @Override
        public void onPress() {
            PacketDistributor.sendToServer(new AddItemToContractCreatorPacket(this.displayStack.copy()));
            ContractCreatorItemBrowserScreen.this.minecraft.setScreen(ContractCreatorItemBrowserScreen.this.parent);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.isHovered()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x80FFFFFF);
            }

            guiGraphics.renderItem(this.displayStack, this.getX() + 1, this.getY() + 1);
            guiGraphics.renderItemDecorations(ContractCreatorItemBrowserScreen.this.font, this.displayStack, this.getX() + 1, this.getY() + 1);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

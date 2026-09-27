package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
public final class CraftorioScreenScroll {

    private static final int CONTENT_PADDING = 6;
    private static final double SCROLL_STEP = 20.0;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_MARGIN = 2;
    private static final int SCROLLBAR_MIN_THUMB = 16;
    private static final int SCROLLBAR_TRACK_COLOR = 0x55000000;
    private static final int SCROLLBAR_THUMB_COLOR = 0xFFC0C0C0;

    private static Screen trackedScreen;
    private static double offset;
    private static double minOffset;
    private static double maxOffset;
    private static boolean pendingReset = true;
    private static double activeRenderOffset;

    private CraftorioScreenScroll() {}

    public static boolean isScrollable(Screen screen) {
        return screen instanceof ScrollableScreen;
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (isScrollable(event.getScreen())) {
            trackedScreen = event.getScreen();
            pendingReset = true;
        }
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Post event) {
        Screen screen = event.getScreen();
        if (!isScrollable(screen)) return;

        update(screen);
        if (maxOffset <= minOffset) return;
        offset = Mth.clamp(offset - event.getScrollDeltaY() * SCROLL_STEP, minOffset, maxOffset);
    }

    public static double offset(Screen screen) {
        if (screen != trackedScreen || !isScrollable(screen)) return 0.0;
        return offset;
    }

    public static double currentScreenOffset() {
        return offset(Minecraft.getInstance().screen);
    }

    public static double activeRenderOffset() {
        return activeRenderOffset;
    }

    public static void beginRender(double renderOffset) {
        activeRenderOffset = renderOffset;
    }

    public static void endRender() {
        activeRenderOffset = 0.0;
    }

    public static void update(Screen screen) {
        if (screen != trackedScreen) {
            trackedScreen = screen;
            pendingReset = true;
        }

        double top = 0.0;
        double bottom = screen.height;
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && !widget.visible) continue;
            if (child instanceof LayoutElement element) {
                top = Math.min(top, element.getY());
                bottom = Math.max(bottom, element.getY() + element.getHeight());
            }
        }
        if (screen instanceof AbstractContainerScreen<?> container) {
            top = Math.min(top, container.getGuiTop());
            bottom = Math.max(bottom, container.getGuiTop() + container.getYSize());
        }

        if (top < 0.0) top -= CONTENT_PADDING;
        if (bottom > screen.height) bottom += CONTENT_PADDING;

        minOffset = top;
        maxOffset = Math.max(top, bottom - screen.height);

        if (pendingReset) {
            offset = minOffset;
            pendingReset = false;
        } else {
            offset = Mth.clamp(offset, minOffset, maxOffset);
        }
    }

    public static void drawScrollbar(GuiGraphics guiGraphics, Screen screen) {
        if (maxOffset <= minOffset) return;

        double range = maxOffset - minOffset;
        int trackHeight = screen.height;
        int thumbHeight = Math.max(SCROLLBAR_MIN_THUMB, (int) (trackHeight * trackHeight / (trackHeight + range)));
        int thumbY = (int) ((offset - minOffset) / range * (trackHeight - thumbHeight));
        int right = screen.width - SCROLLBAR_MARGIN;
        int left = right - SCROLLBAR_WIDTH;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0f, 0f, 500f);
        guiGraphics.fill(left, 0, right, trackHeight, SCROLLBAR_TRACK_COLOR);
        guiGraphics.fill(left, thumbY, right, thumbY + thumbHeight, SCROLLBAR_THUMB_COLOR);
        guiGraphics.pose().popPose();
    }
}

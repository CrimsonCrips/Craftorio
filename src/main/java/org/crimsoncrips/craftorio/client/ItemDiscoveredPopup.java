package org.crimsoncrips.craftorio.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.Craftorio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ItemDiscoveredPopup {

    private static final ResourceLocation LOCKED_TEXTURE = Craftorio.getGuiTexture("locked.png");
    private static final long DURATION_MS = 500L;
    private static final float POP_SCALE = 1.4f;
    private static final float POP_FRACTION = 0.2f;
    private static final float DRIFT_Y = 20.0f;
    private static final int ICON_SIZE = 16;
    private static final int LOCK_SIZE = 8;
    private static final int POPUP_GAP = 20;

    private static final List<Popup> popups = new ArrayList<>();

    private ItemDiscoveredPopup() {}

    public static void spawn(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null) return;

        popups.add(new Popup(new ItemStack(item), System.currentTimeMillis()));
    }

    public static void render(GuiGraphics graphics, float anchorX, float anchorY) {
        if (popups.isEmpty()) return;

        long now = System.currentTimeMillis();
        Iterator<Popup> iterator = popups.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Popup popup = iterator.next();
            long age = now - popup.startMillis;
            if (age >= DURATION_MS) {
                iterator.remove();
                continue;
            }

            float t = age / (float) DURATION_MS;
            float drawX = anchorX;
            float drawY = anchorY - 24 - index * POPUP_GAP - DRIFT_Y * easeOutCubic(t);

            graphics.renderItem(popup.stack, (int) drawX - ICON_SIZE / 2, (int) drawY - ICON_SIZE / 2);

            float scale = lockScale(t);
            if (scale > 0f) {
                float alpha = lockAlpha(t);
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);
                graphics.pose().translate(drawX, drawY, 0f);
                graphics.pose().scale(scale, scale, 1f);
                graphics.pose().translate(-drawX, -drawY, 0f);
                graphics.setColor(1f, 1f, 1f, alpha);
                graphics.blit(LOCKED_TEXTURE, (int) drawX - LOCK_SIZE / 2, (int) drawY - LOCK_SIZE / 2, 0.0F, 0.0F, LOCK_SIZE, LOCK_SIZE, LOCK_SIZE, LOCK_SIZE);
                graphics.setColor(1f, 1f, 1f, 1f);
                graphics.pose().popPose();
            }

            index++;
        }
    }

    private static float lockScale(float t) {
        if (t <= POP_FRACTION) {
            return 1f + (POP_SCALE - 1f) * (t / POP_FRACTION);
        }
        float shrinkT = (t - POP_FRACTION) / (1f - POP_FRACTION);
        return POP_SCALE * (1f - shrinkT);
    }

    private static float lockAlpha(float t) {
        if (t <= POP_FRACTION) return 1f;
        float shrinkT = (t - POP_FRACTION) / (1f - POP_FRACTION);
        return 1f - shrinkT;
    }

    private static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    private record Popup(ItemStack stack, long startMillis) {}
}

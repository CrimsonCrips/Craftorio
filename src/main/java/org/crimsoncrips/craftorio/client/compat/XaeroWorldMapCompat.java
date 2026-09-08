package org.crimsoncrips.craftorio.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.custom_border.CraftorioBorder;
import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;
import xaero.map.world.MapWorld;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class XaeroWorldMapCompat {

    private static final int CLAIM_COLOR = 0x30D5C8;
    private static final int CHUNK_FILL_ALPHA = 0x50;
    private static final int BORDER_FILL_ALPHA = 0x40;
    private static final int OUTLINE_ALPHA = 0xC0;

    private static final Map<ResourceKey<Level>, Set<Long>> claimedChunks = new HashMap<>();

    private static final Field CAMERA_X_FIELD;
    private static final Field CAMERA_Z_FIELD;
    private static final Field SCALE_FIELD;
    private static boolean reflectionFailed;

    static {
        Field cameraX = null, cameraZ = null, scale = null;
        try {
            cameraX = GuiMap.class.getDeclaredField("cameraX");
            cameraX.setAccessible(true);
            cameraZ = GuiMap.class.getDeclaredField("cameraZ");
            cameraZ.setAccessible(true);
            scale = GuiMap.class.getDeclaredField("scale");
            scale.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Craftorio.LOGGER.error("Xaero World Map compat: failed to locate GuiMap camera fields via reflection, chunk claim overlay disabled", e);
            reflectionFailed = true;
        }
        CAMERA_X_FIELD = cameraX;
        CAMERA_Z_FIELD = cameraZ;
        SCALE_FIELD = scale;
    }

    private static final int SCAN_INTERVAL_TICKS = 20;
    private static int scanCooldown = 0;

    private XaeroWorldMapCompat() {}

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!CraftorioMisc.chunkBased(mc.level)) return;

        if (scanCooldown-- > 0) return;
        scanCooldown = SCAN_INTERVAL_TICKS;

        ChunkPos center = new ChunkPos(mc.player.blockPosition());
        int radius = mc.options.renderDistance().get();
        Set<Long> set = claimedChunks.computeIfAbsent(mc.level.dimension(), d -> new HashSet<>());

        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                ChunkAccess chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;

                long packed = ChunkPos.asLong(cx, cz);
                if (CraftorioMisc.isOwnedBy(chunk, mc.player)) {
                    set.add(packed);
                } else {
                    set.remove(packed);
                }
            }
        }
    }

    public static void renderOverlay(ScreenEvent.Render.Post event) {
        if (reflectionFailed) return;
        if (!(event.getScreen() instanceof GuiMap guiMap)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Double cameraX = readDouble(CAMERA_X_FIELD, guiMap);
        Double cameraZ = readDouble(CAMERA_Z_FIELD, guiMap);
        Double scale = readDouble(SCALE_FIELD, guiMap);
        if (cameraX == null || cameraZ == null || scale == null || scale <= 0.0) return;

        MapProcessor mapProcessor = guiMap.getMapProcessor();
        if (mapProcessor == null) return;
        MapWorld mapWorld = mapProcessor.getMapWorld();
        if (mapWorld == null) return;
        ResourceKey<Level> viewedDim = mapWorld.getCurrentDimensionId();
        if (viewedDim == null) return;

        double guiScale = mc.getWindow().getGuiScale();
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();

        double halfViewX = guiWidth / 2.0 * guiScale / scale;
        double halfViewZ = guiHeight / 2.0 * guiScale / scale;
        double viewMinX = cameraX - halfViewX;
        double viewMaxX = cameraX + halfViewX;
        double viewMinZ = cameraZ - halfViewZ;
        double viewMaxZ = cameraZ + halfViewZ;

        GuiGraphics graphics = event.getGuiGraphics();
        Level playerLevel = mc.player.level();
        boolean viewingOwnDimension = playerLevel.dimension().equals(viewedDim);

        if (viewingOwnDimension && CraftorioMisc.chunkBased(playerLevel)) {
            Set<Long> claims = claimedChunks.get(viewedDim);
            if (claims == null || claims.isEmpty()) return;

            for (long packed : claims) {
                ChunkPos pos = new ChunkPos(packed);
                double minX = pos.getMinBlockX();
                double maxX = pos.getMaxBlockX() + 1.0;
                double minZ = pos.getMinBlockZ();
                double maxZ = pos.getMaxBlockZ() + 1.0;
                if (maxX < viewMinX || minX > viewMaxX || maxZ < viewMinZ || minZ > viewMaxZ) continue;

                drawWorldRect(graphics, minX, minZ, maxX, maxZ, cameraX, cameraZ, scale, guiScale, guiWidth, guiHeight,
                        withAlpha(CHUNK_FILL_ALPHA), withAlpha(OUTLINE_ALPHA));
            }
        } else if (viewingOwnDimension) {
            CraftorioBorder border = CraftorioMisc.getCraftorioBorder(mc.player, viewedDim);
            if (border == null) return;

            double half = border.getSize() / 2.0;
            double minX = border.getCenterX() - half;
            double maxX = border.getCenterX() + half;
            double minZ = border.getCenterZ() - half;
            double maxZ = border.getCenterZ() + half;
            if (maxX < viewMinX || minX > viewMaxX || maxZ < viewMinZ || minZ > viewMaxZ) return;

            drawWorldRect(graphics, minX, minZ, maxX, maxZ, cameraX, cameraZ, scale, guiScale, guiWidth, guiHeight,
                    withAlpha(BORDER_FILL_ALPHA), withAlpha(OUTLINE_ALPHA));
        }
    }

    private static int withAlpha(int alpha) {
        return (alpha << 24) | CLAIM_COLOR;
    }

    private static void drawWorldRect(GuiGraphics graphics, double minX, double minZ, double maxX, double maxZ,
                                       double cameraX, double cameraZ, double scale, double guiScale,
                                       int guiWidth, int guiHeight, int fillColor, int outlineColor) {
        int x1 = worldToGui(minX, cameraX, scale, guiScale, guiWidth);
        int x2 = worldToGui(maxX, cameraX, scale, guiScale, guiWidth);
        int y1 = worldToGui(minZ, cameraZ, scale, guiScale, guiHeight);
        int y2 = worldToGui(maxZ, cameraZ, scale, guiScale, guiHeight);
        if (x2 <= x1 || y2 <= y1) return;

        graphics.fill(x1, y1, x2, y2, fillColor);
        graphics.fill(x1, y1, x2, Math.min(y2, y1 + 1), outlineColor);
        graphics.fill(x1, Math.max(y1, y2 - 1), x2, y2, outlineColor);
        graphics.fill(x1, y1, Math.min(x2, x1 + 1), y2, outlineColor);
        graphics.fill(Math.max(x1, x2 - 1), y1, x2, y2, outlineColor);
    }

    private static int worldToGui(double worldCoord, double cameraCoord, double scale, double guiScale, int guiSize) {
        double gui = (worldCoord - cameraCoord) * scale / guiScale + guiSize / 2.0;
        return (int) Math.round(Mth.clamp(gui, -2_000_000.0, 2_000_000.0));
    }

    private static Double readDouble(Field field, GuiMap guiMap) {
        try {
            return field.getDouble(guiMap);
        } catch (IllegalAccessException e) {
            Craftorio.LOGGER.error("Xaero World Map compat: failed to read GuiMap field {}, chunk claim overlay disabled", field.getName(), e);
            reflectionFailed = true;
            return null;
        }
    }
}

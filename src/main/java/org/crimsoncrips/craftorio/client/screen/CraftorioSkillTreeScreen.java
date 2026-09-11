package org.crimsoncrips.craftorio.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.UnlockUpgradePacket;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;
import java.util.*;

public class CraftorioSkillTreeScreen extends Screen {

    private static final ResourceLocation LINE_TEXTURE = Craftorio.getGuiTexture("skill_tree_line.png");
    private static final int NODE_SIZE = 24;
    private static final double RADIUS_STEP = 55.0;
    private static final int LINE_THICKNESS = 2;
    private static final long APPEAR_DURATION_MS = 250L;

    private final Map<ResourceLocation, NodePos> positions = new HashMap<>();
    private final Map<ResourceLocation, CraftorioUpgrade> upgrades = new HashMap<>();
    private final Map<ResourceLocation, ResourceLocation> parents = new HashMap<>();
    private final Map<ResourceLocation, Long> spawnTimes = new HashMap<>();
    private Set<ResourceLocation> lastUnlockedSnapshot = Set.of();
    private int centerX;
    private int centerY;

    public CraftorioSkillTreeScreen() {
        super(Component.translatable("misc.craftorio.skill_tree_title"));
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.positions.clear();
        this.upgrades.clear();
        this.parents.clear();

        Player player = this.minecraft.player;
        if (player == null || this.minecraft.level == null) return;

        this.lastUnlockedSnapshot = new HashSet<>(CraftorioMisc.getUnlockedUpgrades(player));

        Registry<CraftorioUpgrade> registry = this.minecraft.level.registryAccess().registryOrThrow(CraftorioUpgrade.REGISTRY_KEY);

        Map<ResourceLocation, List<ResourceLocation>> children = new HashMap<>();
        List<ResourceLocation> roots = new ArrayList<>();

        for (Holder.Reference<CraftorioUpgrade> holder : registry.holders().toList()) {
            ResourceLocation id = holder.key().location();
            CraftorioUpgrade upgrade = holder.value();
            this.upgrades.put(id, upgrade);

            Optional<ResourceLocation> parent = upgrade.getParent();
            if (parent.isPresent()) {
                this.parents.put(id, parent.get());
                children.computeIfAbsent(parent.get(), k -> new ArrayList<>()).add(id);
            } else {
                roots.add(id);
            }
        }

        double sweepPerRoot = roots.isEmpty() ? 0 : (2 * Math.PI / roots.size());
        double cursor = 0;
        for (ResourceLocation root : roots) {
            layout(root, cursor, sweepPerRoot, 0, children);
            cursor += sweepPerRoot;
        }

        for (Map.Entry<ResourceLocation, NodePos> entry : this.positions.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!isVisible(id, player)) continue;

            this.spawnTimes.putIfAbsent(id, System.currentTimeMillis());

            CraftorioUpgrade upgrade = this.upgrades.get(id);
            NodePos pos = entry.getValue();

            UpgradeNodeButton button = new UpgradeNodeButton(
                    this.centerX + (int) pos.x - NODE_SIZE / 2,
                    this.centerY + (int) pos.y - NODE_SIZE / 2,
                    id, upgrade
            );
            this.addRenderableWidget(button);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    private boolean isVisible(ResourceLocation id, Player player) {
        ResourceLocation parent = this.parents.get(id);
        return parent == null || CraftorioMisc.hasUnlockedUpgrade(player, parent);
    }

    private double leafCount(ResourceLocation id, Map<ResourceLocation, List<ResourceLocation>> childrenMap) {
        List<ResourceLocation> childList = childrenMap.getOrDefault(id, List.of());
        if (childList.isEmpty()) return 1;
        double sum = 0;
        for (ResourceLocation child : childList) sum += leafCount(child, childrenMap);
        return sum;
    }

    private void layout(ResourceLocation id, double angleStart, double angleSweep, int depth, Map<ResourceLocation, List<ResourceLocation>> childrenMap) {
        double angle = angleStart + angleSweep / 2;
        double radius = depth * RADIUS_STEP;
        this.positions.put(id, new NodePos(radius * Math.cos(angle), radius * Math.sin(angle)));

        List<ResourceLocation> childList = childrenMap.getOrDefault(id, List.of());
        if (childList.isEmpty()) return;

        double totalLeaves = leafCount(id, childrenMap);
        double cursor = angleStart;
        for (ResourceLocation child : childList) {
            double childSweep = angleSweep * (leafCount(child, childrenMap) / totalLeaves);
            layout(child, cursor, childSweep, depth + 1, childrenMap);
            cursor += childSweep;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);

        Player player = this.minecraft.player;
        if (player != null) {
            Set<ResourceLocation> currentUnlocked = CraftorioMisc.getUnlockedUpgrades(player);
            if (!currentUnlocked.equals(this.lastUnlockedSnapshot)) {
                this.rebuildWidgets();
            }
        }

        for (Map.Entry<ResourceLocation, ResourceLocation> entry : this.parents.entrySet()) {
            ResourceLocation childId = entry.getKey();
            if (player == null || !isVisible(childId, player)) continue;

            NodePos child = this.positions.get(childId);
            NodePos parent = this.positions.get(entry.getValue());
            if (child == null || parent == null) continue;

            boolean unlocked = CraftorioMisc.hasUnlockedUpgrade(player, childId);
            int lineColor = unlocked ? 0xFF55FF55 : 0xFFFFFFFF;
            drawLine(graphics, this.centerX + (int) parent.x, this.centerY + (int) parent.y,
                    this.centerX + (int) child.x, this.centerY + (int) child.y, lineColor);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (player != null) {
            String pointsLine = Component.translatable("misc.craftorio.points_label").getString()
                    + CraftorioMisc.bigIntFormat(CraftorioMisc.getPoints(player), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
            graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 8, 0xFFFF55);
        }
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 0.5) return;

        float angle = (float) Math.atan2(dy, dx);
        float alpha = ((color >>> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(angle));
        graphics.setColor(red, green, blue, alpha);
        graphics.blit(LINE_TEXTURE, 0, -LINE_THICKNESS / 2, 0, 0, (int) Math.round(length), LINE_THICKNESS, 1, 1);
        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float t1 = t - 1;
        return 1 + c3 * t1 * t1 * t1 + c1 * t1 * t1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record NodePos(double x, double y) {}

    private class UpgradeNodeButton extends AbstractButton {
        private final ResourceLocation id;
        private final CraftorioUpgrade upgrade;

        UpgradeNodeButton(int x, int y, ResourceLocation id, CraftorioUpgrade upgrade) {
            super(x, y, NODE_SIZE, NODE_SIZE, Component.translatable(upgrade.getNameKey()));
            this.id = id;
            this.upgrade = upgrade;
            updateTooltip();
        }

        private void updateTooltip() {
            String costText = CraftorioMisc.bigIntFormat(upgrade.getCost(), Craftorio.CLIENT_CONFIG.POINT_FORMATTING.getAsInt());
            Component tooltip = Component.literal(upgrade.getActualName())
                    .append("\n").append(upgrade.getActualDescription())
                    .append("\n").append(Component.translatable("misc.craftorio.upgrade_cost_tooltip", costText));
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        public void onPress() {
            PacketDistributor.sendToServer(new UnlockUpgradePacket(id));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Player player = CraftorioSkillTreeScreen.this.minecraft.player;
            boolean unlocked = player != null && CraftorioMisc.hasUnlockedUpgrade(player, id);
            boolean parentUnlocked = upgrade.getParent().isEmpty() || (player != null && CraftorioMisc.hasUnlockedUpgrade(player, upgrade.getParent().get()));
            boolean affordable = player != null && CraftorioMisc.getPoints(player).compareTo(upgrade.getCost()) >= 0;

            int borderColor;
            if (unlocked) {
                borderColor = 0xFF55FF55;
            } else if (!parentUnlocked) {
                borderColor = 0xFF666666;
            } else if (affordable) {
                borderColor = 0xFFFFFF55;
            } else {
                borderColor = 0xFFFF5555;
            }

            long elapsed = System.currentTimeMillis() - CraftorioSkillTreeScreen.this.spawnTimes.getOrDefault(id, System.currentTimeMillis());
            float t = Mth.clamp(elapsed / (float) APPEAR_DURATION_MS, 0f, 1f);
            float scale = Mth.clamp(easeOutBack(t), 0f, 1.3f);

            int pivotX = this.getX() + this.getWidth() / 2;
            int pivotY = this.getY() + this.getHeight() / 2;

            double floatPhase = (id.hashCode() & 0xFFFF) * 0.01;
            double time = System.currentTimeMillis() / 600.0;
            int floatX = (int) Math.round(Math.sin(time + floatPhase) * 1.5);
            int floatY = (int) Math.round(Math.cos(time * 0.8 + floatPhase) * 1.5);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(pivotX, pivotY, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.pose().translate(-pivotX, -pivotY, 0);
            guiGraphics.pose().translate(floatX, floatY, 0);

            guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, borderColor);
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), unlocked ? 0xFF203020 : 0xFF202020);
            guiGraphics.blit(upgrade.getIcon(), this.getX() + 3, this.getY() + 3, 0, 0, NODE_SIZE - 6, NODE_SIZE - 6, NODE_SIZE - 6, NODE_SIZE - 6);

            if (this.isHovered()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x40FFFFFF);
            }

            guiGraphics.pose().popPose();
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

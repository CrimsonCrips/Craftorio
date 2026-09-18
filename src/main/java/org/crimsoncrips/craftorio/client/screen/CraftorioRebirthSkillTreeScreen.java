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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.UnlockRebirthUpgradePacket;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CraftorioRebirthSkillTreeScreen extends Screen {

    private static final int NODE_SIZE = 24;
    private static final double RADIUS_STEP = 90.0;
    private static final double NODE_ARC_MARGIN = 40.0;
    private static final int LINE_THICKNESS = 2;
    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 3.5;

    private final Map<ResourceLocation, NodePos> positions = new HashMap<>();
    private final Map<ResourceLocation, CraftorioUpgrade> upgrades = new HashMap<>();
    private final Map<ResourceLocation, ResourceLocation> parents = new HashMap<>();
    private final Map<ResourceLocation, UpgradeNodeButton> buttons = new HashMap<>();
    private Set<ResourceLocation> lastUnlockedSnapshot = Set.of();
    private int centerX;
    private int centerY;
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean panning;
    private double panStartMouseX, panStartMouseY, panStartX, panStartY;

    public CraftorioRebirthSkillTreeScreen() {
        super(Component.translatable("misc.craftorio.rebirth_skill_tree_title"));
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.positions.clear();
        this.upgrades.clear();
        this.parents.clear();
        this.buttons.clear();

        Player player = this.minecraft.player;
        if (player == null || this.minecraft.level == null) return;

        this.lastUnlockedSnapshot = new HashSet<>(CraftorioMisc.getRebirthUpgradePurchaseCounts(player).keySet());

        Registry<CraftorioUpgrade> registry = this.minecraft.level.registryAccess().registryOrThrow(CraftorioUpgrade.REBIRTH_REGISTRY_KEY);

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

        Map<ResourceLocation, Double> leafCounts = new HashMap<>();
        for (ResourceLocation root : roots) {
            computeLeafCounts(root, children, leafCounts);
        }

        double sweepPerRoot = roots.isEmpty() ? 0 : (2 * Math.PI / roots.size());
        double cursor = 0;
        for (ResourceLocation root : roots) {
            layout(root, cursor, sweepPerRoot, 0, 0.0, children, leafCounts);
            cursor += sweepPerRoot;
        }

        for (Map.Entry<ResourceLocation, NodePos> entry : this.positions.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!isVisible(id, player)) continue;

            CraftorioUpgrade upgrade = this.upgrades.get(id);
            NodePos pos = entry.getValue();
            int size = (int) Math.round(NODE_SIZE * zoom);

            UpgradeNodeButton button = new UpgradeNodeButton(
                    this.centerX + (int) Math.round(pos.x * zoom + panX) - size / 2,
                    this.centerY + (int) Math.round(pos.y * zoom + panY) - size / 2,
                    size, id, upgrade
            );
            this.addRenderableWidget(button);
            this.buttons.put(id, button);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.done"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            double factor = scrollY > 0 ? 1.1 : (1.0 / 1.1);
            double newZoom = Mth.clamp(this.zoom * factor, MIN_ZOOM, MAX_ZOOM);
            if (newZoom != this.zoom) {
                this.zoom = newZoom;
                this.rebuildWidgets();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 1) {
            this.panning = true;
            this.panStartMouseX = mouseX;
            this.panStartMouseY = mouseY;
            this.panStartX = this.panX;
            this.panStartY = this.panY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.panning) {
            this.panX = this.panStartX + (mouseX - this.panStartMouseX);
            this.panY = this.panStartY + (mouseY - this.panStartMouseY);
            repositionButtons();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.panning && button == 1) {
            this.panning = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void repositionButtons() {
        int size = (int) Math.round(NODE_SIZE * zoom);
        for (Map.Entry<ResourceLocation, UpgradeNodeButton> entry : this.buttons.entrySet()) {
            NodePos pos = this.positions.get(entry.getKey());
            if (pos == null) continue;
            UpgradeNodeButton button = entry.getValue();
            button.setX(this.centerX + (int) Math.round(pos.x * zoom + panX) - size / 2);
            button.setY(this.centerY + (int) Math.round(pos.y * zoom + panY) - size / 2);
        }
    }

    private boolean isVisible(ResourceLocation id, Player player) {
        ResourceLocation parent = this.parents.get(id);
        return parent == null || CraftorioMisc.hasUnlockedRebirthUpgrade(player, parent);
    }

    private double computeLeafCounts(ResourceLocation id, Map<ResourceLocation, List<ResourceLocation>> childrenMap, Map<ResourceLocation, Double> leafCounts) {
        Double cached = leafCounts.get(id);
        if (cached != null) return cached;

        List<ResourceLocation> childList = childrenMap.getOrDefault(id, List.of());
        double result;
        if (childList.isEmpty()) {
            result = 1;
        } else {
            double sum = 0;
            for (ResourceLocation child : childList) sum += computeLeafCounts(child, childrenMap, leafCounts);
            result = sum;
        }
        leafCounts.put(id, result);
        return result;
    }

    private void layout(ResourceLocation id, double angleStart, double angleSweep, int depth, double parentRadius, Map<ResourceLocation, List<ResourceLocation>> childrenMap, Map<ResourceLocation, Double> leafCounts) {
        double angle = angleStart + angleSweep / 2;

        double radius;
        if (depth == 0) {
            radius = 0;
        } else {
            double minArcLength = NODE_SIZE + NODE_ARC_MARGIN;
            double minRadiusForSweep = angleSweep > 1.0E-6 ? minArcLength / angleSweep : parentRadius + RADIUS_STEP;
            radius = Math.max(parentRadius + RADIUS_STEP, minRadiusForSweep);
        }

        if (depth == 0) {
            this.positions.put(id, new NodePos(0.0, 0.0));
        } else {
            CraftorioUpgrade upgrade = this.upgrades.get(id);
            if (upgrade != null && (upgrade.getX() != 0.0 || upgrade.getY() != 0.0)) {
                this.positions.put(id, new NodePos(upgrade.getX(), upgrade.getY()));
            } else {
                this.positions.put(id, new NodePos(radius * Math.cos(angle), radius * Math.sin(angle)));
            }
        }

        List<ResourceLocation> childList = childrenMap.getOrDefault(id, List.of());
        if (childList.isEmpty()) return;

        double totalLeaves = leafCounts.get(id);
        double cursor = angleStart;
        for (ResourceLocation child : childList) {
            double childSweep = angleSweep * (leafCounts.get(child) / totalLeaves);
            layout(child, cursor, childSweep, depth + 1, radius, childrenMap, leafCounts);
            cursor += childSweep;
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        org.crimsoncrips.craftorio.client.CraftorioStarfield.render(graphics, this.width, this.height, this.panX, this.panY);

        Player player = this.minecraft.player;
        if (player != null) {
            Set<ResourceLocation> currentUnlocked = CraftorioMisc.getRebirthUpgradePurchaseCounts(player).keySet();
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

            boolean unlocked = CraftorioMisc.hasUnlockedRebirthUpgrade(player, childId);
            int lineColor = unlocked ? 0xFF55FF55 : 0xFFFFFFFF;
            drawLine(graphics, this.centerX + (int) Math.round(parent.x * zoom + panX), this.centerY + (int) Math.round(parent.y * zoom + panY),
                    this.centerX + (int) Math.round(child.x * zoom + panX), this.centerY + (int) Math.round(child.y * zoom + panY), lineColor);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (player != null) {
            String lifeLine = Component.translatable("misc.craftorio.rebirth_current_life", CraftorioMisc.getLife(player)).getString();
            String pointsLine = Component.translatable("misc.craftorio.rebirth_life_points_current", CraftorioMisc.getLifePoints(player).toString()).getString();
            graphics.drawCenteredString(this.font, lifeLine, this.width / 2, 8, 0xFFFFFF);
            graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 20, 0xFFFF55);
        }
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 0.5) return;

        float angle = (float) Math.atan2(dy, dx);

        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(angle));
        graphics.fill(0, -LINE_THICKNESS / 2, (int) Math.round(length), -LINE_THICKNESS / 2 + LINE_THICKNESS, color);
        graphics.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record NodePos(double x, double y) {}

    private class UpgradeNodeButton extends AbstractButton {
        private final ResourceLocation id;
        private final CraftorioUpgrade upgrade;
        private int lastTooltipPurchaseCount = -1;

        UpgradeNodeButton(int x, int y, int size, ResourceLocation id, CraftorioUpgrade upgrade) {
            super(x, y, size, size, Component.translatable(upgrade.getNameKey()));
            this.id = id;
            this.upgrade = upgrade;
            Player player = CraftorioRebirthSkillTreeScreen.this.minecraft.player;
            updateTooltip(player != null ? CraftorioMisc.getRebirthUpgradeCount(player, id) : 0);
        }

        private void updateTooltip(int purchaseCount) {
            this.lastTooltipPurchaseCount = purchaseCount;
            String costText = upgrade.getCost().toString();
            Component tooltip = Component.literal(upgrade.getActualName())
                    .append("\n").append(upgrade.getActualDescription())
                    .append("\n").append(Component.translatable("misc.craftorio.upgrade_cost_tooltip", costText))
                    .append("\n").append(Component.translatable("misc.craftorio.upgrade_purchases_tooltip", purchaseCount, upgrade.getMaxPurchases()));
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        public void onPress() {
            PacketDistributor.sendToServer(new UnlockRebirthUpgradePacket(id));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Player player = CraftorioRebirthSkillTreeScreen.this.minecraft.player;
            int purchaseCount = player != null ? CraftorioMisc.getRebirthUpgradeCount(player, id) : 0;
            if (purchaseCount != this.lastTooltipPurchaseCount) {
                updateTooltip(purchaseCount);
            }

            boolean parentUnlocked = upgrade.getParent().isEmpty() || (player != null && CraftorioMisc.hasUnlockedRebirthUpgrade(player, upgrade.getParent().get()));
            boolean maxed = purchaseCount >= upgrade.getMaxPurchases();
            boolean affordable = player != null && CraftorioMisc.getLifePoints(player).compareTo(upgrade.getCost()) >= 0;

            int borderColor;
            if (maxed) {
                borderColor = 0xFF55FF55;
            } else if (!parentUnlocked) {
                borderColor = 0xFF666666;
            } else if (affordable) {
                borderColor = 0xFFFFFF55;
            } else {
                borderColor = 0xFFFF5555;
            }

            guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, borderColor);
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), maxed ? 0xFF203020 : 0xFF202020);
            int inset = Math.max(2, this.getWidth() / 8);
            int iconSize = this.getWidth() - inset * 2;
            guiGraphics.blit(upgrade.getIcon(), this.getX() + inset, this.getY() + inset, 0, 0, iconSize, iconSize, iconSize, iconSize);

            if (this.isHovered()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x40FFFFFF);
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

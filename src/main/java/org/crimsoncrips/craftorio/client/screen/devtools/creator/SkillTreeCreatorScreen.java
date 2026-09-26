package org.crimsoncrips.craftorio.client.screen.devtools.creator;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.render.CraftorioStarfield;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsHelpPanel;
import org.crimsoncrips.craftorio.client.screen.devtools.DevToolsUpgradeTrees;
import org.crimsoncrips.craftorio.networking.devtools.GenerateSkillTreeCodePacket;
import org.crimsoncrips.craftorio.networking.devtools.SkillTreeNodeData;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;
import org.crimsoncrips.craftorio.skill_tree.target.PlayerActionTarget;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioActionEffectUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class SkillTreeCreatorScreen extends Screen {

    private static final String[] CATEGORIES = {"modifier", "attribute", "action_effect"};
    private static final String[] MODIFIER_TARGETS = {
            "MULTIPLIER", "ITEM_BASE_VALUE", "ITEM_TAG_BASE_VALUE", "CONTRACT_REFRESH_SPEED", "EFFECT_TIMER_SPEED",
            "PUNISHMENT_DURATION", "EFFECT_DURATION", "EXPANSION_COST",
            "RARER_CONTRACT_CHANCE", "RARER_EFFECT_CHANCE", "SHOP_COST", "CONTRACT_REFRESH_COST",
            "LOST_BET_REFUND", "MULT_PER_CONTRACT_DONE", "BET_ODDS", "BET_BONUS", "MANUAL_SINK_VALUE"
    };
    private static final String[] ATTRIBUTE_TARGETS = {
            "HEALTH", "SPEED", "DEFENSE", "DAMAGE", "BLOCK_REACH", "JUMP_HEIGHT", "XP_GAIN", "RESISTANCE"
    };
    private static final String[] PLAYER_ACTION_TARGETS = {"WAKE_UP", "TRADE"};
    private static final String[] OPERATIONS = {"ADD", "MULTIPLY"};

    private static final int NODE_W = 64;
    private static final int NODE_H = 24;
    private static final int NUB_GAP = 6;
    private static final double TREE_RADIUS_STEP = 180.0;
    private static final double TREE_NODE_ARC_MARGIN = 90.0;
    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 3.5;

    private final List<DraftNode> nodes = new ArrayList<>();
    private int nextLocalId = 0;
    private int nextNodeNumber = 1;
    private int nodeCascade = 0;
    private boolean jsonExport = false;
    private boolean includeLang = false;
    private UpgradeTree tree = UpgradeTree.BASIC;
    private String savedModId = "yourmodid";
    private double panX = 0, panY = 0;
    private double zoom = 1.0;

    private final Screen parent;
    private final DevToolsHelpPanel helpPanel = new DevToolsHelpPanel();
    private Consumer<ResourceLocation> pickCallback;

    private DraftNode selected;
    private final Set<DraftNode> selectedNodes = new HashSet<>();
    private Component statusMessage;
    private boolean statusSuccess;

    private int canvasLeft, canvasTop, canvasWidth, canvasHeight;
    private int sidebarLeft, sidebarTop, sidebarWidth;
    private int fieldX, fieldWidth;

    private final Map<DraftNode, double[]> dragStartPositions = new HashMap<>();
    private double dragStartMouseX, dragStartMouseY;
    private DraftNode linkingFrom;
    private double linkCursorX, linkCursorY;
    private boolean panning;
    private double panStartMouseX, panStartMouseY, panStartX, panStartY;

    private Button categoryButton;
    private Button modifierTargetButton;
    private Button attributeTargetButton;
    private Button playerActionTargetButton;
    private Button operationButton;
    private Button duplicateButton;
    private Button deleteButton;
    private Button unlinkButton;
    private Button exportButton;
    private Button includeLangButton;

    private EditBox globalModIdBox;
    private EditBox idBox;
    private EditBox descriptionBox;
    private EditBox costBox;
    private Button costScientificButton;
    private EditBox maxPurchasesBox;
    private EditBox valueBox;
    private EditBox itemTagBox;
    private EditBox externalParentBox;
    private EditBox nameBox;

    public SkillTreeCreatorScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.dev_tools_skill_tree_title"));
        this.parent = parent;
    }

    public static SkillTreeCreatorScreen forPicking(Screen parent, Minecraft minecraft, UpgradeTree upgradeTree, Consumer<ResourceLocation> onPick) {
        SkillTreeCreatorScreen screen = new SkillTreeCreatorScreen(parent);
        screen.loadFromRegistry(minecraft, upgradeTree);
        screen.pickCallback = onPick;
        return screen;
    }

    private boolean isPicking() {
        return this.pickCallback != null;
    }

    public void loadFromRegistry(Minecraft minecraft, UpgradeTree upgradeTree) {
        if (minecraft.level == null) return;
        Registry<CraftorioUpgrade> registry = DevToolsUpgradeTrees.registry(minecraft, upgradeTree);

        this.tree = upgradeTree;
        this.selected = null;
        this.selectedNodes.clear();
        this.linkingFrom = null;
        nodes.clear();
        nextLocalId = 0;
        nodeCascade = 0;
        panX = 0;
        panY = 0;
        zoom = 1.0;

        Map<ResourceLocation, DraftNode> nodeByLocation = new HashMap<>();
        Map<DraftNode, ResourceLocation> parentByNode = new HashMap<>();
        Map<String, Integer> namespaceCounts = new HashMap<>();

        for (Holder.Reference<CraftorioUpgrade> holder : registry.holders().toList()) {
            CraftorioUpgrade upgrade = holder.value();
            boolean manual = !(upgrade instanceof CraftorioModifierUpgrade) && !(upgrade instanceof CraftorioAttributeUpgrade) && !(upgrade instanceof CraftorioActionEffectUpgrade);

            ResourceLocation id = holder.key().location();
            DraftNode node = new DraftNode(nextLocalId++, id.getPath());
            node.location = id;

            node.cost = upgrade.getCost().toString();
            node.maxPurchases = String.valueOf(upgrade.getMaxPurchases());
            node.name = Component.translatable(upgrade.getNameKey()).getString();
            node.description = Component.translatable(upgrade.getDescriptionKey()).getString();
            node.x = upgrade.getX();
            node.y = upgrade.getY();
            node.hasStoredPosition = node.x != 0.0 || node.y != 0.0;

            if (manual) {
                node.manual = true;
                node.manualLocation = id.toString();
            } else if (upgrade instanceof CraftorioModifierUpgrade modifierUpgrade) {
                node.category = "modifier";
                node.modifierTargetIndex = indexOf(MODIFIER_TARGETS, modifierUpgrade.getTarget().name());
                node.operationIndex = indexOf(OPERATIONS, modifierUpgrade.getOperation().name());
                double value = modifierUpgrade.getValue();
                if (modifierUpgrade.getOperation().name().equals("ADD") && isTickDurationTarget(node)) {
                    value /= CraftorioMisc.SECONDS_TO_TICKS;
                }
                node.value = String.valueOf(value);
                node.itemTag = modifierUpgrade.getItemTag().map(tag -> tag.location().toString()).orElse("");
            } else if (upgrade instanceof CraftorioAttributeUpgrade attributeUpgrade) {
                node.category = "attribute";
                node.attributeTargetIndex = indexOf(ATTRIBUTE_TARGETS, attributeUpgrade.getTarget().name());
                node.operationIndex = indexOf(OPERATIONS, attributeUpgrade.getOperation().name());
                node.value = String.valueOf(attributeUpgrade.getValue());
            } else if (upgrade instanceof CraftorioActionEffectUpgrade actionEffectUpgrade) {
                node.category = "action_effect";
                node.playerActionTargetIndex = indexOf(PLAYER_ACTION_TARGETS, actionEffectUpgrade.getTarget().name());
                node.value = actionEffectUpgrade.getEffect().toString();
            }

            nodeByLocation.put(id, node);
            if (!manual) {
                namespaceCounts.merge(id.getNamespace(), 1, Integer::sum);
            }
            Optional<ResourceLocation> parentLoc = upgrade.getParent();
            if (parentLoc.isPresent()) {
                parentByNode.put(node, parentLoc.get());
            } else {
                node.lockedRoot = true;
            }
            nodes.add(node);
        }

        for (Map.Entry<DraftNode, ResourceLocation> entry : parentByNode.entrySet()) {
            DraftNode childNode = entry.getKey();
            DraftNode parentNode = nodeByLocation.get(entry.getValue());
            if (parentNode == childNode) {
                continue;
            }
            if (parentNode != null) {
                childNode.parentId = parentNode.localId;
            } else {
                childNode.externalParent = entry.getValue().toString();
            }
        }

        Map<Integer, List<DraftNode>> childrenByParent = new HashMap<>();
        List<DraftNode> roots = new ArrayList<>();
        for (DraftNode node : nodes) {
            if (node.parentId != null) {
                childrenByParent.computeIfAbsent(node.parentId, k -> new ArrayList<>()).add(node);
            } else {
                roots.add(node);
            }
        }

        Map<Integer, Double> leafCounts = new HashMap<>();
        for (DraftNode root : roots) {
            computeTreeLeafCounts(root, childrenByParent, leafCounts);
        }

        double sweepPerRoot = roots.isEmpty() ? 0 : (2 * Math.PI / roots.size());
        double cursor = 0;
        for (DraftNode root : roots) {
            layoutTree(root, cursor, sweepPerRoot, 0, 0.0, childrenByParent, leafCounts);
            cursor += sweepPerRoot;
        }

        savedModId = namespaceCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("yourmodid");
    }

    private static double computeTreeLeafCounts(DraftNode node, Map<Integer, List<DraftNode>> childrenByParent, Map<Integer, Double> leafCounts) {
        Double cached = leafCounts.get(node.localId);
        if (cached != null) return cached;

        List<DraftNode> childList = childrenByParent.getOrDefault(node.localId, List.of());
        double result;
        if (childList.isEmpty()) {
            result = 1;
        } else {
            double sum = 0;
            for (DraftNode child : childList) sum += computeTreeLeafCounts(child, childrenByParent, leafCounts);
            result = sum;
        }
        leafCounts.put(node.localId, result);
        return result;
    }

    private static void layoutTree(DraftNode node, double angleStart, double angleSweep, int depth, double parentRadius, Map<Integer, List<DraftNode>> childrenByParent, Map<Integer, Double> leafCounts) {
        double angle = angleStart + angleSweep / 2;

        double radius;
        if (depth == 0) {
            radius = 0;
        } else {
            double minArcLength = NODE_W + TREE_NODE_ARC_MARGIN;
            double minRadiusForSweep = angleSweep > 1.0E-6 ? minArcLength / angleSweep : parentRadius + TREE_RADIUS_STEP;
            radius = Math.max(parentRadius + TREE_RADIUS_STEP, minRadiusForSweep);
        }

        if (depth == 0) {
            node.x = 0.0;
            node.y = 0.0;
        } else if (!node.hasStoredPosition) {
            node.x = radius * Math.cos(angle);
            node.y = radius * Math.sin(angle);
        }

        List<DraftNode> childList = childrenByParent.getOrDefault(node.localId, List.of());
        if (childList.isEmpty()) return;

        double totalLeaves = leafCounts.get(node.localId);
        double cursor = angleStart;
        for (DraftNode child : childList) {
            double childSweep = angleSweep * (leafCounts.get(child.localId) / totalLeaves);
            layoutTree(child, cursor, childSweep, depth + 1, radius, childrenByParent, leafCounts);
            cursor += childSweep;
        }
    }

    private void toggleCostScientific() {
        if (selected == null) return;
        String current = costBox.getValue();
        BigInteger value;
        try {
            value = CraftorioMisc.scientificToInt(current);
        } catch (Exception e) {
            return;
        }
        String converted = current.toLowerCase().contains("e") ? value.toString() : toScientificNotation(value);
        costBox.setValue(converted);
    }

    private static String toScientificNotation(BigInteger value) {
        if (value.signum() == 0) return "0";
        boolean negative = value.signum() < 0;
        String digits = value.abs().toString();
        int exponent = digits.length() - 1;
        String mantissa = digits.length() == 1 ? digits : digits.charAt(0) + "." + digits.substring(1).replaceAll("0+$", "");
        if (mantissa.endsWith(".")) mantissa = mantissa.substring(0, mantissa.length() - 1);
        return (negative ? "-" : "") + mantissa + "e" + exponent;
    }

    private static int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) return i;
        }
        return 0;
    }

    @Override
    protected void init() {
        if (isPicking()) {
            initPicking();
            return;
        }
        this.sidebarWidth = 260;
        this.canvasLeft = 8;
        this.canvasTop = 30;
        this.sidebarLeft = this.width - sidebarWidth - 8;
        this.sidebarTop = canvasTop;
        this.canvasWidth = Math.max(50, sidebarLeft - canvasLeft - 8);
        this.canvasHeight = Math.max(50, this.height - 68 - canvasTop);

        this.fieldX = sidebarLeft + 90;
        this.fieldWidth = sidebarWidth - 96;
        int y = sidebarTop;
        int rowHeight = 20;

        this.globalModIdBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("mod id"));
        this.globalModIdBox.setMaxLength(256);
        this.globalModIdBox.setValue(savedModId);
        this.globalModIdBox.setResponder(s -> savedModId = s);
        this.addRenderableWidget(this.globalModIdBox);
        y += rowHeight;

        this.includeLangButton = Button.builder(Component.literal(String.valueOf(includeLang)), b -> {
            includeLang = !includeLang;
            includeLangButton.setMessage(Component.literal(String.valueOf(includeLang)));
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.includeLangButton);
        y += rowHeight;

        this.categoryButton = Button.builder(Component.literal("modifier"), b -> {
            if (selected == null) return;
            selected.category = CATEGORIES[(indexOf(CATEGORIES, selected.category) + 1) % CATEGORIES.length];
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.categoryButton);
        y += rowHeight;

        this.idBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("id"));
        this.idBox.setMaxLength(256);
        this.idBox.setResponder(s -> { if (selected != null) selected.id = s; });
        this.addRenderableWidget(this.idBox);
        y += rowHeight;

        this.descriptionBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("description"));
        this.descriptionBox.setMaxLength(256);
        this.descriptionBox.setResponder(s -> { if (selected != null) selected.description = s; });
        this.addRenderableWidget(this.descriptionBox);
        y += rowHeight;

        this.costBox = new EditBox(this.font, fieldX, y, fieldWidth - 24, 16, Component.literal("cost"));
        this.costBox.setMaxLength(256);
        this.costBox.setResponder(s -> { if (selected != null) selected.cost = s; });
        this.addRenderableWidget(this.costBox);

        this.costScientificButton = Button.builder(Component.literal("1e"), b -> toggleCostScientific())
                .bounds(fieldX + fieldWidth - 22, y, 22, 16).build();
        this.addRenderableWidget(this.costScientificButton);
        y += rowHeight;

        this.maxPurchasesBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("max purchases"));
        this.maxPurchasesBox.setMaxLength(256);
        this.maxPurchasesBox.setResponder(s -> { if (selected != null) selected.maxPurchases = s; });
        this.addRenderableWidget(this.maxPurchasesBox);
        y += rowHeight;

        this.modifierTargetButton = Button.builder(Component.literal(MODIFIER_TARGETS[0]), b -> {
            if (selected == null) return;
            selected.modifierTargetIndex = (selected.modifierTargetIndex + 1) % MODIFIER_TARGETS.length;
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.modifierTargetButton);

        this.attributeTargetButton = Button.builder(Component.literal(ATTRIBUTE_TARGETS[0]), b -> {
            if (selected == null) return;
            selected.attributeTargetIndex = (selected.attributeTargetIndex + 1) % ATTRIBUTE_TARGETS.length;
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.attributeTargetButton);

        this.playerActionTargetButton = Button.builder(Component.literal(PLAYER_ACTION_TARGETS[0]), b -> {
            if (selected == null) return;
            selected.playerActionTargetIndex = (selected.playerActionTargetIndex + 1) % PLAYER_ACTION_TARGETS.length;
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.playerActionTargetButton);
        y += rowHeight;

        this.operationButton = Button.builder(Component.literal(OPERATIONS[0]), b -> {
            if (selected == null) return;
            selected.operationIndex = (selected.operationIndex + 1) % OPERATIONS.length;
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.operationButton);
        y += rowHeight;

        this.valueBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("value"));
        this.valueBox.setMaxLength(256);
        this.valueBox.setResponder(s -> { if (selected != null) selected.value = s; });
        this.addRenderableWidget(this.valueBox);
        y += rowHeight;

        this.itemTagBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("item tag"));
        this.itemTagBox.setMaxLength(256);
        this.itemTagBox.setResponder(s -> { if (selected != null) selected.itemTag = s; });
        this.addRenderableWidget(this.itemTagBox);
        y += rowHeight;

        this.nameBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("name"));
        this.nameBox.setMaxLength(256);
        this.nameBox.setResponder(s -> { if (selected != null) selected.name = s; });
        this.addRenderableWidget(this.nameBox);
        y += rowHeight;

        this.externalParentBox = new EditBox(this.font, fieldX, y, fieldWidth, 16, Component.literal("parent"));
        this.externalParentBox.setMaxLength(256);
        this.externalParentBox.setResponder(s -> { if (selected != null) selected.externalParent = s; });
        this.addRenderableWidget(this.externalParentBox);

        this.unlinkButton = Button.builder(Component.translatable("misc.craftorio.dev_tools_skill_tree_unlink"), b -> {
            if (selected == null) return;
            selected.parentId = null;
            refreshSidebarFromSelection();
        }).bounds(fieldX, y, fieldWidth, 16).build();
        this.addRenderableWidget(this.unlinkButton);

        int barY1 = this.height - 56;
        int barY2 = this.height - 32;
        int btnW = 110;
        int gap = 6;
        int rowX = this.width / 2 - (btnW * 3 + gap * 2) / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_skill_tree_new_node"), b -> createNode())
                .bounds(rowX, barY1, btnW, 20).build());

        this.duplicateButton = Button.builder(Component.translatable("misc.craftorio.dev_tools_skill_tree_duplicate"), b -> duplicateSelected())
                .bounds(rowX + btnW + gap, barY1, btnW, 20).build();
        this.addRenderableWidget(this.duplicateButton);

        this.deleteButton = Button.builder(Component.translatable("misc.craftorio.dev_tools_skill_tree_delete"), b -> deleteSelected())
                .bounds(rowX + (btnW + gap) * 2, barY1, btnW, 20).build();
        this.addRenderableWidget(this.deleteButton);

        this.exportButton = Button.builder(exportLabel(), b -> {
            jsonExport = !jsonExport;
            exportButton.setMessage(exportLabel());
        }).bounds(rowX, barY2, btnW, 20).build();
        this.addRenderableWidget(this.exportButton);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_generate"), b -> generate())
                .bounds(rowX + btnW + gap, barY2, btnW, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(rowX + (btnW + gap) * 2, barY2, btnW, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.dev_tools_edit_skill_tree"), b ->
                        DevToolsUpgradeTrees.openTreePicker(this.minecraft, this, pickedTree -> {
                            loadFromRegistry(this.minecraft, pickedTree);
                            this.minecraft.setScreen(this);
                        }))
                .bounds(this.width - 108, this.height - 28, 100, 20).build());

        this.addRenderableWidget(this.helpPanel.createButton(this.width, 6, true, () -> {}));

        refreshSidebarFromSelection();
    }

    private void initPicking() {
        this.canvasLeft = 8;
        this.canvasTop = 30;
        this.canvasWidth = Math.max(50, this.width - 16);
        this.canvasHeight = Math.max(50, this.height - 40 - canvasTop);

        this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.back"), b -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 - 55, this.height - 30, 110, 20).build());
        this.addRenderableWidget(this.helpPanel.createButton(this.width, 6, true, () -> {}));
    }

    private void renderPicking(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        DraftNode hovered = isInCanvas(mouseX, mouseY) ? findNodeByBody(mouseX, mouseY) : null;
        if (hovered != null) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(hovered.name.isEmpty() ? hovered.id : hovered.name));
            lines.add(Component.literal(hovered.location == null ? hovered.id : hovered.location.toString()).withStyle(style -> style.withColor(0x808080)));
            if (hovered.manual) {
                lines.add(Component.translatable("misc.craftorio.dev_tools_pick_upgrade_manual").withStyle(style -> style.withColor(0xFF5555)));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }

        this.helpPanel.render(graphics, this.font, this.width, 30, true, List.of(
                Component.translatable("misc.craftorio.dev_tools_pick_upgrade_help_1"),
                Component.translatable("misc.craftorio.dev_tools_skill_tree_help_3")
        ));
    }

    public void showGenerateResult(boolean success, Component message) {
        this.statusMessage = message;
        this.statusSuccess = success;
    }

    private Component exportLabel() {
        return Component.translatable(jsonExport ? "misc.craftorio.dev_tools_export_json" : "misc.craftorio.dev_tools_export_code");
    }

    private void refreshSidebarFromSelection() {
        boolean has = selected != null;

        this.categoryButton.visible = has;
        this.categoryButton.active = has;
        this.idBox.visible = has;
        this.idBox.active = has;
        this.costBox.visible = has;
        this.costBox.active = has;
        this.costScientificButton.visible = has;
        this.costScientificButton.active = has;
        this.maxPurchasesBox.visible = has;
        this.maxPurchasesBox.active = has;
        this.operationButton.visible = has;
        this.operationButton.active = has;
        this.valueBox.visible = has;
        this.valueBox.active = has;

        this.duplicateButton.active = has;
        this.deleteButton.active = has;

        if (!has) {
            this.modifierTargetButton.visible = false;
            this.modifierTargetButton.active = false;
            this.attributeTargetButton.visible = false;
            this.attributeTargetButton.active = false;
            this.playerActionTargetButton.visible = false;
            this.playerActionTargetButton.active = false;
            this.itemTagBox.visible = false;
            this.itemTagBox.active = false;
            this.nameBox.visible = false;
            this.nameBox.active = false;
            this.descriptionBox.visible = false;
            this.descriptionBox.active = false;
            this.externalParentBox.visible = false;
            this.externalParentBox.active = false;
            this.unlinkButton.visible = false;
            this.unlinkButton.active = false;
            return;
        }

        this.duplicateButton.active = !selected.manual;

        this.costBox.setValue(selected.cost);
        this.maxPurchasesBox.setValue(selected.maxPurchases);

        this.nameBox.visible = includeLang;
        this.nameBox.active = includeLang;
        this.nameBox.setValue(selected.name);

        this.descriptionBox.visible = includeLang;
        this.descriptionBox.active = includeLang;
        this.descriptionBox.setValue(selected.description);

        boolean linked = selected.parentId != null;
        this.externalParentBox.visible = !linked;
        this.externalParentBox.active = !linked;
        this.externalParentBox.setValue(selected.externalParent);
        this.unlinkButton.visible = linked;
        this.unlinkButton.active = linked;

        if (selected.manual) {
            this.categoryButton.visible = false;
            this.categoryButton.active = false;
            this.idBox.visible = false;
            this.idBox.active = false;
            this.maxPurchasesBox.visible = false;
            this.maxPurchasesBox.active = false;
            this.operationButton.visible = false;
            this.operationButton.active = false;
            this.valueBox.visible = false;
            this.valueBox.active = false;
            this.modifierTargetButton.visible = false;
            this.modifierTargetButton.active = false;
            this.attributeTargetButton.visible = false;
            this.attributeTargetButton.active = false;
            this.playerActionTargetButton.visible = false;
            this.playerActionTargetButton.active = false;
            this.itemTagBox.visible = false;
            this.itemTagBox.active = false;
            return;
        }

        this.categoryButton.setMessage(Component.literal(selected.category));
        this.idBox.setValue(selected.id);
        this.operationButton.setMessage(Component.literal(OPERATIONS[selected.operationIndex]));
        this.valueBox.setValue(selected.value);

        boolean isModifier = selected.category.equals("modifier");
        boolean isActionEffect = selected.category.equals("action_effect");
        boolean isAttribute = !isModifier && !isActionEffect;

        this.modifierTargetButton.visible = isModifier;
        this.modifierTargetButton.active = isModifier;
        this.modifierTargetButton.setMessage(Component.literal(MODIFIER_TARGETS[selected.modifierTargetIndex]));
        this.attributeTargetButton.visible = isAttribute;
        this.attributeTargetButton.active = isAttribute;
        this.attributeTargetButton.setMessage(Component.literal(ATTRIBUTE_TARGETS[selected.attributeTargetIndex]));
        this.playerActionTargetButton.visible = isActionEffect;
        this.playerActionTargetButton.active = isActionEffect;
        this.playerActionTargetButton.setMessage(Component.literal(PLAYER_ACTION_TARGETS[selected.playerActionTargetIndex]));

        this.operationButton.visible = !isActionEffect;
        this.operationButton.active = !isActionEffect;

        boolean usesTag = isModifier && MODIFIER_TARGETS[selected.modifierTargetIndex].equals("ITEM_TAG_BASE_VALUE");
        this.itemTagBox.visible = usesTag;
        this.itemTagBox.active = usesTag;
        this.itemTagBox.setValue(selected.itemTag);
    }

    private void selectNode(DraftNode node) {
        this.selected = node;
        this.selectedNodes.clear();
        if (node != null) this.selectedNodes.add(node);
        refreshSidebarFromSelection();
    }

    private void toggleSelection(DraftNode node) {
        if (this.selectedNodes.contains(node)) {
            this.selectedNodes.remove(node);
            if (this.selected == node) {
                this.selected = this.selectedNodes.isEmpty() ? null : this.selectedNodes.iterator().next();
            }
        } else {
            this.selectedNodes.add(node);
            this.selected = node;
        }
        refreshSidebarFromSelection();
    }

    private void beginDrag(double mouseX, double mouseY) {
        this.dragStartMouseX = mouseX;
        this.dragStartMouseY = mouseY;
        this.dragStartPositions.clear();
        for (DraftNode node : this.selectedNodes) {
            this.dragStartPositions.put(node, new double[]{node.x, node.y});
        }
    }

    private void createNode() {
        DraftNode node = new DraftNode(nextLocalId++, "node_" + (nextNodeNumber++));
        node.x = (nodeCascade % 5) * 90 - 180;
        node.y = (nodeCascade / 5) * 60 - 60;
        nodeCascade++;
        nodes.add(node);
        selectNode(node);
    }

    private void duplicateSelected() {
        if (selected == null || selected.manual) return;
        DraftNode copy = new DraftNode(nextLocalId++, selected.id + "_copy");
        copy.x = selected.x + 20;
        copy.y = selected.y + 20;
        copy.parentId = selected.parentId;
        copy.externalParent = selected.externalParent;
        copy.category = selected.category;
        copy.modifierTargetIndex = selected.modifierTargetIndex;
        copy.attributeTargetIndex = selected.attributeTargetIndex;
        copy.playerActionTargetIndex = selected.playerActionTargetIndex;
        copy.operationIndex = selected.operationIndex;
        copy.description = selected.description;
        copy.cost = selected.cost;
        copy.maxPurchases = selected.maxPurchases;
        copy.value = selected.value;
        copy.itemTag = selected.itemTag;
        copy.name = selected.name;
        nodes.add(copy);
        selectNode(copy);
    }

    private void deleteSelected() {
        if (selected == null) return;
        int removedId = selected.localId;
        nodes.remove(selected);
        for (DraftNode node : nodes) {
            if (node.parentId != null && node.parentId == removedId) {
                node.parentId = null;
            }
        }
        selectNode(null);
    }

    private void generate() {
        if (nodes.isEmpty()) return;
        String modId = globalModIdBox.getValue();
        List<SkillTreeNodeData> data = new ArrayList<>();
        for (DraftNode node : nodes) {
            int parentLocalId = -1;
            String externalParent = node.externalParent;
            if (node.parentId != null) {
                DraftNode parentNode = findByLocalId(node.parentId);
                if (parentNode != null && parentNode.manual) {
                    externalParent = parentNode.manualLocation;
                } else if (parentNode != null) {
                    parentLocalId = node.parentId;
                }
            }

            data.add(new SkillTreeNodeData(
                    node.localId,
                    parentLocalId,
                    externalParent,
                    node.category,
                    node.id,
                    modId,
                    node.description,
                    node.cost,
                    node.maxPurchases,
                    node.category.equals("modifier") ? MODIFIER_TARGETS[node.modifierTargetIndex]
                            : node.category.equals("action_effect") ? PLAYER_ACTION_TARGETS[node.playerActionTargetIndex]
                            : ATTRIBUTE_TARGETS[node.attributeTargetIndex],
                    OPERATIONS[node.operationIndex],
                    node.value,
                    node.itemTag,
                    node.name,
                    node.manual,
                    node.x,
                    node.y
            ));
        }
        PacketDistributor.sendToServer(new GenerateSkillTreeCodePacket(data, includeLang, jsonExport, tree));
    }

    private static boolean isTickDurationTarget(DraftNode node) {
        if (!node.category.equals("modifier")) return false;
        String target = MODIFIER_TARGETS[node.modifierTargetIndex];
        return target.equals("CONTRACT_REFRESH_SPEED") || target.equals("EFFECT_TIMER_SPEED")
                || target.equals("PUNISHMENT_DURATION") || target.equals("EFFECT_DURATION");
    }

    private String valueHint(DraftNode node) {
        if (node.category.equals("action_effect")) {
            return "e.g. craftorio:productive";
        }
        if (isTickDurationTarget(node) && OPERATIONS[node.operationIndex].equals("ADD")) {
            return "e.g. 10 (seconds)";
        }
        return "e.g. 0.1";
    }

    private int countRoots() {
        int count = 0;
        for (DraftNode node : nodes) {
            if (node.parentId == null) count++;
        }
        return count;
    }

    private boolean hasManualNode() {
        for (DraftNode node : nodes) {
            if (node.manual) return true;
        }
        return false;
    }

    private boolean isInCanvas(double mouseX, double mouseY) {
        return mouseX >= canvasLeft && mouseX <= canvasLeft + canvasWidth && mouseY >= canvasTop && mouseY <= canvasTop + canvasHeight;
    }

    private double canvasCenterX() {
        return canvasLeft + canvasWidth / 2.0;
    }

    private double canvasCenterY() {
        return canvasTop + canvasHeight / 2.0;
    }

    private double nodeScreenX(DraftNode node) {
        return canvasCenterX() + panX + node.x * zoom;
    }

    private double nodeScreenY(DraftNode node) {
        return canvasCenterY() + panY + node.y * zoom;
    }

    private int nodeWidth() {
        return (int) Math.round(NODE_W * zoom);
    }

    private int nodeHeight() {
        return (int) Math.round(NODE_H * zoom);
    }

    private DraftNode findNodeByNub(double mouseX, double mouseY) {
        int w = nodeWidth();
        int h = nodeHeight();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            DraftNode node = nodes.get(i);
            double nubX = nodeScreenX(node) + w / 2.0;
            double nubY = nodeScreenY(node) + h + NUB_GAP;
            if (Math.abs(mouseX - nubX) <= 5 && Math.abs(mouseY - nubY) <= 5) return node;
        }
        return null;
    }

    private DraftNode findNodeByBody(double mouseX, double mouseY) {
        int w = nodeWidth();
        int h = nodeHeight();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            DraftNode node = nodes.get(i);
            double x = nodeScreenX(node);
            double y = nodeScreenY(node);
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) return node;
        }
        return null;
    }

    private DraftNode findByLocalId(int localId) {
        for (DraftNode node : nodes) {
            if (node.localId == localId) return node;
        }
        return null;
    }

    private boolean createsCycle(DraftNode source, DraftNode target) {
        Set<Integer> seen = new HashSet<>();
        DraftNode cur = target;
        while (cur != null) {
            if (cur.localId == source.localId) return true;
            if (!seen.add(cur.localId)) return true;
            cur = cur.parentId == null ? null : findByLocalId(cur.parentId);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (!isInCanvas(mouseX, mouseY)) return false;

        if (button == 1) {
            this.panning = true;
            this.panStartMouseX = mouseX;
            this.panStartMouseY = mouseY;
            this.panStartX = panX;
            this.panStartY = panY;
            return true;
        }

        if (button != 0) return false;

        if (isPicking()) {
            DraftNode picked = findNodeByBody(mouseX, mouseY);
            if (picked != null && !picked.manual && picked.location != null) {
                this.pickCallback.accept(picked.location);
            }
            return true;
        }

        DraftNode hitNub = findNodeByNub(mouseX, mouseY);
        if (hitNub != null) {
            this.linkingFrom = hitNub;
            this.linkCursorX = mouseX;
            this.linkCursorY = mouseY;
            return true;
        }

        DraftNode hitBody = findNodeByBody(mouseX, mouseY);
        if (hitBody != null) {
            if (Screen.hasShiftDown()) {
                toggleSelection(hitBody);
            } else if (!this.selectedNodes.contains(hitBody)) {
                selectNode(hitBody);
            } else {
                this.selected = hitBody;
                refreshSidebarFromSelection();
            }
            if (this.selectedNodes.contains(hitBody)) {
                beginDrag(mouseX, mouseY);
            }
            return true;
        }

        selectNode(null);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.panning) {
            this.panX = panStartX + (mouseX - panStartMouseX);
            this.panY = panStartY + (mouseY - panStartMouseY);
            return true;
        }
        if (!this.dragStartPositions.isEmpty()) {
            double dx = (mouseX - dragStartMouseX) / zoom;
            double dy = (mouseY - dragStartMouseY) / zoom;
            for (Map.Entry<DraftNode, double[]> entry : this.dragStartPositions.entrySet()) {
                DraftNode node = entry.getKey();
                if (node.lockedRoot) continue;
                double[] start = entry.getValue();
                node.x = start[0] + dx;
                node.y = start[1] + dy;
            }
            return true;
        }
        if (this.linkingFrom != null) {
            this.linkCursorX = mouseX;
            this.linkCursorY = mouseY;
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
        if (!this.dragStartPositions.isEmpty() && button == 0) {
            this.dragStartPositions.clear();
            return true;
        }
        if (this.linkingFrom != null && button == 0) {
            DraftNode target = findNodeByBody(mouseX, mouseY);
            if (target != null && target != linkingFrom && !createsCycle(linkingFrom, target)) {
                linkingFrom.parentId = target.localId;
            }
            this.linkingFrom = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInCanvas(mouseX, mouseY) && scrollY != 0) {
            double factor = scrollY > 0 ? 1.1 : (1.0 / 1.1);
            this.zoom = Mth.clamp(this.zoom * factor, MIN_ZOOM, MAX_ZOOM);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void drawNode(GuiGraphics graphics, DraftNode node, boolean isSelected) {
        int x = (int) Math.round(nodeScreenX(node));
        int y = (int) Math.round(nodeScreenY(node));
        int w = nodeWidth();
        int h = nodeHeight();
        boolean isModifier = node.category.equals("modifier");
        boolean isActionEffect = node.category.equals("action_effect");
        boolean isRoot = node.parentId == null;
        int fillColor = node.manual ? 0xFF3A3A3A : (isModifier ? 0xFF2A3A5A : (isActionEffect ? 0xFF3A5A3A : 0xFF5A3A2A));
        int borderColor = isSelected ? 0xFFFFFF55 : (node.manual ? 0xFF888888 : (isRoot ? 0xFF55AAFF : 0xFF808080));

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, borderColor);
        graphics.fill(x, y, x + w, y + h, fillColor);

        String idLabel = node.id.isEmpty() ? "?" : node.id;
        graphics.drawCenteredString(this.font, idLabel, x + w / 2, y + 3, 0xFFFFFF);
        String targetLabel = node.manual ? "MANUAL"
                : isModifier ? MODIFIER_TARGETS[node.modifierTargetIndex]
                : isActionEffect ? PLAYER_ACTION_TARGETS[node.playerActionTargetIndex]
                : ATTRIBUTE_TARGETS[node.attributeTargetIndex];
        graphics.drawCenteredString(this.font, targetLabel, x + w / 2, y + 14, node.manual ? 0xFFAA55 : 0xAAAAAA);

        if (isRoot && !node.manual) {
            graphics.drawString(this.font, "R", x + 2, y + 1, 0xFF55AAFF, true);
        }

        if (isPicking()) return;

        int nubX = x + w / 2;
        int nubY = y + h + NUB_GAP;
        int nubColor = node.parentId != null ? 0xFF55FF55 : 0xFFFFFFFF;
        graphics.fill(nubX - 3, nubY - 3, nubX + 3, nubY + 3, nubColor);
    }

    private void drawLink(GuiGraphics graphics, DraftNode parentNode, DraftNode child, int color) {
        int w = nodeWidth();
        int h = nodeHeight();
        double x1 = nodeScreenX(parentNode) + w / 2.0;
        double y1 = nodeScreenY(parentNode) + h / 2.0;
        double x2 = nodeScreenX(child) + w / 2.0;
        double y2 = nodeScreenY(child) + h / 2.0;
        drawLineRaw(graphics, x1, y1, x2, y2, color);
    }

    private void drawLinkToPoint(GuiGraphics graphics, DraftNode from, double toX, double toY, int color) {
        int w = nodeWidth();
        int h = nodeHeight();
        double x1 = nodeScreenX(from) + w / 2.0;
        double y1 = nodeScreenY(from) + h / 2.0;
        drawLineRaw(graphics, x1, y1, toX, toY, color);
    }

    private void drawLineRaw(GuiGraphics graphics, double x1, double y1, double x2, double y2, int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 0.5) return;

        float angle = (float) Math.atan2(dy, dx);

        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0);
        graphics.pose().mulPose(Axis.ZP.rotation(angle));
        graphics.fill(0, -1, (int) Math.round(length), 1, color);
        graphics.pose().popPose();
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF101010);
        CraftorioStarfield.render(graphics, this.width, this.height, panX, panY, zoom, 0x30FF5D);
        graphics.drawCenteredString(this.font, isPicking() ? Component.translatable("misc.craftorio.dev_tools_pick_upgrade") : this.title, this.width / 2, 8, 0xFFFFFF);

        for (DraftNode node : nodes) {
            if (node.lockedRoot) {
                node.x = 0.0;
                node.y = 0.0;
            }
        }

        int bannerY = canvasTop - 11;
        if (hasManualNode() && !isPicking()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_manual_reminder"),
                    this.width / 2, bannerY, 0xFFFFAA55);
            bannerY -= 11;
        }

        int rootCount = countRoots();
        if (rootCount > 1 && !isPicking()) {
            graphics.drawCenteredString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_multiple_roots", rootCount),
                    this.width / 2, bannerY, 0xFFFF5555);
        }

        graphics.fill(canvasLeft, canvasTop, canvasLeft + canvasWidth, canvasTop + canvasHeight, 0xFF000000);
        graphics.renderOutline(canvasLeft, canvasTop, canvasWidth, canvasHeight, 0xFF808080);

        graphics.enableScissor(canvasLeft, canvasTop, canvasLeft + canvasWidth, canvasTop + canvasHeight);
        for (DraftNode node : nodes) {
            if (node.parentId != null) {
                DraftNode parentNode = findByLocalId(node.parentId);
                if (parentNode != null) drawLink(graphics, parentNode, node, 0xFFAAAAAA);
            }
        }
        if (linkingFrom != null) {
            drawLinkToPoint(graphics, linkingFrom, linkCursorX, linkCursorY, 0xFFFFFF55);
        }
        DraftNode hoveredNode = isPicking() && isInCanvas(mouseX, mouseY) ? findNodeByBody(mouseX, mouseY) : null;
        for (DraftNode node : nodes) {
            drawNode(graphics, node, selectedNodes.contains(node) || node == hoveredNode);
        }
        graphics.disableScissor();

        if (isPicking()) {
            renderPicking(graphics, mouseX, mouseY, partialTick);
            return;
        }

        graphics.fill(sidebarLeft - 4, sidebarTop - 4, sidebarLeft + sidebarWidth + 4, this.height - 60, 0xE0202020);

        int labelX = sidebarLeft + 6;
        int rowHeight = 20;
        int y = sidebarTop;
        graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_mod_id"), labelX, y + 4, 0xAAAAAA, false);
        y += rowHeight;
        graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_include_lang"), labelX, y + 4, 0xAAAAAA, false);
        y += rowHeight;

        if (selected == null) {
            graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_hint_select"), labelX, y + 4, 0xAAAAAA, false);
        } else if (selected.manual) {
            graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_manual_upgrade_1", selected.id), labelX, y + 4, 0xFFAA55, false);
            y += rowHeight;
            graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_manual_upgrade_2"), labelX, y + 4, 0xFFAA55, false);
            y += rowHeight;

            if (includeLang) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_description"), labelX, y + 4, 0xAAAAAA, false);
            }
            y += rowHeight;

            graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_cost"), labelX, y + 4, 0xAAAAAA, false);
            y += rowHeight;

            y += rowHeight;
            y += rowHeight;
            graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_manual_upgrade_3"), labelX, y + 4, 0xFFAA55, false);
            y += rowHeight;
            y += rowHeight * 2;

            if (includeLang) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_name"), labelX, y + 4, 0xAAAAAA, false);
            }
            y += rowHeight;

            if (selected.parentId != null) {
                DraftNode parentNode = findByLocalId(selected.parentId);
                String parentLabel = parentNode == null ? "?" : parentNode.id;
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_linked_to", parentLabel), labelX, y + 4, 0xAAAAAA, false);
            } else {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_parent"), labelX, y + 4, 0xAAAAAA, false);
            }
        } else {
            String[] labelKeys = {"dev_tools_label_type", "dev_tools_label_id"};
            for (String key : labelKeys) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio." + key), labelX, y + 4, 0xAAAAAA, false);
                y += rowHeight;
            }

            if (includeLang) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_description"), labelX, y + 4, 0xAAAAAA, false);
            }
            y += rowHeight;

            String targetLabelKey = selected.category.equals("modifier") ? "dev_tools_label_target"
                    : selected.category.equals("action_effect") ? "dev_tools_label_target_action"
                    : "dev_tools_label_target_attribute";
            String[] moreLabelKeys = {
                    "dev_tools_label_cost", "dev_tools_label_max_purchases", targetLabelKey,
                    "dev_tools_label_operation", "dev_tools_label_value"
            };
            boolean isActionEffectSelected = selected.category.equals("action_effect");
            for (String key : moreLabelKeys) {
                if (key.equals("dev_tools_label_operation") && isActionEffectSelected) {
                    y += rowHeight;
                    continue;
                }
                graphics.drawString(this.font, Component.translatable("misc.craftorio." + key), labelX, y + 4, 0xAAAAAA, false);
                y += rowHeight;
            }

            boolean usesTag = selected.category.equals("modifier") && MODIFIER_TARGETS[selected.modifierTargetIndex].equals("ITEM_TAG_BASE_VALUE");
            if (usesTag) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_item_tag_target"), labelX, y + 4, 0xAAAAAA, false);
            }
            y += rowHeight;

            if (includeLang) {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_name"), labelX, y + 4, 0xAAAAAA, false);
            }
            y += rowHeight;

            if (selected.parentId != null) {
                DraftNode parentNode = findByLocalId(selected.parentId);
                String parentLabel = parentNode == null ? "?" : parentNode.id;
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_skill_tree_linked_to", parentLabel), labelX, y + 4, 0xAAAAAA, false);
            } else {
                graphics.drawString(this.font, Component.translatable("misc.craftorio.dev_tools_label_parent"), labelX, y + 4, 0xAAAAAA, false);
            }
        }

        if (statusMessage != null) {
            int statusColor = statusSuccess ? 0xFF55FF55 : 0xFFFF5555;
            graphics.drawCenteredString(this.font, statusMessage, this.width / 2, canvasTop + canvasHeight + 2, statusColor);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.globalModIdBox, "e.g. yourmodid");

        if (selected != null) {
            if (this.descriptionBox.visible) {
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.descriptionBox, "e.g. My upgrade.");
            }
            CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.costBox, "e.g. 1000 or 1e6");
            if (this.maxPurchasesBox.visible) {
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.maxPurchasesBox, "e.g. 1");
            }
            if (this.nameBox.visible) {
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.nameBox, "e.g. My Upgrade");
            }
            if (this.externalParentBox.visible) {
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.externalParentBox, "e.g. craftorio:root");
            }
            if (!selected.manual) {
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.idBox, "e.g. my_node");
                CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.valueBox, valueHint(selected));
                if (this.itemTagBox.visible) {
                    CraftorioMisc.CraftorioTextEffects.drawEditBoxHint(graphics, this.font, this.itemTagBox, "e.g. craftorio:copper");
                }
            }
        }

        this.helpPanel.render(graphics, this.font, this.width, 30, true, List.of(
                Component.translatable("misc.craftorio.dev_tools_skill_tree_help_1"),
                Component.translatable("misc.craftorio.dev_tools_skill_tree_help_2"),
                Component.translatable("misc.craftorio.dev_tools_skill_tree_help_3"),
                Component.translatable("misc.craftorio.dev_tools_skill_tree_help_4")
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class DraftNode {
        final int localId;
        double x, y;
        Integer parentId;
        String category = "modifier";
        int modifierTargetIndex = 0;
        int attributeTargetIndex = 0;
        int playerActionTargetIndex = 0;
        int operationIndex = 0;
        String id;
        String description = "";
        String cost = "1000";
        String maxPurchases = "1";
        String value = "0.1";
        String itemTag = "";
        String name = "";
        String externalParent = "craftorio:root";
        boolean manual = false;
        String manualLocation = "";
        ResourceLocation location;
        boolean lockedRoot = false;
        boolean hasStoredPosition = false;

        DraftNode(int localId, String id) {
            this.localId = localId;
            this.id = id;
        }
    }
}

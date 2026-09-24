package org.crimsoncrips.craftorio.networking.devtools;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.devtools.CraftorioDevTools;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.target.AttributeTarget;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.target.PlayerActionTarget;
import org.crimsoncrips.craftorio.skill_tree.target.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioActionEffectUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record GenerateSkillTreeCodePacket(List<SkillTreeNodeData> nodes, boolean includeLang, boolean jsonExport, boolean rebirth) implements CustomPacketPayload {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_icon.png");

    public static final Type<GenerateSkillTreeCodePacket> TYPE = new Type<>(Craftorio.prefix("generate_skill_tree_code_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateSkillTreeCodePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> {
                SkillTreeNodeData.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, message.nodes());
                ByteBufCodecs.BOOL.encode(buffer, message.includeLang());
                ByteBufCodecs.BOOL.encode(buffer, message.jsonExport());
                ByteBufCodecs.BOOL.encode(buffer, message.rebirth());
            },
            buffer -> new GenerateSkillTreeCodePacket(
                    SkillTreeNodeData.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GenerateSkillTreeCodePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            Map<Integer, SkillTreeNodeData> byLocalId = new LinkedHashMap<>();
            for (SkillTreeNodeData node : message.nodes()) {
                if (!sanitize(node.id()).isEmpty()) {
                    byLocalId.put(node.localId(), node);
                }
            }

            if (byLocalId.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
                PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeGenerateResultPacket(false, "dev_tools_generate_failed", ""));
                return;
            }

            List<SkillTreeNodeData> order = topologicalOrder(byLocalId);

            Map<String, String> langEntries = new LinkedHashMap<>();
            if (message.includeLang()) {
                for (SkillTreeNodeData node : order) {
                    collectLangEntries(node, langEntries);
                }
            }

            if (message.jsonExport()) {
                Map<String, String> bundle = new LinkedHashMap<>();
                for (SkillTreeNodeData node : order) {
                    if (node.manual()) {
                        bundle.put("upgrade_" + sanitize(node.id()) + "_MANUAL_REFERENCE.json", buildManualReferenceJson(node, byLocalId));
                        continue;
                    }
                    CraftorioUpgrade upgrade = buildUpgrade(node, byLocalId);
                    CraftorioUpgrade.dispatchCodec().encodeStart(JsonOps.INSTANCE, upgrade).resultOrPartial(Craftorio.LOGGER::error)
                            .ifPresent(json -> bundle.put("upgrade_" + sanitize(node.id()) + ".json", CraftorioDevTools.toPrettyJson(json)));
                }
                if (langEntries.isEmpty()) {
                    for (Map.Entry<String, String> entry : bundle.entrySet()) {
                        CraftorioDevTools.writeFile(serverPlayer, entry.getKey().replace(".json", ""), entry.getValue(), "json");
                    }
                } else {
                    bundle.put("lang_en_us.json", CraftorioDevTools.buildLangJson(langEntries));
                    CraftorioDevTools.writeBundle(serverPlayer, "skill_tree", bundle);
                }
                PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeGenerateResultPacket(true, "dev_tools_skill_tree_generated_json", String.valueOf(order.size())));
                return;
            }

            StringBuilder code = new StringBuilder();
            Set<Integer> emitted = new HashSet<>();
            for (SkillTreeNodeData node : order) {
                if (node.manual()) {
                    appendManualNodeCode(code, node, byLocalId);
                    continue;
                }
                appendNodeCode(code, node, byLocalId, emitted);
                emitted.add(node.localId());
            }

            code.append("\n// Requires: import net.minecraft.core.Holder; import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;\n");

            if (!langEntries.isEmpty()) {
                code.append("\n// Add to your LanguageProvider's addTranslations(...):\n");
                for (Map.Entry<String, String> entry : langEntries.entrySet()) {
                    code.append("this.add(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\");\n");
                }
            }

            CraftorioDevTools.writeCodeFile(serverPlayer, "skill_tree", message.rebirth() ? code.toString().replace(".save(context,", ".saveRebirth(context,") : code.toString());
            PacketDistributor.sendToPlayer(serverPlayer, new SkillTreeGenerateResultPacket(true, "dev_tools_skill_tree_generated_code", String.valueOf(order.size())));
        });
    }

    private static List<SkillTreeNodeData> topologicalOrder(Map<Integer, SkillTreeNodeData> byLocalId) {
        List<SkillTreeNodeData> ordered = new ArrayList<>();
        Set<Integer> emitted = new HashSet<>();
        List<SkillTreeNodeData> remaining = new ArrayList<>(byLocalId.values());

        while (!remaining.isEmpty()) {
            boolean progress = false;
            Iterator<SkillTreeNodeData> it = remaining.iterator();
            while (it.hasNext()) {
                SkillTreeNodeData node = it.next();
                boolean hasInternalParent = node.parentLocalId() >= 0 && byLocalId.containsKey(node.parentLocalId());
                if (!hasInternalParent || emitted.contains(node.parentLocalId())) {
                    ordered.add(node);
                    emitted.add(node.localId());
                    it.remove();
                    progress = true;
                }
            }
            if (!progress) {
                ordered.addAll(remaining);
                remaining.clear();
            }
        }
        return ordered;
    }

    private static CraftorioUpgrade buildUpgrade(SkillTreeNodeData node, Map<Integer, SkillTreeNodeData> byLocalId) {
        String id = sanitize(node.id());
        String modId = sanitize(node.modId()).isEmpty() ? "yourmodid" : sanitize(node.modId());
        String cost = sanitize(node.cost()).isEmpty() ? "1000" : sanitize(node.cost());
        int maxPurchases = Math.max(1, parseInt(node.maxPurchases(), 1));
        double value = parseDouble(node.value(), 0.1);
        if (node.operation().equals("ADD") && isTickDurationTarget(node.category(), node.target())) {
            value *= CraftorioMisc.SECONDS_TO_TICKS;
        }
        ResourceLocation ownLocation = ResourceLocation.fromNamespaceAndPath(modId, id);
        Optional<ResourceLocation> parentLocation = resolveParentLocation(node, byLocalId, ownLocation);
        UpgradeOperation operationEnum = UpgradeOperation.valueOf(node.operation());

        CraftorioUpgrade.Builder builder = CraftorioUpgrade.builder()
                .name("misc." + modId + ".upgrade_" + id)
                .icon(DEFAULT_ICON)
                .description("misc." + modId + ".upgrade_" + id + "_description")
                .cost(CraftorioMisc.scientificToInt(cost))
                .maxPurchases(maxPurchases)
                .position(parentLocation.isPresent() ? node.x() : 0.0, parentLocation.isPresent() ? node.y() : 0.0);
        parentLocation.ifPresent(builder::parent);

        if (node.category().equals("modifier")) {
            ModifierTarget targetEnum = ModifierTarget.valueOf(node.target());
            if (targetEnum == ModifierTarget.ITEM_TAG_BASE_VALUE) {
                String tagId = sanitize(node.itemTag()).isEmpty() ? "craftorio:copper" : sanitize(node.itemTag());
                return CraftorioModifierUpgrade.of(builder, targetEnum, operationEnum, value,
                        TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId)));
            }
            return CraftorioModifierUpgrade.of(builder, targetEnum, operationEnum, value);
        }

        if (node.category().equals("action_effect")) {
            PlayerActionTarget targetEnum = PlayerActionTarget.valueOf(node.target());
            String effectId = sanitize(node.value()).isEmpty() ? "craftorio:productive" : sanitize(node.value());
            return CraftorioActionEffectUpgrade.of(builder, targetEnum, ResourceLocation.parse(effectId));
        }

        AttributeTarget targetEnum = AttributeTarget.valueOf(node.target());
        return CraftorioAttributeUpgrade.of(builder, targetEnum, operationEnum, value);
    }

    private static String buildManualReferenceJson(SkillTreeNodeData node, Map<Integer, SkillTreeNodeData> byLocalId) {
        String id = sanitize(node.id());
        String modId = sanitize(node.modId()).isEmpty() ? "yourmodid" : sanitize(node.modId());
        String cost = sanitize(node.cost()).isEmpty() ? "1000" : sanitize(node.cost());
        ResourceLocation ownLocation = ResourceLocation.fromNamespaceAndPath(modId, id);
        Optional<ResourceLocation> parentLocation = resolveParentLocation(node, byLocalId, ownLocation);

        JsonObject json = new JsonObject();
        json.addProperty("type", "TODO_your_manual_upgrade_type");
        parentLocation.ifPresent(loc -> json.addProperty("parent", loc.toString()));
        json.addProperty("cost", CraftorioMisc.scientificToInt(cost).toString());
        json.addProperty("description", "misc." + modId + ".upgrade_" + id + "_description");
        json.addProperty("icon", DEFAULT_ICON.toString());
        json.addProperty("name", "misc." + modId + ".upgrade_" + id);
        json.addProperty("x", parentLocation.isPresent() ? node.x() : 0.0);
        json.addProperty("y", parentLocation.isPresent() ? node.y() : 0.0);
        return CraftorioDevTools.toPrettyJson(json);
    }

    private static void appendManualNodeCode(StringBuilder code, SkillTreeNodeData node, Map<Integer, SkillTreeNodeData> byLocalId) {
        String id = sanitize(node.id());
        String modId = sanitize(node.modId()).isEmpty() ? "yourmodid" : sanitize(node.modId());
        String cost = sanitize(node.cost()).isEmpty() ? "1000" : sanitize(node.cost());
        ResourceLocation ownLocation = ResourceLocation.fromNamespaceAndPath(modId, id);
        Optional<ResourceLocation> parentLocation = resolveParentLocation(node, byLocalId, ownLocation);

        double posX = parentLocation.isPresent() ? node.x() : 0.0;
        double posY = parentLocation.isPresent() ? node.y() : 0.0;

        code.append("// Manual upgrade - replace the factory below with your existing class, e.g. YourManualUpgrade::of\n");
        code.append("CraftorioUpgrade.builder()\n");
        code.append("        .name(\"misc.").append(modId).append(".upgrade_").append(id).append("\")\n");
        code.append("        .icon(DEFAULT_ICON)\n");
        parentLocation.ifPresent(loc -> code.append("        .parent(ResourceLocation.parse(\"").append(loc).append("\"))\n"));
        code.append("        .description(\"misc.").append(modId).append(".upgrade_").append(id).append("_description\")\n");
        code.append("        .cost(scientificToInt(\"").append(cost).append("\"))\n");
        code.append("        .position(").append(posX).append(", ").append(posY).append(")\n");
        code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
        code.append("                b -> /* TODO: your manual upgrade factory */ null);\n");
        code.append("\n");
    }

    private static void collectLangEntries(SkillTreeNodeData node, Map<String, String> out) {
        String id = sanitize(node.id());
        String modId = sanitize(node.modId()).isEmpty() ? "yourmodid" : sanitize(node.modId());
        if (!sanitize(node.name()).isEmpty()) {
            out.put("misc." + modId + ".upgrade_" + id, sanitize(node.name()));
        }
        if (!sanitize(node.description()).isEmpty()) {
            out.put("misc." + modId + ".upgrade_" + id + "_description", sanitize(node.description()));
        }
    }

    private static Optional<ResourceLocation> resolveParentLocation(SkillTreeNodeData node, Map<Integer, SkillTreeNodeData> byLocalId, ResourceLocation ownLocation) {
        ResourceLocation resolved;
        if (node.parentLocalId() >= 0) {
            SkillTreeNodeData parent = byLocalId.get(node.parentLocalId());
            if (parent == null) return Optional.empty();
            String parentModId = sanitize(parent.modId()).isEmpty() ? "yourmodid" : sanitize(parent.modId());
            resolved = ResourceLocation.fromNamespaceAndPath(parentModId, sanitize(parent.id()));
        } else {
            String external = sanitize(node.externalParent());
            resolved = ResourceLocation.parse(external.isEmpty() ? "craftorio:root" : external);
        }
        return resolved.equals(ownLocation) ? Optional.empty() : Optional.of(resolved);
    }

    private static void appendNodeCode(StringBuilder code, SkillTreeNodeData node, Map<Integer, SkillTreeNodeData> byLocalId, Set<Integer> emittedSoFar) {
        String id = sanitize(node.id());
        String modId = sanitize(node.modId()).isEmpty() ? "yourmodid" : sanitize(node.modId());
        String cost = sanitize(node.cost()).isEmpty() ? "1000" : sanitize(node.cost());
        int maxPurchases = Math.max(1, parseInt(node.maxPurchases(), 1));
        double value = parseDouble(node.value(), 0.1);
        if (node.operation().equals("ADD") && isTickDurationTarget(node.category(), node.target())) {
            value *= CraftorioMisc.SECONDS_TO_TICKS;
        }
        String varName = toCamelCase(id) + "Upgrade";
        ResourceLocation ownLocation = ResourceLocation.fromNamespaceAndPath(modId, id);

        boolean internalParent = node.parentLocalId() >= 0 && emittedSoFar.contains(node.parentLocalId());
        String parentExpr = null;
        if (internalParent) {
            SkillTreeNodeData parentNode = byLocalId.get(node.parentLocalId());
            String parentModId = sanitize(parentNode.modId()).isEmpty() ? "yourmodid" : sanitize(parentNode.modId());
            ResourceLocation parentLocation = ResourceLocation.fromNamespaceAndPath(parentModId, sanitize(parentNode.id()));
            if (!parentLocation.equals(ownLocation)) {
                parentExpr = toCamelCase(sanitize(parentNode.id())) + "Upgrade";
            }
        } else {
            String external = sanitize(node.externalParent());
            String resolvedExternal = external.isEmpty() ? "craftorio:root" : external;
            if (!ResourceLocation.parse(resolvedExternal).equals(ownLocation)) {
                parentExpr = "ResourceLocation.parse(\"" + resolvedExternal + "\")";
            }
        }

        double posX = parentExpr != null ? node.x() : 0.0;
        double posY = parentExpr != null ? node.y() : 0.0;

        code.append("Holder.Reference<CraftorioUpgrade> ").append(varName).append(" = CraftorioUpgrade.builder()\n");
        code.append("        .name(\"misc.").append(modId).append(".upgrade_").append(id).append("\")\n");
        code.append("        .icon(DEFAULT_ICON)\n");
        if (parentExpr != null) {
            code.append("        .parent(").append(parentExpr).append(")\n");
        }
        code.append("        .description(\"misc.").append(modId).append(".upgrade_").append(id).append("_description\")\n");
        code.append("        .cost(scientificToInt(\"").append(cost).append("\"))\n");
        code.append("        .maxPurchases(").append(maxPurchases).append(")\n");
        code.append("        .position(").append(posX).append(", ").append(posY).append(")\n");

        if (node.category().equals("modifier")) {
            if (node.target().equals("ITEM_TAG_BASE_VALUE")) {
                String tagId = sanitize(node.itemTag()).isEmpty() ? "craftorio:copper" : sanitize(node.itemTag());
                code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
                code.append("                b -> CraftorioModifierUpgrade.of(b, ModifierTarget.").append(node.target()).append(", UpgradeOperation.").append(node.operation()).append(", ").append(value).append(", TagKey.create(Registries.ITEM, ResourceLocation.parse(\"").append(tagId).append("\"))));\n");
            } else {
                code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
                code.append("                b -> CraftorioModifierUpgrade.of(b, ModifierTarget.").append(node.target()).append(", UpgradeOperation.").append(node.operation()).append(", ").append(value).append("));\n");
            }
        } else if (node.category().equals("action_effect")) {
            String effectId = sanitize(node.value()).isEmpty() ? "craftorio:productive" : sanitize(node.value());
            code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
            code.append("                b -> CraftorioActionEffectUpgrade.of(b, PlayerActionTarget.").append(node.target()).append(", ResourceLocation.parse(\"").append(effectId).append("\")));\n");
        } else {
            code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
            code.append("                b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.").append(node.target()).append(", UpgradeOperation.").append(node.operation()).append(", ").append(value).append("));\n");
        }
        code.append("\n");
    }

    private static boolean isTickDurationTarget(String category, String target) {
        return category.equals("modifier") && (target.equals("CONTRACT_REFRESH_SPEED") || target.equals("EFFECT_TIMER_SPEED")
                || target.equals("PUNISHMENT_DURATION") || target.equals("EFFECT_DURATION"));
    }

    private static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String toCamelCase(String snakeCase) {
        String[] parts = snakeCase.split("_");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1).toLowerCase());
        }
        return result.toString();
    }
}

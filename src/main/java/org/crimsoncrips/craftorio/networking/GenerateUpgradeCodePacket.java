package org.crimsoncrips.craftorio.networking;

import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.CraftorioDevTools;
import org.crimsoncrips.craftorio.skill_tree.AttributeTarget;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;

import java.util.LinkedHashMap;
import java.util.Map;

public record GenerateUpgradeCodePacket(String category, String id, String modId, String description, String cost, String parent,
                                         String target, String operation, String value, String itemTag,
                                         boolean includeLang, String name, boolean jsonExport) implements CustomPacketPayload {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static final Type<GenerateUpgradeCodePacket> TYPE = new Type<>(Craftorio.prefix("generate_upgrade_code_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateUpgradeCodePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.category());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.id());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.modId());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.description());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.cost());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.parent());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.target());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.operation());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.value());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.itemTag());
                ByteBufCodecs.BOOL.encode(buffer, message.includeLang());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.name());
                ByteBufCodecs.BOOL.encode(buffer, message.jsonExport());
            },
            buffer -> new GenerateUpgradeCodePacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GenerateUpgradeCodePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            String id = sanitize(message.id());
            if (id.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
                return;
            }

            boolean isModifier = message.category().equals("modifier");

            String cost = sanitize(message.cost()).isEmpty() ? "1000" : sanitize(message.cost());
            String parent = sanitize(message.parent()).isEmpty() ? "craftorio:root" : sanitize(message.parent());
            double value = parseDouble(message.value(), 0.1);
            if (message.operation().equals("ADD") && isTickDurationTarget(message.category(), message.target())) {
                value *= CraftorioMisc.SECONDS_TO_TICKS;
            }
            String modId = sanitize(message.modId()).isEmpty() ? "yourmodid" : sanitize(message.modId());
            UpgradeOperation operationEnum = UpgradeOperation.valueOf(message.operation());

            Map<String, String> langEntries = new LinkedHashMap<>();
            if (message.includeLang()) {
                if (!sanitize(message.name()).isEmpty()) {
                    langEntries.put("misc." + modId + ".upgrade_" + id, sanitize(message.name()));
                }
                if (!sanitize(message.description()).isEmpty()) {
                    langEntries.put("misc." + modId + ".upgrade_" + id + "_description", sanitize(message.description()));
                }
            }

            if (message.jsonExport()) {
                CraftorioUpgrade.Builder builder = CraftorioUpgrade.builder()
                        .name("misc." + modId + ".upgrade_" + id)
                        .icon(DEFAULT_ICON)
                        .parent(ResourceLocation.parse(parent))
                        .description("misc." + modId + ".upgrade_" + id + "_description")
                        .cost(CraftorioMisc.scientificToInt(cost));

                CraftorioUpgrade upgrade;
                if (isModifier) {
                    ModifierTarget targetEnum = ModifierTarget.valueOf(message.target());
                    if (targetEnum == ModifierTarget.ITEM_TAG_BASE_VALUE) {
                        String tagId = sanitize(message.itemTag()).isEmpty() ? "craftorio:copper" : sanitize(message.itemTag());
                        upgrade = CraftorioModifierUpgrade.of(builder, targetEnum, operationEnum, value,
                                net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse(tagId)));
                    } else {
                        upgrade = CraftorioModifierUpgrade.of(builder, targetEnum, operationEnum, value);
                    }
                } else {
                    AttributeTarget targetEnum = AttributeTarget.valueOf(message.target());
                    upgrade = CraftorioAttributeUpgrade.of(builder, targetEnum, operationEnum, value);
                }

                CraftorioUpgrade.dispatchCodec().encodeStart(JsonOps.INSTANCE, upgrade).resultOrPartial(Craftorio.LOGGER::error)
                        .ifPresentOrElse(
                                json -> {
                                    if (langEntries.isEmpty()) {
                                        CraftorioDevTools.writeFile(serverPlayer, "upgrade_" + id, CraftorioDevTools.toPrettyJson(json), "json");
                                    } else {
                                        Map<String, String> bundle = new LinkedHashMap<>();
                                        bundle.put("upgrade_" + id + ".json", CraftorioDevTools.toPrettyJson(json));
                                        bundle.put("lang_en_us.json", CraftorioDevTools.buildLangJson(langEntries));
                                        CraftorioDevTools.writeBundle(serverPlayer, "upgrade_" + id, bundle);
                                    }
                                },
                                () -> serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED))
                        );
                return;
            }

            String varName = toCamelCase(id) + "Upgrade";

            StringBuilder code = new StringBuilder();
            code.append("Holder.Reference<CraftorioUpgrade> ").append(varName).append(" = CraftorioUpgrade.builder()\n");
            code.append("        .name(\"misc.").append(modId).append(".upgrade_").append(id).append("\")\n");
            code.append("        .icon(DEFAULT_ICON)\n");
            code.append("        .parent(ResourceLocation.parse(\"").append(parent).append("\"))\n");
            code.append("        .description(\"misc.").append(modId).append(".upgrade_").append(id).append("_description\")\n");
            code.append("        .cost(scientificToInt(\"").append(cost).append("\"))\n");

            if (isModifier) {
                if (message.target().equals("ITEM_TAG_BASE_VALUE")) {
                    String tagId = sanitize(message.itemTag()).isEmpty() ? "craftorio:copper" : sanitize(message.itemTag());
                    code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
                    code.append("                b -> CraftorioModifierUpgrade.of(b, ModifierTarget.").append(message.target()).append(", UpgradeOperation.").append(message.operation()).append(", ").append(value).append(", TagKey.create(Registries.ITEM, ResourceLocation.parse(\"").append(tagId).append("\"))));\n");
                } else {
                    code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
                    code.append("                b -> CraftorioModifierUpgrade.of(b, ModifierTarget.").append(message.target()).append(", UpgradeOperation.").append(message.operation()).append(", ").append(value).append("));\n");
                }
            } else {
                code.append("        .save(context, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\"),\n");
                code.append("                b -> CraftorioAttributeUpgrade.of(b, AttributeTarget.").append(message.target()).append(", UpgradeOperation.").append(message.operation()).append(", ").append(value).append("));\n");
            }

            code.append("\n// Use ").append(varName).append(" as .parent(").append(varName).append(") for upgrades that should branch off this one.\n");
            code.append("// Requires: import net.minecraft.core.Holder; import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;\n");

            if (!langEntries.isEmpty()) {
                code.append("\n// Add to your LanguageProvider's addTranslations(...):\n");
                for (Map.Entry<String, String> entry : langEntries.entrySet()) {
                    code.append("this.add(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\");\n");
                }
            }

            CraftorioDevTools.writeCodeFile(serverPlayer, "upgrade_" + id, code.toString());
        });
    }

    private static boolean isTickDurationTarget(String category, String target) {
        return category.equals("modifier") && (target.equals("CONTRACT_REFRESH_SPEED") || target.equals("EFFECT_TIMER_SPEED")
                || target.equals("PUNISHMENT_DURATION") || target.equals("EFFECT_DURATION"));
    }

    private static String sanitize(String input) {
        return input == null ? "" : input.trim();
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

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}

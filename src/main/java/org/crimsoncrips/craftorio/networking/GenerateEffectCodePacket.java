package org.crimsoncrips.craftorio.networking;

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
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioDevTools;

import java.util.LinkedHashMap;
import java.util.Map;

public record GenerateEffectCodePacket(String effectType, String id, String modId, String multiplier, String seconds, String weight,
                                        boolean unobtainable, String itemTag, boolean includeLang, String name, boolean jsonExport) implements CustomPacketPayload {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static final Type<GenerateEffectCodePacket> TYPE = new Type<>(Craftorio.prefix("generate_effect_code_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateEffectCodePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.effectType());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.id());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.modId());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.multiplier());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.seconds());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.weight());
                ByteBufCodecs.BOOL.encode(buffer, message.unobtainable());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.itemTag());
                ByteBufCodecs.BOOL.encode(buffer, message.includeLang());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.name());
                ByteBufCodecs.BOOL.encode(buffer, message.jsonExport());
            },
            buffer -> new GenerateEffectCodePacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
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

    public static void handle(GenerateEffectCodePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;

            String id = sanitize(message.id());
            if (id.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
                return;
            }

            boolean isTagType = message.effectType().equals("tag");
            boolean hasItemTag = !sanitize(message.itemTag()).isEmpty();
            if (isTagType && !hasItemTag) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_effect_tag_required").withStyle(ChatFormatting.RED));
                return;
            }
            if (!isTagType && hasItemTag) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_effect_tag_not_allowed").withStyle(ChatFormatting.RED));
                return;
            }

            float multiplier = parseFloat(message.multiplier(), 1.0F);
            int seconds = parseInt(message.seconds(), 60);
            int weight = parseInt(message.weight(), 10);
            String nameKey = "registry." + id;
            String modId = sanitize(message.modId()).isEmpty() ? "yourmodid" : sanitize(message.modId());

            Map<String, String> langEntries = new LinkedHashMap<>();
            if (message.includeLang() && !sanitize(message.name()).isEmpty()) {
                langEntries.put(nameKey, sanitize(message.name()));
            }

            if (message.jsonExport()) {
                CraftorioEffects effect = switch (message.effectType()) {
                    case "shop" -> new ShopMultiplierEffect(multiplier, nameKey, seconds, DEFAULT_ICON, weight, message.unobtainable());
                    case "tag" -> {
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(sanitize(message.itemTag())));
                        yield new TagMultiplierEffect(multiplier, nameKey, tag, seconds, DEFAULT_ICON, weight, message.unobtainable());
                    }
                    default -> new GeneralMultiplierEffect(multiplier, nameKey, seconds, DEFAULT_ICON, weight, message.unobtainable());
                };

                CraftorioEffects.dispatchCodec().encodeStart(JsonOps.INSTANCE, effect).resultOrPartial(Craftorio.LOGGER::error)
                        .ifPresentOrElse(
                                json -> {
                                    if (langEntries.isEmpty()) {
                                        CraftorioDevTools.writeFile(serverPlayer, "effect_" + id, CraftorioDevTools.toPrettyJson(json), "json");
                                    } else {
                                        Map<String, String> bundle = new LinkedHashMap<>();
                                        bundle.put("effect_" + id + ".json", CraftorioDevTools.toPrettyJson(json));
                                        bundle.put("lang_en_us.json", CraftorioDevTools.buildLangJson(langEntries));
                                        CraftorioDevTools.writeBundle(serverPlayer, "effect_" + id, bundle);
                                    }
                                },
                                () -> serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED))
                        );
                return;
            }

            String constructorClass = switch (message.effectType()) {
                case "shop" -> "ShopMultiplierEffect";
                case "tag" -> "TagMultiplierEffect";
                default -> "GeneralMultiplierEffect";
            };

            StringBuilder code = new StringBuilder();
            code.append("context.register(\n");
            code.append("        ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\")),\n");

            if (message.effectType().equals("tag")) {
                code.append("        new ").append(constructorClass).append("(").append(multiplier).append("F, \"registry.").append(id).append("\", TagKey.create(Registries.ITEM, ResourceLocation.parse(\"").append(sanitize(message.itemTag())).append("\")), ").append(seconds).append(", DEFAULT_ICON, ").append(weight).append(", ").append(message.unobtainable()).append(")\n");
            } else {
                code.append("        new ").append(constructorClass).append("(").append(multiplier).append("F, \"registry.").append(id).append("\", ").append(seconds).append(", DEFAULT_ICON, ").append(weight).append(", ").append(message.unobtainable()).append(")\n");
            }

            code.append(");\n");

            if (!langEntries.isEmpty()) {
                code.append("\n// Add to your LanguageProvider's addTranslations(...):\n");
                for (Map.Entry<String, String> entry : langEntries.entrySet()) {
                    code.append("this.add(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\");\n");
                }
            }

            CraftorioDevTools.writeCodeFile(serverPlayer, "effect_" + id, code.toString());
        });
    }

    private static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
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
}

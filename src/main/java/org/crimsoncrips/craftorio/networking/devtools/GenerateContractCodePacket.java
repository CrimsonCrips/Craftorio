package org.crimsoncrips.craftorio.networking.devtools;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.RegistryOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;
import org.crimsoncrips.craftorio.item.rune.EffectRune;
import org.crimsoncrips.craftorio.item.rune.EffectRune;
import org.crimsoncrips.craftorio.registries.contract.ContractGoal;
import org.crimsoncrips.craftorio.registries.contract.ContractTextColors;
import org.crimsoncrips.craftorio.registries.contract.ContractType;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItem;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItemReward;
import org.crimsoncrips.craftorio.server.devtools.CraftorioDevTools;
import net.minecraft.resources.ResourceKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record GenerateContractCodePacket(String id, String modId, String seconds, String basePointValue, String weight,
                                          String claimPointThreshold, String minPointThreshold, String maxPointThreshold,
                                          String punishment, String requiredModId, String rewardRandomEffectCount,
                                          boolean includeLang, String title, String description, String cardTexture,
                                          boolean jsonExport, String titleColor, String timeColor,
                                          String descriptionColor, String punishmentColor,
                                          String contractType, String structure, String tagBounty) implements CustomPacketPayload {

    public static final Type<GenerateContractCodePacket> TYPE = new Type<>(Craftorio.prefix("generate_contract_code_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateContractCodePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.id());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.modId());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.seconds());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.basePointValue());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.weight());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.claimPointThreshold());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.minPointThreshold());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.maxPointThreshold());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.punishment());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.requiredModId());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.rewardRandomEffectCount());
                ByteBufCodecs.BOOL.encode(buffer, message.includeLang());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.title());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.description());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.cardTexture());
                ByteBufCodecs.BOOL.encode(buffer, message.jsonExport());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.titleColor());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.timeColor());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.descriptionColor());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.punishmentColor());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.contractType());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.structure());
                ByteBufCodecs.STRING_UTF8.encode(buffer, message.tagBounty());
            },
            buffer -> new GenerateContractCodePacket(
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
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GenerateContractCodePacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return;
            if (!(serverPlayer.containerMenu instanceof ContractCreatorMenu menu)) return;

            String id = sanitize(message.id());
            if (id.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
                return;
            }

            int seconds = parseInt(message.seconds(), 600);
            String basePointValue = sanitize(message.basePointValue()).isEmpty() ? "1000" : sanitize(message.basePointValue());
            int weight = parseInt(message.weight(), 10);
            String claimThreshold = sanitize(message.claimPointThreshold()).isEmpty() ? "0" : sanitize(message.claimPointThreshold());
            String minThreshold = sanitize(message.minPointThreshold()).isEmpty() ? "0" : sanitize(message.minPointThreshold());
            String maxThreshold = sanitize(message.maxPointThreshold()).isEmpty() ? "1000000" : sanitize(message.maxPointThreshold());
            String punishment = sanitize(message.punishment());
            String requiredModId = sanitize(message.requiredModId());
            String cardTexture = sanitize(message.cardTexture());
            int randomEffectCount = parseInt(message.rewardRandomEffectCount(), 0);
            String modId = sanitize(message.modId()).isEmpty() ? "yourmodid" : sanitize(message.modId());
            ContractGoal goal = parseGoal(message.contractType(), sanitize(message.structure()));
            ContractTextColors textColors = new ContractTextColors(
                    ContractTextColors.tryParse(message.titleColor()),
                    ContractTextColors.tryParse(message.timeColor()),
                    ContractTextColors.tryParse(message.descriptionColor()),
                    ContractTextColors.tryParse(message.punishmentColor()));

            Map<String, String> langEntries = new LinkedHashMap<>();
            if (message.includeLang()) {
                if (!sanitize(message.title()).isEmpty()) {
                    langEntries.put("registry." + id + ".title", sanitize(message.title()));
                }
                if (!sanitize(message.description()).isEmpty()) {
                    langEntries.put("registry." + id + ".description", sanitize(message.description()));
                }
            }

            Container container = menu.getContainer();

            if (message.jsonExport()) {
                List<CraftorioContractItem> bountyItems = new ArrayList<>();
                for (int i = 0; goal.type() == ContractType.SINK && i < ContractCreatorMenu.BOUNTY_SLOTS; i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.isEmpty()) continue;
                    bountyItems.add(stack.getComponentsPatch().isEmpty()
                            ? new CraftorioContractItem(stack.getCount(), stack.getItem())
                            : new CraftorioContractItem(stack.getCount(), stack.copyWithCount(1)));
                }

                if (goal.type() == ContractType.SINK) {
                    for (Map.Entry<ResourceLocation, Integer> tagEntry : parseTagBounty(message.tagBounty()).entrySet()) {
                        bountyItems.add(new CraftorioContractItem(tagEntry.getValue(), TagKey.create(Registries.ITEM, tagEntry.getKey())));
                    }
                }

                List<CraftorioContractItemReward> rewardItems = new ArrayList<>();
                for (ItemStack stack : mergedRewards(container)) {
                    rewardItems.add(new CraftorioContractItemReward(stack.getCount(), stack.copyWithCount(1), stack.getItem() instanceof EffectRune ? randomEffectCount : 0));
                }

                Optional<ResourceLocation> punishmentLoc = punishment.isEmpty() ? Optional.empty() : Optional.of(ResourceLocation.parse(punishment));
                Optional<String> requiredModOpt = requiredModId.isEmpty() ? Optional.empty() : Optional.of(requiredModId);
                Optional<ResourceLocation> cardTextureLoc = Optional.ofNullable(cardTexture.isEmpty() ? null : ResourceLocation.tryParse(cardTexture));

                BigInteger basePoints = CraftorioMisc.scientificToInt(basePointValue);
                BigInteger claim = CraftorioMisc.scientificToInt(claimThreshold);
                BigInteger min = CraftorioMisc.scientificToInt(minThreshold);
                BigInteger max = CraftorioMisc.scientificToInt(maxThreshold);

                CraftorioContract contract = new CraftorioContract(bountyItems, id, seconds, basePoints, rewardItems,
                        punishmentLoc, weight, claim, min, max, requiredModOpt, cardTextureLoc, textColors).withGoal(goal);

                CraftorioContract.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, serverPlayer.registryAccess()), contract).resultOrPartial(Craftorio.LOGGER::error)
                        .ifPresentOrElse(
                                json -> {
                                    if (langEntries.isEmpty()) {
                                        CraftorioDevTools.writeFile(serverPlayer, "contract_" + id, CraftorioDevTools.toPrettyJson(json), "json");
                                    } else {
                                        Map<String, String> bundle = new LinkedHashMap<>();
                                        bundle.put("contract_" + id + ".json", CraftorioDevTools.toPrettyJson(json));
                                        bundle.put("en_us.json", CraftorioDevTools.buildLangJson(langEntries));
                                        CraftorioDevTools.writeBundle(serverPlayer, "contract_" + id, bundle);
                                    }
                                },
                                () -> serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED))
                        );
                return;
            }

            if (ContractCreatorMenu.hasComponentItems(container)) {
                ContractCreatorMenu.warnComponentItems(serverPlayer);
            }

            StringBuilder bounty = new StringBuilder();
            for (int i = 0; goal.type() == ContractType.SINK && i < ContractCreatorMenu.BOUNTY_SLOTS; i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) continue;
                appendComponentNote(bounty, stack, serverPlayer);
                bounty.append("                        new CraftorioContractItem(").append(stack.getCount())
                        .append(", ").append(itemReferenceExpr(stack.getItem())).append("),\n");
            }
            for (Map.Entry<ResourceLocation, Integer> tagEntry : goal.type() == ContractType.SINK ? parseTagBounty(message.tagBounty()).entrySet() : Map.<ResourceLocation, Integer>of().entrySet()) {
                bounty.append("                        new CraftorioContractItem(").append(tagEntry.getValue())
                        .append(", TagKey.create(Registries.ITEM, ResourceLocation.parse(\"").append(tagEntry.getKey()).append("\"))),\n");
            }
            if (bounty.isEmpty()) {
                bounty.append("                        // no bounty items were placed in the creator's slots\n");
            } else {
                bounty.setLength(bounty.length() - 2);
                bounty.append("\n");
            }

            StringBuilder rewards = new StringBuilder();
            for (ItemStack stack : mergedRewards(container)) {
                appendComponentNote(rewards, stack, serverPlayer);
                rewards.append("                        new CraftorioContractItemReward(").append(stack.getCount())
                        .append(", ").append(itemReferenceExpr(stack.getItem()));
                if (randomEffectCount > 0 && stack.getItem() instanceof EffectRune) {
                    rewards.append(", ").append(randomEffectCount);
                }
                rewards.append("),\n");
            }
            if (rewards.isEmpty()) {
                rewards.append("                        // no reward items were placed in the creator's slots\n");
            } else {
                rewards.setLength(rewards.length() - 2);
                rewards.append("\n");
            }

            String punishmentExpr = punishment.isEmpty() ? "Optional.empty()" : "Optional.of(ResourceLocation.parse(\"" + punishment + "\"))";
            String requiredModIdExpr = requiredModId.isEmpty() ? "Optional.empty()" : "Optional.of(\"" + requiredModId + "\")";
            String cardTextureExpr = cardTexture.isEmpty() ? "Optional.empty()"
                    : "Optional.of(ResourceLocation.parse(\"" + cardTexture + "\"))";

            StringBuilder code = new StringBuilder();
            code.append("context.register(\n");
            code.append("        ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(\"").append(modId).append("\", \"").append(id).append("\")),\n");
            code.append("        new CraftorioContract(\n");
            code.append("                List.of(\n").append(bounty);
            code.append("                ),\n");
            code.append("                \"").append(id).append("\", ").append(seconds).append(", scientificToInt(\"").append(basePointValue).append("\"),\n");
            code.append("                List.of(\n").append(rewards);
            code.append("                ),\n");
            code.append("                ").append(punishmentExpr).append(",\n");
            code.append("                ").append(weight).append(",\n");
            code.append("                scientificToInt(\"").append(claimThreshold).append("\"), scientificToInt(\"").append(minThreshold).append("\"), scientificToInt(\"").append(maxThreshold).append("\"),\n");
            code.append("                ").append(requiredModIdExpr).append(",\n");
            code.append("                ").append(cardTextureExpr).append(textColors.isEmpty() ? "\n" : ",\n");
            if (!textColors.isEmpty()) {
                code.append("                new ContractTextColors(")
                        .append(colorExpr(textColors.title())).append(", ")
                        .append(colorExpr(textColors.time())).append(", ")
                        .append(colorExpr(textColors.description())).append(", ")
                        .append(colorExpr(textColors.punishment())).append(")\n");
            }
            code.append("        )").append(goalExpr(goal)).append("\n");
            code.append(");\n");
            code.append("\n// Requires: import static org.crimsoncrips.craftorio.CraftorioMisc.scientificToInt;\n");
            code.append("// Requires: import static org.crimsoncrips.craftorio.CraftorioMisc.toItem;\n");
            if (!textColors.isEmpty()) {
                code.append("// Requires: import org.crimsoncrips.craftorio.registries.contract.ContractTextColors;\n");
            }
            if (goal.type() != ContractType.SINK) {
                code.append("// Requires: import org.crimsoncrips.craftorio.registries.contract.ContractGoal;\n");
            }

            if (!langEntries.isEmpty()) {
                code.append("\n// Add to CraftorioRegistryLang.addContracts(...):\n");
                String langTitle = langEntries.getOrDefault("registry." + id + ".title", "");
                String langDescription = langEntries.getOrDefault("registry." + id + ".description", "");
                code.append("lang.addContractLang(\"").append(id).append("\", \"").append(langTitle).append("\", \"").append(langDescription).append("\");\n");
            }

            CraftorioDevTools.writeCodeFile(serverPlayer, "contract_" + id, code.toString());
        });
    }

    private static ContractGoal parseGoal(String type, String structure) {
        ContractType contractType;
        try {
            contractType = ContractType.valueOf(type.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            contractType = ContractType.SINK;
        }
        return switch (contractType) {
            case SINK -> ContractGoal.SINK;
            case BUILDING -> {
                ResourceLocation id = ResourceLocation.tryParse(structure);
                yield id == null || structure.isEmpty() ? ContractGoal.SINK : ContractGoal.building(id);
            }
        };
    }

    private static String goalExpr(ContractGoal goal) {
        return switch (goal.type()) {
            case SINK -> "";
            case BUILDING -> ".withGoal(ContractGoal.building(ResourceLocation.parse(\"" + goal.structure().map(Object::toString).orElse("") + "\")))";
        };
    }

    private static String colorExpr(Optional<Integer> color) {
        return color.map(value -> "Optional.of(0x" + ContractTextColors.toHex(value).substring(1) + ")").orElse("Optional.empty()");
    }

    private static void appendComponentNote(StringBuilder code, ItemStack stack, ServerPlayer player) {
        if (stack.getComponentsPatch().isEmpty()) return;
        String components = DataComponentPatch.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, player.registryAccess()), stack.getComponentsPatch())
                .result().map(Object::toString).orElse("?");
        code.append("                        // ").append(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                .append(" has data components that Java export can't include, use JSON export to keep them: ").append(components).append("\n");
    }

    private static Map<ResourceLocation, Integer> parseTagBounty(String encoded) {
        Map<ResourceLocation, Integer> tags = new LinkedHashMap<>();
        for (String part : sanitize(encoded).split(";")) {
            int split = part.indexOf('=');
            if (split <= 0) continue;
            ResourceLocation tag = ResourceLocation.tryParse(part.substring(split + 1).trim());
            int amount = parseInt(part.substring(0, split), 0);
            if (tag != null && amount > 0) {
                tags.merge(tag, amount, Integer::sum);
            }
        }
        return tags;
    }

    private static String itemReferenceExpr(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (item instanceof EffectRune) {
            return "BuiltInRegistries.ITEM.get(ResourceLocation.parse(\"" + itemId + "\"))";
        }
        return "toItem(\"" + itemId + "\")";
    }

    private static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static List<ItemStack> mergedRewards(Container container) {
        List<ItemStack> merged = new ArrayList<>();
        for (int i = 0; i < ContractCreatorMenu.REWARD_SLOTS; i++) {
            ItemStack stack = container.getItem(ContractCreatorMenu.BOUNTY_SLOTS + i);
            if (stack.isEmpty()) continue;

            ItemStack existing = null;
            for (ItemStack candidate : merged) {
                if (ItemStack.isSameItemSameComponents(candidate, stack)) {
                    existing = candidate;
                    break;
                }
            }
            if (existing != null) {
                existing.setCount(existing.getCount() + stack.getCount());
            } else {
                merged.add(stack.copy());
            }
        }
        return merged;
    }
}

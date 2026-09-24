package org.crimsoncrips.craftorio.client.screen.devtools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioActionEffectUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class DevToolsUpgradeTrees {

    public record TreeEntry(ResourceLocation id, CraftorioUpgrade upgrade, int depth) {}

    private DevToolsUpgradeTrees() {}

    public static Registry<CraftorioUpgrade> registry(Minecraft minecraft, boolean rebirth) {
        return minecraft.level.registryAccess().registryOrThrow(rebirth ? CraftorioUpgrade.REBIRTH_REGISTRY_KEY : CraftorioUpgrade.REGISTRY_KEY);
    }

    public static boolean isEditable(CraftorioUpgrade upgrade) {
        return upgrade instanceof CraftorioModifierUpgrade
                || upgrade instanceof CraftorioAttributeUpgrade
                || upgrade instanceof CraftorioActionEffectUpgrade;
    }

    public static List<TreeEntry> orderedEntries(Registry<CraftorioUpgrade> registry) {
        Map<ResourceLocation, CraftorioUpgrade> all = new LinkedHashMap<>();
        for (Holder.Reference<CraftorioUpgrade> holder : registry.holders().toList()) {
            all.put(holder.key().location(), holder.value());
        }

        Map<ResourceLocation, List<ResourceLocation>> children = new LinkedHashMap<>();
        List<ResourceLocation> roots = new ArrayList<>();
        for (Map.Entry<ResourceLocation, CraftorioUpgrade> entry : all.entrySet()) {
            Optional<ResourceLocation> parent = entry.getValue().getParent();
            if (parent.isPresent() && all.containsKey(parent.get()) && !parent.get().equals(entry.getKey())) {
                children.computeIfAbsent(parent.get(), k -> new ArrayList<>()).add(entry.getKey());
            } else {
                roots.add(entry.getKey());
            }
        }

        List<TreeEntry> ordered = new ArrayList<>();
        Set<ResourceLocation> visited = new HashSet<>();
        for (ResourceLocation root : roots) {
            walk(root, 0, all, children, visited, ordered);
        }
        return ordered;
    }

    private static void walk(ResourceLocation id, int depth, Map<ResourceLocation, CraftorioUpgrade> all,
                             Map<ResourceLocation, List<ResourceLocation>> children, Set<ResourceLocation> visited, List<TreeEntry> out) {
        if (!visited.add(id)) return;
        out.add(new TreeEntry(id, all.get(id), depth));
        for (ResourceLocation child : children.getOrDefault(id, List.of())) {
            walk(child, depth + 1, all, children, visited, out);
        }
    }

    public static void openTreePicker(Minecraft minecraft, Screen parent, Consumer<Boolean> onPick) {
        if (minecraft.level == null) return;

        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        options.add(new DevToolsPickerScreen.Option(Component.translatable("misc.craftorio.dev_tools_tree_basic"),
                registry(minecraft, false).size() + " upgrades", 0, true, () -> onPick.accept(false)));
        options.add(new DevToolsPickerScreen.Option(Component.translatable("misc.craftorio.dev_tools_tree_rebirth"),
                registry(minecraft, true).size() + " upgrades", 0, true, () -> onPick.accept(true)));

        minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_tree"), parent, options));
    }

    public static void openUpgradePicker(Minecraft minecraft, Screen parent, boolean rebirth, Consumer<TreeEntry> onPick) {
        if (minecraft.level == null) return;

        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        for (TreeEntry entry : orderedEntries(registry(minecraft, rebirth))) {
            boolean editable = isEditable(entry.upgrade());
            options.add(new DevToolsPickerScreen.Option(
                    Component.literal(Component.translatable(entry.upgrade().getNameKey()).getString()),
                    entry.id().toString(),
                    entry.depth(),
                    editable,
                    () -> onPick.accept(entry)
            ));
        }

        minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_upgrade"), parent, options));
    }
}

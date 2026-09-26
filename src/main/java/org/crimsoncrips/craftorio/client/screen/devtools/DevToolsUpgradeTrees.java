package org.crimsoncrips.craftorio.client.screen.devtools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class DevToolsUpgradeTrees {

    private DevToolsUpgradeTrees() {}

    public static Registry<CraftorioUpgrade> registry(Minecraft minecraft, UpgradeTree tree) {
        return minecraft.level.registryAccess().registryOrThrow(tree.registryKey());
    }

    public static void openTreePicker(Minecraft minecraft, Screen parent, Consumer<UpgradeTree> onPick) {
        if (minecraft.level == null) return;

        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        for (UpgradeTree tree : UpgradeTree.values()) {
            options.add(new DevToolsPickerScreen.Option(Component.translatable(tree.translationKey()),
                    registry(minecraft, tree).size() + " upgrades", 0, true, () -> onPick.accept(tree)));
        }

        minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_tree"), parent, options));
    }
}

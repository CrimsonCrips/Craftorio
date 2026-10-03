package org.crimsoncrips.craftorio.client.screen.devtools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class DevToolsTagPicker {

    private DevToolsTagPicker() {}

    public static void open(Minecraft minecraft, Screen returnTo, Consumer<String> onPick) {
        List<DevToolsPickerScreen.Option> options = new ArrayList<>();
        BuiltInRegistries.ITEM.getTags()
                .sorted(Comparator.comparing(pair -> pair.getFirst().location().toString()))
                .forEach(pair -> {
                    ResourceLocation id = pair.getFirst().location();
                    String detail = Component.translatable("misc.craftorio.dev_tools_tag_items", pair.getSecond().size()).getString();
                    options.add(new DevToolsPickerScreen.Option(Component.literal("#" + id), detail, 0, true, () -> {
                        onPick.accept(id.toString());
                        minecraft.setScreen(returnTo);
                    }));
                });
        minecraft.setScreen(new DevToolsPickerScreen(Component.translatable("misc.craftorio.dev_tools_pick_tag"), returnTo, options));
    }

}

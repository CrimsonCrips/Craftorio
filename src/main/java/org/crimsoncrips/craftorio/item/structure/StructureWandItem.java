package org.crimsoncrips.craftorio.item.structure;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.crimsoncrips.craftorio.item.ScannerStickItem;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;

import java.util.List;

public class StructureWandItem extends ScannerStickItem {

    public StructureWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("misc.craftorio.structure_wand_tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("misc.craftorio.scan_stick_tooltip_usage").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("misc.craftorio.structure_wand_tooltip_menu", Component.keybind("key.craftorio.print_scan")).withStyle(ChatFormatting.GRAY));

        StructureWandSettings settings = stack.getOrDefault(CraftorioDataComponents.STRUCTURE_WAND.get(), StructureWandSettings.DEFAULT);
        if (!settings.name().isEmpty()) {
            tooltip.add(Component.translatable("structure_block.hover.save", settings.name()).withStyle(ChatFormatting.GOLD));
        }

        BlockPos pos1 = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        BlockPos pos2 = stack.get(CraftorioDataComponents.SCAN_POS_2.get());
        if (pos1 != null) {
            tooltip.add(Component.translatable("misc.craftorio.scan_pos_1_label", pos1.getX(), pos1.getY(), pos1.getZ()).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (pos2 != null) {
            tooltip.add(Component.translatable("misc.craftorio.scan_pos_2_label", pos2.getX(), pos2.getY(), pos2.getZ()).withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}

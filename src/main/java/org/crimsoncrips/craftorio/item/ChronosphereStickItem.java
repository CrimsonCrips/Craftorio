package org.crimsoncrips.craftorio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;

import java.util.List;

public class ChronosphereStickItem extends ScannerStickItem {

    public ChronosphereStickItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("misc.craftorio.chronosphere_stick_tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("misc.craftorio.scan_stick_tooltip_usage").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("misc.craftorio.chronosphere_stick_tooltip_activate", Component.keybind("key.craftorio.print_scan")).withStyle(ChatFormatting.GRAY));

        BlockPos pos1 = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        BlockPos pos2 = stack.get(CraftorioDataComponents.SCAN_POS_2.get());
        if (pos1 != null) {
            tooltip.add(Component.translatable("misc.craftorio.scan_pos_1_label", pos1.getX(), pos1.getY(), pos1.getZ()).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (pos2 != null) {
            tooltip.add(Component.translatable("misc.craftorio.scan_pos_2_label", pos2.getX(), pos2.getY(), pos2.getZ()).withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    public static void activate(ServerPlayer player, ItemStack stack) {
        if (!player.canUseGameMasterBlocks()) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.chronosphere_stick_no_permission").withStyle(ChatFormatting.RED));
            return;
        }
        BlockPos pos1 = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        BlockPos pos2 = stack.get(CraftorioDataComponents.SCAN_POS_2.get());
        if (pos1 == null || pos2 == null) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.scan_positions_not_set").withStyle(ChatFormatting.RED));
            return;
        }

        BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));

        long volume = (long) (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        if (volume > MAX_VOLUME) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.scan_area_large_warning", volume, MAX_VOLUME).withStyle(ChatFormatting.RED));
            return;
        }

        CraftorioSchematics.removeArea(player.serverLevel(), min, max);
    }
}

package org.crimsoncrips.craftorio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScannerStickItem extends Item {

    public static final long MAX_VOLUME = 262144L;

    private static final LevelResource SCANS_DIR = new LevelResource(Craftorio.MODID + "/area_scans");

    public ScannerStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        stack.set(CraftorioDataComponents.SCAN_POS_2.get(), pos);
        player.sendSystemMessage(Component.translatable("misc.craftorio.scan_pos_2_set", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));

        return InteractionResult.SUCCESS;
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;

        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ScannerStickItem)) return;

        event.setCanceled(true);

        BlockPos pos = event.getPos();
        stack.set(CraftorioDataComponents.SCAN_POS_1.get(), pos);
        player.sendSystemMessage(Component.translatable("misc.craftorio.scan_pos_1_set", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("misc.craftorio.scan_stick_tooltip").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("misc.craftorio.scan_stick_tooltip_usage").withStyle(ChatFormatting.GRAY));

        BlockPos pos1 = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        BlockPos pos2 = stack.get(CraftorioDataComponents.SCAN_POS_2.get());

        if (pos1 != null) {
            tooltipComponents.add(Component.translatable("misc.craftorio.scan_pos_1_label", pos1.getX(), pos1.getY(), pos1.getZ()).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (pos2 != null) {
            tooltipComponents.add(Component.translatable("misc.craftorio.scan_pos_2_label", pos2.getX(), pos2.getY(), pos2.getZ()).withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    public static void scanAndWriteFile(ServerPlayer player, ItemStack stack) {
        BlockPos pos1 = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        BlockPos pos2 = stack.get(CraftorioDataComponents.SCAN_POS_2.get());

        if (pos1 == null || pos2 == null) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.scan_positions_not_set").withStyle(ChatFormatting.RED));
            return;
        }

        BlockPos min = new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()));
        BlockPos max = new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ()));

        long volume = (long) (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        if (volume > MAX_VOLUME) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.scan_area_large_warning", volume, MAX_VOLUME).withStyle(ChatFormatting.YELLOW));
        }

        Level level = player.level();

        Map<Block, Integer> counts = new TreeMap<>((a, b) ->
                BuiltInRegistries.BLOCK.getKey(a).toString().compareTo(BuiltInRegistries.BLOCK.getKey(b).toString()));

        int totalBlocks = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) continue;

            counts.merge(block, 1, Integer::sum);
            totalBlocks++;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Area Scanner Report\n");
        builder.append("Scanned region: (").append(min.getX()).append(", ").append(min.getY()).append(", ").append(min.getZ())
                .append(") to (").append(max.getX()).append(", ").append(max.getY()).append(", ").append(max.getZ()).append(")\n");
        builder.append("Total blocks (excluding air): ").append(totalBlocks).append("\n\n");

        BigInteger grandTotal = BigInteger.ZERO;
        for (Map.Entry<Block, Integer> entry : counts.entrySet()) {
            Block block = entry.getKey();
            int count = entry.getValue();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            Item item = block.asItem();

            builder.append(id).append(" - Count: ").append(count);
            if (item != Items.AIR) {
                BigInteger valueEach = CraftorioMisc.checkValue(new ItemStack(item), player, false);
                BigInteger valueTotal = valueEach.multiply(BigInteger.valueOf(count));
                grandTotal = grandTotal.add(valueTotal);
                builder.append(" - Value each: ").append(valueEach).append(" - Total value: ").append(valueTotal);
            } else {
                builder.append(" - Value each: N/A - Total value: N/A");
            }
            builder.append("\n");
        }

        builder.append("\nGrand total value: ").append(grandTotal).append("\n");

        try {
            Path dir = player.getServer().getWorldPath(SCANS_DIR);
            Files.createDirectories(dir);
            String fileName = "area_scan_" + min.getX() + "_" + min.getY() + "_" + min.getZ() + "_" + System.currentTimeMillis() + ".txt";
            Files.writeString(dir.resolve(fileName), builder.toString(), StandardCharsets.UTF_8);

            player.sendSystemMessage(Component.translatable("misc.craftorio.area_scan_success", fileName).withStyle(ChatFormatting.GREEN));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write area scan report", e);
            player.sendSystemMessage(Component.translatable("misc.craftorio.area_scan_failed").withStyle(ChatFormatting.RED));
        }
    }
}

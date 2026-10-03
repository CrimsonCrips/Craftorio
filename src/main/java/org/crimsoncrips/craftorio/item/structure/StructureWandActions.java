package org.crimsoncrips.craftorio.item.structure;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.ScannerStickItem;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.server.devtools.CraftorioDevTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class StructureWandActions {

    public static final int STORE = 0;
    public static final int SAVE = 1;

    private StructureWandActions() {}

    public static void handle(ServerPlayer player, ItemStack stack, int action, StructureWandSettings settings) {
        stack.set(CraftorioDataComponents.STRUCTURE_WAND.get(), settings);
        if (action == STORE) return;

        if (!player.canUseGameMasterBlocks()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_no_permission").withStyle(ChatFormatting.RED), false);
            return;
        }

        Optional<ResourceLocation> name = parseName(player, settings.name());
        if (name.isEmpty()) return;

        switch (action) {
            case SAVE -> save(player, stack, settings, name.get());
            default -> {
            }
        }
    }

    private static Path structureFile(MinecraftServer server, ResourceLocation name) {
        return CraftorioDevTools.directory(server).resolve(name.getPath().replace('/', '_') + ".nbt");
    }

    private static Optional<ResourceLocation> parseName(ServerPlayer player, String name) {
        ResourceLocation id = name.isBlank() ? null : ResourceLocation.tryParse(name.trim());
        if (id == null) {
            player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", name), false);
        }
        return Optional.ofNullable(id);
    }

    private static void save(ServerPlayer player, ItemStack stack, StructureWandSettings settings, ResourceLocation name) {
        BlockPos pos1 = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        BlockPos pos2 = stack.get(CraftorioDataComponents.SCAN_POS_2.get());
        if (pos1 == null || pos2 == null) {
            player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_positions_not_set").withStyle(ChatFormatting.RED), false);
            return;
        }

        BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        Vec3i size = new Vec3i(Math.abs(pos1.getX() - pos2.getX()) + 1, Math.abs(pos1.getY() - pos2.getY()) + 1, Math.abs(pos1.getZ() - pos2.getZ()) + 1);
        long volume = (long) size.getX() * size.getY() * size.getZ();
        if (volume > ScannerStickItem.maxVolume()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_too_large", volume, ScannerStickItem.maxVolume()).withStyle(ChatFormatting.RED), false);
            return;
        }

        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(player.serverLevel(), min, size, false, Blocks.STRUCTURE_VOID);
        template.setAuthor(player.getName().getString());

        Path file = structureFile(player.server, name);
        try {
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(template.save(new CompoundTag()), file);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to save the structure {}", name, e);
            player.displayClientMessage(Component.translatable("structure_block.save_failure", name.toString()), false);
            return;
        }

        String relative = CraftorioDevTools.DEV_TOOLS_DIR_NAME + "/" + CraftorioDevTools.directory(player.server).relativize(file).toString().replace('\\', '/');
        player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_saved", name.toString(), relative).withStyle(ChatFormatting.GREEN), false);
    }
}

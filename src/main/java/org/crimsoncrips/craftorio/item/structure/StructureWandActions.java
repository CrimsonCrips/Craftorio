package org.crimsoncrips.craftorio.item.structure;

import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
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
    public static final int LOAD = 2;
    private static final String STRUCTURES_FOLDER = "structures";

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
            case LOAD -> load(player, stack, settings, name.get());
            default -> {
            }
        }
    }

    private static Path structureFile(MinecraftServer server, ResourceLocation name) {
        return CraftorioDevTools.directory(server).resolve(STRUCTURES_FOLDER).resolve(name.getNamespace()).resolve(name.getPath() + ".nbt");
    }

    private static Optional<StructureTemplate> readDevToolsStructure(ServerLevel level, ResourceLocation name) {
        Path file = structureFile(level.getServer(), name);
        if (!Files.exists(file)) return Optional.empty();

        try {
            StructureTemplate template = new StructureTemplate();
            template.load(level.holderLookup(Registries.BLOCK), NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()));
            return Optional.of(template);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to read the structure {}", file, e);
            return Optional.empty();
        }
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
        if (volume > ScannerStickItem.MAX_VOLUME) {
            player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_too_large", volume, ScannerStickItem.MAX_VOLUME).withStyle(ChatFormatting.RED), false);
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

    private static void load(ServerPlayer player, ItemStack stack, StructureWandSettings settings, ResourceLocation name) {
        BlockPos anchor = stack.get(CraftorioDataComponents.SCAN_POS_1.get());
        if (anchor == null) {
            player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_anchor_not_set").withStyle(ChatFormatting.RED), false);
            return;
        }

        ServerLevel level = player.serverLevel();
        Optional<StructureTemplate> template = readDevToolsStructure(level, name);
        if (template.isEmpty()) {
            try {
                template = level.getStructureManager().get(name);
            } catch (ResourceLocationException e) {
                template = Optional.empty();
            }
        }
        if (template.isEmpty()) {
            player.displayClientMessage(Component.translatable("structure_block.load_not_found", name.toString()), false);
            return;
        }

        StructurePlaceSettings placeSettings = new StructurePlaceSettings()
                .setMirror(settings.mirror())
                .setRotation(settings.rotation())
                .setIgnoreEntities(true);
        BoundingBox bounds = template.get().getBoundingBox(placeSettings, anchor);
        BlockPos farCorner = new BlockPos(
                bounds.minX() == anchor.getX() ? bounds.maxX() : bounds.minX(),
                bounds.minY() == anchor.getY() ? bounds.maxY() : bounds.minY(),
                bounds.minZ() == anchor.getZ() ? bounds.maxZ() : bounds.minZ());

        if (!farCorner.equals(stack.get(CraftorioDataComponents.SCAN_POS_2.get()))) {
            stack.set(CraftorioDataComponents.SCAN_POS_2.get(), farCorner);
            player.displayClientMessage(Component.translatable("structure_block.load_prepare", name.toString()), false);
            return;
        }

        if (settings.integrity() < 1.0F) {
            placeSettings.clearProcessors()
                    .addProcessor(new BlockRotProcessor(Mth.clamp(settings.integrity(), 0.0F, 1.0F)))
                    .setRandom(StructureBlockEntity.createRandom(settings.seed()));
        }
        template.get().placeInWorld(level, anchor, anchor, placeSettings, StructureBlockEntity.createRandom(settings.seed()), 2);
        player.displayClientMessage(Component.translatable("structure_block.load_success", name.toString()), false);
    }
}

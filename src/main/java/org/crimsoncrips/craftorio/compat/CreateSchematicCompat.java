package org.crimsoncrips.craftorio.compat;

import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.table.SchematicTableBlockEntity;
import com.simibubi.create.foundation.utility.CreatePaths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.networking.schematic.SaveCreateSchematicPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class CreateSchematicCompat {

    private static final double SHARE_DISTANCE_SQUARED = 16.0 * 16.0;

    private CreateSchematicCompat() {}

    public static boolean isSchematic(ItemStack stack) {
        return stack.has(CraftorioDataComponents.SCHEMATIC.get());
    }

    public static void tickTable(SchematicTableBlockEntity table) {
        Level level = table.getLevel();
        if (level == null || level.isClientSide() || table.isUploading) return;

        ItemStack input = table.inventory.getStackInSlot(0);
        SchematicData data = input.get(CraftorioDataComponents.SCHEMATIC.get());
        if (data == null || data.converted() || !table.inventory.getStackInSlot(1).isEmpty()) return;

        MinecraftServer server = level.getServer();
        if (server == null || !CraftorioSchematics.isLinked(server, data)) return;
        Optional<CompoundTag> structure = CraftorioSchematics.tag(server, data.structure());
        if (structure.isEmpty()) return;

        String owner = data.owner().isEmpty() ? Craftorio.MODID : data.owner();
        String fileName = Craftorio.MODID + "_" + data.contract().getPath().replace('/', '_') + ".nbt";
        Path ownerDirectory = CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve(owner).normalize();
        Path file = ownerDirectory.resolve(fileName).normalize();
        if (!ownerDirectory.startsWith(CreatePaths.UPLOADED_SCHEMATICS_DIR) || !file.startsWith(ownerDirectory)) return;

        try {
            Files.createDirectories(ownerDirectory);
            NbtIo.writeCompressed(structure.get(), file);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to convert the schematic {} into a Create schematic", data.structure(), e);
            return;
        }

        table.inventory.setStackInSlot(1, SchematicItem.create(level, fileName, owner));
        ItemStack marked = input.copy();
        marked.set(CraftorioDataComponents.SCHEMATIC.get(), data.withConverted(true));
        table.inventory.setStackInSlot(0, marked);

        List<SaveCreateSchematicPacket> packets;
        try {
            packets = SaveCreateSchematicPacket.create(fileName, structure.get());
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to send the Create schematic {}", fileName, e);
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean nearby = player.level() == level && player.distanceToSqr(table.getBlockPos().getCenter()) <= SHARE_DISTANCE_SQUARED;
            if (nearby || player.getGameProfile().getName().equals(owner)) {
                for (SaveCreateSchematicPacket packet : packets) {
                    PacketDistributor.sendToPlayer(player, packet);
                }
            }
        }
    }
}

package org.crimsoncrips.craftorio.client.schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.networking.schematic.RequestSchematicStructurePacket;
import org.crimsoncrips.craftorio.networking.schematic.SchematicTransfer;
import org.crimsoncrips.craftorio.server.schematic.SchematicStructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class ClientSchematics {

    private static final Map<ResourceLocation, Optional<SchematicStructure>> STRUCTURES = new HashMap<>();
    private static final Set<ResourceLocation> REQUESTED = new HashSet<>();

    private ClientSchematics() {}

    public static Optional<SchematicStructure> get(ResourceLocation structure) {
        Optional<SchematicStructure> cached = STRUCTURES.get(structure);
        if (cached != null) return cached;

        if (REQUESTED.add(structure)) {
            PacketDistributor.sendToServer(new RequestSchematicStructurePacket(structure));
        }
        return Optional.empty();
    }

    public static void receive(ResourceLocation structure, Optional<CompoundTag> data) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        STRUCTURES.put(structure, data.map(tag -> SchematicStructure.parse(tag, minecraft.level.holderLookup(Registries.BLOCK))));
        CraftorioSchematicRenderer.invalidate(structure);
    }

    public static void saveSchematic(String fileName, CompoundTag data) {
        Path directory = FMLPaths.GAMEDIR.get().resolve("schematics");
        Path file = directory.resolve(fileName).normalize();
        if (!file.startsWith(directory) || !fileName.endsWith(".nbt")) return;

        try {
            Files.createDirectories(directory);
            NbtIo.writeCompressed(data, file);
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to save the schematic schematic {}", fileName, e);
        }
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        STRUCTURES.clear();
        REQUESTED.clear();
        SchematicTransfer.clear();
        CraftorioSchematicRenderer.clear();
    }
}

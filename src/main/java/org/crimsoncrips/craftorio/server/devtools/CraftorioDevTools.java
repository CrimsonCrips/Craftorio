package org.crimsoncrips.craftorio.server.devtools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.Craftorio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CraftorioDevTools {

    public static final String DEV_TOOLS_DIR_NAME = "craftorio_dev_tools";

    public static Path directory(MinecraftServer server) {
        return server.getServerDirectory().resolve(DEV_TOOLS_DIR_NAME);
    }

    public static String toPrettyJson(JsonElement json) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    public static String buildLangJson(Map<String, String> entries) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            object.addProperty(entry.getKey(), entry.getValue());
        }
        return toPrettyJson(object);
    }

    public static void appendUpgradeLang(StringBuilder code, Map<String, String> langEntries) {
        for (Map.Entry<String, String> entry : langEntries.entrySet()) {
            String key = entry.getKey();
            int upgrade = key.indexOf(".upgrade_");
            if (!key.startsWith("misc.") || upgrade < 0) {
                code.append("lang.add(\"").append(key).append("\", \"").append(entry.getValue()).append("\");\n");
                continue;
            }
            String modId = key.substring("misc.".length(), upgrade);
            String base = key.endsWith("_description") ? key.substring(0, key.length() - "_description".length()) : key;
            if (key.endsWith("_description") && langEntries.containsKey(base)) continue;

            String id = base.substring(upgrade + ".upgrade_".length());
            String name = langEntries.getOrDefault(base, "");
            String description = langEntries.getOrDefault(base + "_description", "");
            code.append("lang.addUpgradeLang(");
            if (!modId.equals(Craftorio.MODID)) {
                code.append("\"").append(modId).append("\", ");
            }
            code.append("\"").append(id).append("\", \"").append(name).append("\", \"").append(description).append("\");\n");
        }
    }

    public static void writeCodeFile(ServerPlayer player, String fileNamePrefix, String code) {
        writeFile(player, fileNamePrefix, code, "txt");
    }

    public static String writeFile(Path dir, String fileNamePrefix, String content, String extension) throws IOException {
        Files.createDirectories(dir);
        String fileName = fileNamePrefix + "_" + System.currentTimeMillis() + "." + extension;
        Files.writeString(dir.resolve(fileName), content, StandardCharsets.UTF_8);
        return fileName;
    }

    public static void writeFile(ServerPlayer player, String fileNamePrefix, String content, String extension) {
        try {
            String fileName = writeFile(directory(player.getServer()), fileNamePrefix, content, extension);

            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_success", DEV_TOOLS_DIR_NAME + "/" + fileName).withStyle(ChatFormatting.GREEN));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write dev tools generated code", e);
            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
        }
    }

    public static void writeBundle(ServerPlayer player, String folderNamePrefix, Map<String, String> files) {
        try {
            Path dir = directory(player.getServer()).resolve(folderNamePrefix + "_" + System.currentTimeMillis());
            Files.createDirectories(dir);
            for (Map.Entry<String, String> entry : files.entrySet()) {
                Files.writeString(dir.resolve(entry.getKey()), entry.getValue(), StandardCharsets.UTF_8);
            }

            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_success", DEV_TOOLS_DIR_NAME + "/" + dir.getFileName().toString() + "/").withStyle(ChatFormatting.GREEN));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write dev tools generated bundle", e);
            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
        }
    }
}

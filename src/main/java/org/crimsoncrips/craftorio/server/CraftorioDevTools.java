package org.crimsoncrips.craftorio.server;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.Craftorio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CraftorioDevTools {

    private static final String DEV_TOOLS_DIR_NAME = "craftorio_dev_tools";

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

    public static void writeCodeFile(ServerPlayer player, String fileNamePrefix, String code) {
        writeFile(player, fileNamePrefix, code, "txt");
    }

    public static void writeFile(ServerPlayer player, String fileNamePrefix, String content, String extension) {
        try {
            Path dir = player.getServer().getServerDirectory().resolve(DEV_TOOLS_DIR_NAME);
            Files.createDirectories(dir);
            String fileName = fileNamePrefix + "_" + System.currentTimeMillis() + "." + extension;
            Files.writeString(dir.resolve(fileName), content, StandardCharsets.UTF_8);

            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_success", DEV_TOOLS_DIR_NAME + "/" + fileName).withStyle(ChatFormatting.GREEN));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write dev tools generated code", e);
            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_tools_generate_failed").withStyle(ChatFormatting.RED));
        }
    }

    public static void writeBundle(ServerPlayer player, String folderNamePrefix, Map<String, String> files) {
        try {
            Path dir = player.getServer().getServerDirectory().resolve(DEV_TOOLS_DIR_NAME).resolve(folderNamePrefix + "_" + System.currentTimeMillis());
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

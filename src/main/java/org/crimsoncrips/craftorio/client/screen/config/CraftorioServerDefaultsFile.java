package org.crimsoncrips.craftorio.client.screen.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.crimsoncrips.craftorio.Craftorio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public final class CraftorioServerDefaultsFile {

    private final Path path;
    private final CommentedFileConfig config;

    public CraftorioServerDefaultsFile() {
        this.path = FMLPaths.GAMEDIR.get()
                .resolve(FMLConfig.getConfigValue(FMLConfig.ConfigValue.DEFAULT_CONFIG_PATH))
                .resolve(Craftorio.SERVER_CONFIG_FILE);
        this.config = CommentedFileConfig.builder(this.path).preserveInsertionOrder().build();
        if (Files.exists(this.path)) {
            try {
                this.config.load();
            } catch (Exception e) {
                Craftorio.LOGGER.warn("Failed to read {}, showing default values", this.path, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ModConfigSpec.ConfigValue<T> value) {
        T fallback = value.getDefault();
        Object raw = this.config.get(value.getPath());
        if (raw == null) return fallback;

        if (fallback instanceof Integer && raw instanceof Number number) return (T) Integer.valueOf(number.intValue());
        if (fallback instanceof Long && raw instanceof Number number) return (T) Long.valueOf(number.longValue());
        if (fallback instanceof Double && raw instanceof Number number) return (T) Double.valueOf(number.doubleValue());
        if (fallback instanceof Enum<?> fallbackEnum && raw instanceof String name) {
            for (Object constant : fallbackEnum.getDeclaringClass().getEnumConstants()) {
                if (((Enum<?>) constant).name().equalsIgnoreCase(name)) return (T) constant;
            }
            return fallback;
        }
        return fallback.getClass().isInstance(raw) ? (T) raw : fallback;
    }

    public <T> void set(ModConfigSpec.ConfigValue<T> value, T newValue) {
        this.config.set(value.getPath(), newValue instanceof Enum<?> enumValue ? enumValue.name() : newValue);
    }

    public void save() {
        try {
            Files.createDirectories(this.path.getParent());
            this.config.save();
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to save {}", this.path, e);
        }
    }
}

package spazley.scalingguis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import spazley.scalingguis.ScalingGUIs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("ScalingGUIs");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("ScalingGUIsCustomScales.json");
    private static final Path LEGACY_CONFIG_FILE = CONFIG_DIR.resolve("ScalingGUIs.cfg");
    private static CustomScales config;

    private ConfigManager() {
    }

    public static synchronized void initialize() {
        if (config != null) return;

        int vanillaScale = Minecraft.getInstance().options.guiScale().get();
        config = readConfig();
        config.validate(vanillaScale);
        importLegacySettings();
        mergeDefaultBlacklist();
        save();
    }

    public static CustomScales get() {
        if (config == null) initialize();
        return config;
    }

    public static synchronized void save() {
        if (config == null) return;
        try {
            Files.createDirectories(CONFIG_DIR);
            Path temporary = CONFIG_FILE.resolveSibling(CONFIG_FILE.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(temporary, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveUnsupported) {
                Files.move(temporary, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            ScalingGUIs.LOGGER.error("Unable to save ScalingGUIs config", exception);
        }
    }

    private static CustomScales readConfig() {
        if (!Files.isRegularFile(CONFIG_FILE)) return new CustomScales();
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            CustomScales loaded = GSON.fromJson(reader, CustomScales.class);
            return loaded == null ? new CustomScales() : loaded;
        } catch (Exception exception) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path backup = CONFIG_FILE.resolveSibling("ScalingGUIsCustomScales_BACKUP_" + timestamp + ".json");
            try {
                Files.copy(CONFIG_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException backupError) {
                exception.addSuppressed(backupError);
            }
            ScalingGUIs.LOGGER.error("Invalid ScalingGUIs config; backed it up and created a new one", exception);
            return new CustomScales();
        }
    }

    private static void importLegacySettings() {
        if (config.legacySettingsImported) return;
        config.legacySettingsImported = true;
        if (!Files.isRegularFile(LEGACY_CONFIG_FILE)) return;

        try {
            Properties properties = new Properties();
            for (String rawLine : Files.readAllLines(LEGACY_CONFIG_FILE, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                int separator = line.indexOf('=');
                if (separator <= 0) continue;
                String key = line.substring(0, separator).replaceFirst("^[BSI]:", "").trim();
                properties.setProperty(key, line.substring(separator + 1).trim());
            }
            config.logGuiClassNames = bool(properties, "logNames", config.logGuiClassNames);
            config.persistentLog = bool(properties, "persistentLog", config.persistentLog);
            config.addDefaultBlacklist = bool(properties, "updateBlacklist", config.addDefaultBlacklist);
            config.logGuiClassNamesChat = bool(properties, "logToChat", config.logGuiClassNamesChat);
            config.sortLoggedAlphabetically = bool(properties, "sortNames", config.sortLoggedAlphabetically);
        } catch (IOException exception) {
            ScalingGUIs.LOGGER.warn("Unable to import the legacy ScalingGUIs.cfg", exception);
        }
    }

    private static boolean bool(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static void mergeDefaultBlacklist() {
        if (!config.addDefaultBlacklist) return;
        try (InputStream stream = ConfigManager.class.getResourceAsStream(
                "/assets/scalingguis/default_blacklist.json")) {
            if (stream == null) return;
            Type type = new TypeToken<Map<String, List<String>>>() { }.getType();
            Map<String, List<String>> defaults;
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                defaults = GSON.fromJson(reader, type);
            }
            if (defaults == null) return;
            defaults.forEach((modId, classes) -> {
                if (ModList.get().isLoaded(modId) && classes != null) {
                    config.blacklistGuiClassNames.addAll(classes);
                }
            });
        } catch (Exception exception) {
            ScalingGUIs.LOGGER.warn("Unable to load the default ScalingGUIs blacklist", exception);
        }
    }
}

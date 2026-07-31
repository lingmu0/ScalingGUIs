package spazley.scalingguis.config;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class CustomScales {
    public static final int AUTO_SCALE = 0;
    public static final int MAX_EXPLICIT_SCALE = 8;
    public static final int MAIN_GUI_SCALE = 9;

    public int guiScale = AUTO_SCALE;
    public int hudScale = MAIN_GUI_SCALE;
    public int tooltipScale = MAIN_GUI_SCALE;

    public Map<String, ScaleEntry> customIndividualGuiScales = new LinkedHashMap<>();
    public Map<String, ScaleEntry> customGroupGuiScales = new LinkedHashMap<>();
    public Set<String> loggedGuiClassNames = new LinkedHashSet<>();
    public Set<String> blacklistGuiClassNames = new TreeSet<>();
    public Set<String> dynamicGuiScales = new TreeSet<>();

    public boolean logGuiClassNames;
    public boolean persistentLog = true;
    public boolean addDefaultBlacklist = true;
    public boolean logGuiClassNamesChat;
    public boolean sortLoggedAlphabetically;
    public boolean legacySettingsImported;

    public void validate(int vanillaGuiScale) {
        guiScale = clampExplicit(guiScale, vanillaGuiScale);
        hudScale = clampOptionalMain(hudScale);
        tooltipScale = clampOptionalMain(tooltipScale);

        if (customIndividualGuiScales == null) customIndividualGuiScales = new LinkedHashMap<>();
        if (customGroupGuiScales == null) customGroupGuiScales = new LinkedHashMap<>();
        if (loggedGuiClassNames == null) loggedGuiClassNames = new LinkedHashSet<>();
        if (blacklistGuiClassNames == null) blacklistGuiClassNames = new TreeSet<>();
        if (dynamicGuiScales == null) dynamicGuiScales = new TreeSet<>();

        normalizeEntries(customIndividualGuiScales);
        normalizeEntries(customGroupGuiScales);
    }

    public int screenScale(Screen screen) {
        String className = screen.getClass().getName();
        ScaleEntry individual = customIndividualGuiScales.get(className);
        if (individual != null) return resolve(individual.scale);

        for (Map.Entry<String, ScaleEntry> entry : customGroupGuiScales.entrySet()) {
            try {
                Class<?> type = Class.forName(entry.getKey(), false, screen.getClass().getClassLoader());
                if (type.isInstance(screen)) return resolve(entry.getValue().scale);
            } catch (LinkageError | ClassNotFoundException ignored) {
                // Entries for currently absent mods remain in the config for later use.
            }
        }

        return screen instanceof ChatScreen ? resolve(hudScale) : guiScale;
    }

    public int resolve(int scale) {
        return scale == MAIN_GUI_SCALE ? guiScale : clampExplicit(scale, guiScale);
    }

    public boolean resetBaseScalesTo(int vanillaGuiScale) {
        int normalizedScale = clampExplicit(vanillaGuiScale, AUTO_SCALE);
        boolean changed = guiScale != normalizedScale
                || hudScale != MAIN_GUI_SCALE
                || tooltipScale != MAIN_GUI_SCALE;
        guiScale = normalizedScale;
        hudScale = MAIN_GUI_SCALE;
        tooltipScale = MAIN_GUI_SCALE;
        return changed;
    }

    public List<String> unusedLoggedClassNames() {
        List<String> result = new ArrayList<>(loggedGuiClassNames);
        result.removeAll(customIndividualGuiScales.keySet());
        result.removeAll(customGroupGuiScales.keySet());
        result.removeAll(blacklistGuiClassNames);
        result.removeAll(dynamicGuiScales);
        if (sortLoggedAlphabetically) result.sort(String::compareToIgnoreCase);
        else java.util.Collections.reverse(result);
        return result;
    }

    private void normalizeEntries(Map<String, ScaleEntry> entries) {
        entries.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank());
        entries.replaceAll((className, entry) -> {
            ScaleEntry normalized = entry == null ? new ScaleEntry(className, MAIN_GUI_SCALE) : entry;
            if (normalized.name == null || normalized.name.isBlank()) normalized.name = className;
            normalized.scale = clampOptionalMain(normalized.scale);
            return normalized;
        });
    }

    private static int clampExplicit(int value, int fallback) {
        return value < AUTO_SCALE || value > MAX_EXPLICIT_SCALE ?
                Math.max(AUTO_SCALE, Math.min(MAX_EXPLICIT_SCALE, fallback)) : value;
    }

    private static int clampOptionalMain(int value) {
        return value < AUTO_SCALE || value > MAIN_GUI_SCALE ? MAIN_GUI_SCALE : value;
    }

    public static final class ScaleEntry {
        public String name;
        public int scale = MAIN_GUI_SCALE;

        public ScaleEntry() {
        }

        public ScaleEntry(String name, int scale) {
            this.name = name;
            this.scale = scale;
        }
    }
}
